package com.beepcast.router;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.common.ProviderCommon;
import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.beepcast.model.gateway.GatewayLogService;
import com.beepcast.model.provider.ProviderType;
import com.beepcast.model.transaction.MessageType;
import com.beepcast.model.transaction.TransactionCountryUtil;
import com.beepcast.router.common.ProviderResolver;
import com.beepcast.router.common.SendBufferBeanTransformUtils;
import com.beepcast.router.mt.ProviderService;
import com.beepcast.router.mt.SendBufferBean;
import com.beepcast.router.mt.SendBufferService;
import com.beepcast.util.throttle.Throttle;
import com.beepcast.util.throttle.ThrottleProcess;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterMTWorker extends RouterWorker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterMTWorker" );

  static final Object inputLock = new Object();
  static final Object outputLock = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private RouterConf conf;
  private RouterApp app;

  private RouterListProviderIds routerListProviderIds;

  private SendBufferService sendBufferService;
  private ProviderService providerService;
  private GatewayLogService gatewayLogService;

  private BoundedLinkedQueue inouQueue; // input and output queue
  private BoundedLinkedQueue inBatchQueue; // input batch queue
  private BoundedLinkedQueue ouQueue; // output queue

  private boolean inouActive;
  private Thread inouWorker;

  private boolean dbouActive;
  private Thread dbouWorker;

  private boolean ouActive;
  private Thread ouWorker;

  private boolean managementActive;
  private Thread managementWorker;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterMTWorker( RouterApp app ) {
    super();

    initialized = false;

    if ( app == null ) {
      DLog.warning( lctx , "Found null router app , failed to initialized" );
      return;
    }
    this.app = app;

    conf = app.getConf();
    if ( conf == null ) {
      DLog.warning( lctx , "Found null router conf , failed to initialized" );
      return;
    }

    routerListProviderIds = app.getListProviderIds();

    sendBufferService = new SendBufferService();
    DLog.debug( lctx , "Created send buffer service module" );
    providerService = new ProviderService( this.app , conf );
    DLog.debug( lctx , "Created provider service module" );
    gatewayLogService = new GatewayLogService();
    DLog.debug( lctx , "Created gateway log service module" );

    inouQueue = new BoundedLinkedQueue( conf.getMtQueueSize() );
    DLog.debug( lctx , "Created in and out bound queue , with capacity = "
        + inouQueue.capacity() + " msg(s)" );

    inBatchQueue = new BoundedLinkedQueue( conf.getMtLimitRecord() );
    DLog.debug( lctx , "Created in batch queue , with capacity = "
        + inBatchQueue.capacity() + " msg(s)" );

    ouQueue = new BoundedLinkedQueue( conf.getMtLimitRecord() );
    DLog.debug( lctx ,
        "Created out queue , with capacity = " + ouQueue.capacity() + " msg(s)" );

    inouWorker = new RouterMTInOuWorker();
    DLog.debug( lctx , "Created in and out bound worker" );

    dbouWorker = new RouterMTDbOuWorker();
    DLog.debug( lctx , "Created database out bound worker" );

    ouWorker = new RouterMTOuWorker();
    DLog.debug( lctx , "Created out bound worker " );

    managementWorker = new RouterMTManagementWorker();
    DLog.debug( lctx , "Created management worker" );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , found not yet initialized" );
      return;
    }

    inouActive = true;
    inouWorker.start();

    dbouActive = true;
    dbouWorker.start();

    ouActive = true;
    ouWorker.start();

    managementActive = true;
    managementWorker.start();

  }

  public void stop() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to stop , found not yet initialized" );
      return;
    }

    managementActive = false;

    ouActive = false;

    dbouActive = false;

    inouActive = false;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public SendBufferService getSendBufferService() {
    return sendBufferService;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Get Info Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int sizeProcessQueue( boolean percentage ) {
    int size = 0;
    if ( inouQueue != null ) {
      if ( percentage ) {
        size = (int) ( (double) inouQueue.size()
            / (double) inouQueue.capacity() * 100.0 );
      } else {
        size = inouQueue.size();
      }
    }
    return size;
  }

  public int sizeInBatchQueue( boolean percentage ) {
    int size = 0;
    if ( inBatchQueue != null ) {
      if ( percentage ) {
        size = (int) ( (double) inBatchQueue.size()
            / (double) inBatchQueue.capacity() * 100.0 );
      } else {
        size = inBatchQueue.size();
      }
    }
    return size;
  }

  public int sizeOuQueue( boolean percentage ) {
    int size = 0;
    if ( ouQueue != null ) {
      if ( percentage ) {
        size = (int) ( (double) ouQueue.size() / (double) ouQueue.capacity() * 100.0 );
      } else {
        size = ouQueue.size();
      }
    }
    return size;
  }

  public long sizeBuffer() {
    long size = 0;
    if ( sendBufferService != null ) {
      size = sendBufferService.totalRecords();
    }
    return size;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean sendMtMessage( String messageId , int gatewayMessageType ,
      int messageCount , String messageContent , double debitAmount ,
      String phoneNumber , String providerId , int eventId ,
      int channelSessionId , String senderId , int priority , int retry ,
      Date dateSend , Map mapParams ) {
    boolean result = false;

    String headerLog = RouterMessageCommon.headerLog( messageId );

    // trap latency
    long deltaTime = System.currentTimeMillis();

    // log first
    DLog.debug(
        lctx ,
        headerLog + "Send Mt Message : messageId = " + messageId
            + " , gatewayMessageType = " + gatewayMessageType
            + " , messageCount = " + messageCount + " , messageContent = "
            + StringEscapeUtils.escapeJava( messageContent )
            + " , debitAmount = " + debitAmount + " , phoneNumber = "
            + phoneNumber + " , providerId = " + providerId + " , eventId = "
            + eventId + " , channelSessionId = " + channelSessionId
            + " , senderId = " + senderId + " , priority = " + priority
            + " , retry = " + retry + " , dateSend = "
            + DateTimeFormat.convertToString( dateSend ) + " , mapParams = "
            + mapParams );

    // create and verify router message bean
    RouterMessage routerMTMessage = RouterMessageFactory.createRouterMTMessage(
        messageId , gatewayMessageType , messageCount , messageContent ,
        debitAmount , phoneNumber , providerId , eventId , channelSessionId ,
        senderId , priority , retry , dateSend , mapParams , null );
    if ( routerMTMessage == null ) {
      DLog.warning( lctx , headerLog + "Failed to send mt message "
          + ", found failed to create a router mt message from factory" );
      return result;
    }

    // verify router message
    if ( !RouterMessageCommon.verifyRouterMTMessage( routerMTMessage ) ) {
      DLog.warning( lctx , headerLog + "Failed to send mt message "
          + ", found invalid parameters inside" );
      return result;
    }

    // header log
    headerLog = RouterMessageCommon.headerLog( routerMTMessage );

    // trace load mt input
    RouterLoad.hitMtIn( 1 );

    // prepare to store mt message
    boolean stored = false;

    // read and validate the provider profile
    TProvider providerBean = ProviderCommon.getProvider( routerMTMessage
        .getProviderId() );
    if ( providerBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to send mt message "
          + ", found invalid provider id = " + routerMTMessage.getProviderId() );
      return result;
    }
    if ( !providerBean.isActive() ) {
      DLog.warning( lctx , headerLog + "Failed to send mt message "
          + ", found inactive provider id = " + providerBean.getProviderId() );
      return result;
    }

    // verify is using modem as provider ?
    boolean isUsingModemAsProvider = ProviderType.TYPE_MODEM
        .equalsIgnoreCase( providerBean.getType() );
    if ( isUsingModemAsProvider ) {
      DLog.debug( lctx , headerLog + "This mt message is "
          + "using modem as destination provider" );
    }

    // verify the active worker provider id
    // using modem as provider by default no need to validate provider worker
    boolean isValidProviderId = isUsingModemAsProvider;
    if ( !isValidProviderId ) {
      isValidProviderId = routerListProviderIds
          .isExistInListActiveWorkerProviderIdsWithFilterSuspend( providerBean
              .getProviderId() );
    }
    DLog.debug( lctx , headerLog + "Found mt message's provider id "
        + providerBean.getProviderId() + " is "
        + ( isValidProviderId ? "" : "not " ) + "in the valid list" );

    // verify the submit date time
    boolean isSendNow = true;
    Date submitDateTime = routerMTMessage.getSubmitDateTime();
    if ( submitDateTime != null ) {
      Date dateNow = Calendar.getInstance().getTime();
      isSendNow = submitDateTime.getTime() <= dateNow.getTime();
      DLog.debug(
          lctx ,
          headerLog + "Found submit date time "
              + DateTimeFormat.convertToString( submitDateTime )
              + " , send now = " + isSendNow );
    }

    // prepare the condition to submit message straight away ?
    boolean isSuspendMTWorker = app.isSuspendMTWorker();
    boolean isQueueAvailable = sizeProcessQueue( true ) < 90;

    // when found not suspended status , valid provider id
    // , and the queue size is available
    if ( isSendNow && ( !isSuspendMTWorker ) && isValidProviderId
        && ( !isUsingModemAsProvider ) && isQueueAvailable ) {
      // store into the input output queue
      try {
        if ( inouQueue.offer( routerMTMessage , 3000 ) ) {
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.debug( lctx , headerLog + "Stored the mt message "
              + "into input output queue , takes " + deltaTime + " ms" );
          stored = true;
        } else {
          DLog.warning( lctx , headerLog + "Failed to send mt message "
              + ", found failed to store into the input output queue" );
        }
      } catch ( InterruptedException e ) {
        DLog.warning( lctx , headerLog + "Failed to send mt message , " + e );
      }
    } else {
      if ( isSuspendMTWorker ) {
        DLog.debug( lctx , headerLog + "Found app has suspended status "
            + "active on mt worker" );
      }
      if ( !isValidProviderId ) {
        DLog.debug( lctx , headerLog + "Found message has "
            + "invalid provider id" );
      }
      if ( isUsingModemAsProvider ) {
        DLog.debug( lctx , headerLog + "Found message is "
            + "using modem as provider" );
      }
      if ( !isQueueAvailable ) {
        DLog.debug( lctx , headerLog + "Found process queue is full" );
      }
    }

    // if not stored try to store into input batch queue
    if ( !stored ) {
      if ( !insertMTBatchMessage( routerMTMessage ) ) {
        DLog.warning( lctx , headerLog + "Failed to send mt message "
            + ", found failed to store into input batch queue" );
        return result;
      }
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Stored the mt message "
          + "into input mt batch queue , takes " + deltaTime + " ms" );
    }

    // when the buffer fill up , slowing down the process
    slowSendMTMessageProcess();

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void slowSendMTMessageProcess() {
    if ( app == null ) {
      return;
    }
    long curBufferSize = app.totalSizeMtBuffer();
    if ( curBufferSize < 1 ) {
      return;
    }
    long maxBufferSize = app.maxBufferSubmitMessage();
    if ( maxBufferSize < 1 ) {
      return;
    }
    int percBufferSize = (int) ( (double) curBufferSize
        / (double) maxBufferSize * 100.0 );
    try {
      if ( percBufferSize > 95 ) {
        Thread.sleep( 500 );
        return;
      }
      if ( percBufferSize > 85 ) {
        Thread.sleep( 100 );
        return;
      }
      if ( percBufferSize > 75 ) {
        Thread.sleep( 50 );
        return;
      }
      if ( percBufferSize > 50 ) {
        Thread.sleep( 20 );
        return;
      }
      if ( percBufferSize > 25 ) {
        Thread.sleep( 10 );
        return;
      }
    } catch ( InterruptedException e ) {
    }
  }

  private boolean processMTMessage( RouterMessage routerMTMessage ) {
    boolean result = false;

    // validate must be parameters
    if ( routerMTMessage == null ) {
      return result;
    }

    // header log
    String headerLog = RouterMessageCommon.headerLog( routerMTMessage );

    // trace load mt output
    RouterLoad.hitMtOu( 1 );

    // validate and resolve provider
    if ( !routerListProviderIds
        .isExistInListActiveWorkerProviderIdsWithFilterSuspend( routerMTMessage
            .getProviderId() ) ) {
      String providerIdNew = ProviderResolver.resolveNewProvider( headerLog ,
          routerMTMessage.getDestinationAddress() ,
          routerMTMessage.getEventId() , routerMTMessage.getProviderId() ,
          routerMTMessage.getProviderId() );
      DLog.debug( lctx , headerLog + "Found inactive provider "
          + " , resolve with the new one : " + routerMTMessage.getProviderId()
          + " -> " + providerIdNew );
      routerMTMessage.setProviderId( providerIdNew );
    }

    // send message thru provider app
    if ( !providerService.sendMessage( routerMTMessage ) ) {

      // increase buffer retry value
      int bufferRetry = RouterMessageCommon
          .getBufferRetryFromInternalMapParams( routerMTMessage , 0 ) + 1;
      RouterMessageCommon.setBufferRetryFromInternalMapParams( routerMTMessage ,
          bufferRetry );

      // log it
      DLog.warning( lctx , headerLog + "Failed to submit mt message "
          + "to provider app , set to increase the buffer retry value became "
          + bufferRetry );

      return result;
    }

    // processed successfully
    result = true;
    return result;
  }

  private List processMTMessages( List listAvailRouterMTMessages ) {
    List listUnStoredRouterMTMessages = null;

    // validate must be parameters
    if ( listAvailRouterMTMessages == null ) {
      return listUnStoredRouterMTMessages;
    }
    int sizeAvailRouterMTMessages = listAvailRouterMTMessages.size();
    if ( sizeAvailRouterMTMessages < 1 ) {
      return listUnStoredRouterMTMessages;
    }

    // iterate and process each mt message
    int totalProcessed = 0;
    RouterMessage routerMTMessage = null;
    listUnStoredRouterMTMessages = new ArrayList();
    Iterator iterAvailRouterMTMessages = listAvailRouterMTMessages.iterator();
    while ( iterAvailRouterMTMessages.hasNext() ) {
      routerMTMessage = (RouterMessage) iterAvailRouterMTMessages.next();
      if ( routerMTMessage == null ) {
        continue;
      }
      if ( !processMTMessage( routerMTMessage ) ) {
        listUnStoredRouterMTMessages.add( routerMTMessage );
        continue;
      }
      totalProcessed = totalProcessed + 1;
    }

    // log if there is a missing message procesed
    if ( ( sizeAvailRouterMTMessages > 0 )
        && ( totalProcessed != sizeAvailRouterMTMessages ) ) {
      DLog.warning( lctx , "Submitted mt messages thru provider app : "
          + totalProcessed + "/" + sizeAvailRouterMTMessages + " msg(s) "
          + ", found unstored mt message(s)" );
    }

    return listUnStoredRouterMTMessages;
  }

  private int processUnStoredMTMessages( List listUnStoredRouterMTMessages ) {
    int totalProcessed = 0;
    if ( listUnStoredRouterMTMessages == null ) {
      return totalProcessed;
    }
    int sizeUnStoredRouterMTMessages = listUnStoredRouterMTMessages.size();
    if ( sizeUnStoredRouterMTMessages < 1 ) {
      return totalProcessed;
    }
    Iterator iterUnStoredRouterMTMessages = listUnStoredRouterMTMessages
        .iterator();
    while ( iterUnStoredRouterMTMessages.hasNext() ) {
      processUnStoredMTMessage( (RouterMessage) iterUnStoredRouterMTMessages
          .next() );
    }
    return totalProcessed;
  }

  private boolean processUnStoredMTMessage( RouterMessage routerMTMessage ) {
    boolean result = false;
    if ( routerMTMessage == null ) {
      return result;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( routerMTMessage );

    // if buffer retry is acceptable , try to insert back into the send buffer

    int curBufferRetry = RouterMessageCommon
        .getBufferRetryFromInternalMapParams( routerMTMessage , 0 );
    int maxBufferRetry = (int) opropsApp.getLong( "Router.RetryMTBuffer" , 0 );
    if ( ( maxBufferRetry > 0 ) && ( curBufferRetry <= maxBufferRetry ) ) {
      DLog.debug( lctx , headerLog + "Found mt message buffer retry value = "
          + curBufferRetry + " , still hasn't reached the max yet "
          + ", trying to store back into send buffer" );
      if ( insertMTBatchMessage( routerMTMessage ) ) {
        DLog.debug( lctx , headerLog + "Inserted unstored mt message "
            + "into the mt batch queue back" );
        result = true;
        return result;
      }
    }

    // log it

    DLog.warning( lctx , headerLog + "Failed to insert unstored mt message "
        + "into mt batch queue and/or the current buffer retry value = "
        + curBufferRetry + " has reached the maximum "
        + ", trying to store into gateway log table" );

    // if failed try to insert into gateway log table

    try {

      String gatewayMessageType = MessageType.messageTypeToString( Integer
          .parseInt( (String) routerMTMessage.getInternalMapParams().get(
              "gatewayMessageType" ) ) );
      int countryId = TransactionCountryUtil.getCountryId( routerMTMessage
          .getDestinationAddress() );
      DLog.debug(
          lctx ,
          headerLog + "Inserting unstored mt message "
              + "into gateway log table : gatewayMessageType = "
              + gatewayMessageType + " , messageCount = "
              + routerMTMessage.getMessageCount() + " , eventId = "
              + routerMTMessage.getEventId() + " , channelSessionId = "
              + routerMTMessage.getChannelSessionId() + " , providerId = "
              + routerMTMessage.getProviderId() + " , debitAmount = "
              + routerMTMessage.getDebitAmount() + " , countryId = "
              + countryId + " , status = ERROR" );
      if ( gatewayLogService.insertOutgoingMessage(
          routerMTMessage.getInternalMessageId() , gatewayMessageType ,
          routerMTMessage.getMessageCount() , routerMTMessage.getEventId() ,
          routerMTMessage.getChannelSessionId() ,
          routerMTMessage.getProviderId() , routerMTMessage.getDebitAmount() ,
          routerMTMessage.getSubmitDateTime() ,
          routerMTMessage.getDestinationAddress() , countryId ,
          routerMTMessage.getOriginAddrMask() , routerMTMessage.getMessage() ,
          "ERROR" ) ) {
        DLog.debug( lctx , headerLog + "Inserted unstored mt message "
            + "into the gateway log table with status ERROR" );
        Thread.sleep( 30 );
        result = true;
        return result;
      }
      DLog.warning( lctx , headerLog + "Failed to insert unstored mt message "
          + "into gateway log table" );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to insert unstored mt message "
          + "into gateway log table , " + e );
    }
    DLog.warning( lctx , headerLog + "Failed to process unstored mt message" );
    return result;
  }

  private List retrieveRouterMTMessagesFromTable( int limitRecords ) {
    List listRouterMTMessages = new ArrayList();

    // read list active provider ids
    List listActiveProviderIds = routerListProviderIds
        .listActiveWorkerProviderIdsWithoutFilterSuspend();
    if ( ( listActiveProviderIds == null )
        || ( listActiveProviderIds.size() < 1 ) ) {
      return listRouterMTMessages;
    }

    // read available send buffer beans
    List listSendBufferBeans = sendBufferService
        .selectActiveBeansByListProviderIds( limitRecords ,
            listActiveProviderIds );
    if ( ( listSendBufferBeans == null ) || ( listSendBufferBeans.size() < 1 ) ) {
      return listRouterMTMessages;
    }

    // delete all the read send buffer beans
    int totalDeleted = sendBufferService.deleteBeans( listSendBufferBeans );
    if ( totalDeleted < 1 ) {
      DLog.warning( lctx , "Failed to retrieve router mt messages "
          + "from table , found failed to delete the record(s)" );
      return listRouterMTMessages;
    }

    // convert from send buffer beans into router mt messages
    Iterator iterSendBufferBeans = listSendBufferBeans.iterator();
    while ( iterSendBufferBeans.hasNext() ) {
      SendBufferBean sendBufferBean = (SendBufferBean) iterSendBufferBeans
          .next();
      if ( sendBufferBean == null ) {
        continue;
      }
      RouterMessage routerMTMessage = SendBufferBeanTransformUtils
          .transformSendBufferBeanToRouterMTMessage( sendBufferBean );
      if ( routerMTMessage == null ) {
        continue;
      }
      listRouterMTMessages.add( routerMTMessage );
    } // while ( iterSendBufferBeans.hasNext() )

    return listRouterMTMessages;
  }

  private boolean insertMTBatchMessage( RouterMessage routerMTMessage ) {
    boolean result = false;
    if ( routerMTMessage == null ) {
      return result;
    }
    String headerLog = RouterMessageCommon.headerLog( routerMTMessage );
    synchronized ( inputLock ) {
      try {
        inBatchQueue.put( routerMTMessage );
      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to store mt message "
            + "into the input batch queue , " + e );
        return result;
      }
      if ( sizeInBatchQueue( true ) > 95 ) {
        long deltaTime = System.currentTimeMillis();
        int sizeInBatchQueue = sizeInBatchQueue( false );
        DLog.debug( lctx , headerLog
            + "Found batch queue is fill up almost 95% "
            + ", start the batch inserting total " + sizeInBatchQueue
            + " router mt message(s) into send buffer table" );
        int sizeOuBatchQueue = insertMTBatchMessages( sizeInBatchQueue( false ) );
        deltaTime = System.currentTimeMillis() - deltaTime;
        DLog.debug( lctx , headerLog + "Read total " + sizeOuBatchQueue
            + " router mt message(s) have inserted to the send buffer table "
            + ", missing total " + ( sizeInBatchQueue - sizeOuBatchQueue )
            + " record(s) , and it take " + deltaTime + " ms" );
      }
    }
    result = true;
    return result;
  }

  private int insertMTBatchMessages( int sizeInBatchQueue ) {
    int totalRecords = 0;
    if ( sizeInBatchQueue < 1 ) {
      return totalRecords;
    }
    StringBuffer sbMsgIds = new StringBuffer();
    List listSendBufferBeans = new ArrayList();
    for ( int i = 0 ; i < sizeInBatchQueue ; i++ ) {
      RouterMessage routerMTMessage = null;
      try {
        routerMTMessage = (RouterMessage) inBatchQueue.poll( 3000 );
      } catch ( Exception e ) {
        continue;
      }
      if ( routerMTMessage == null ) {
        continue;
      }
      String headerLog = RouterMessageCommon.headerLog( routerMTMessage );
      SendBufferBean sendBufferBean = SendBufferBeanTransformUtils
          .transformRouterMTMessageToSendBufferBean( routerMTMessage );
      if ( sendBufferBean == null ) {
        DLog.warning( lctx , headerLog
            + "Failed to insert router message into send buffer "
            + ", found failed to transform into send buffer bean" );
        continue;
      }
      listSendBufferBeans.add( sendBufferBean );
      sbMsgIds.append( sendBufferBean.getMessageId() + "," );
    }
    if ( listSendBufferBeans.size() < 1 ) {
      return totalRecords;
    }
    DLog.debug( lctx , "Read total " + listSendBufferBeans.size()
        + " send buffer bean(s) ready to insert into send buffer table : "
        + sbMsgIds.toString() );
    totalRecords = sendBufferService.insertBeans( listSendBufferBeans );
    return totalRecords;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  private class RouterMTInOuWorker extends Thread {

    private long readQueueTimeout;

    public RouterMTInOuWorker() {
      super( "RouterMTInOuWorker" );
      readQueueTimeout = conf.getMtMaxSleep();
      DLog.debug( lctx , "Created router mt input to output queue worker "
          + ", with read timeout = " + readQueueTimeout + " ms" );
    }

    public void run() {
      DLog.debug( lctx , "Thread started" );
      while ( inouActive ) {
        try {
          Object objectMessage = inouQueue.poll( readQueueTimeout );
          if ( objectMessage == null ) {
            continue;
          }
          RouterMessage routerMTMessage = null;
          if ( objectMessage instanceof RouterMessage ) {
            routerMTMessage = (RouterMessage) objectMessage;
          }
          if ( routerMTMessage == null ) {
            continue;
          }
          if ( !processMTMessage( routerMTMessage ) ) {
            processUnStoredMTMessage( routerMTMessage );
          }
        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to process mt message , " + e );
        }
      } // end while input output active
      DLog.debug( lctx , "Thread stopped" );
    }

  }

  private class RouterMTDbOuWorker extends Thread {

    public RouterMTDbOuWorker() {
      super( "RouterMTDbOuWorker" );
      DLog.debug( lctx , "Created router mt database to output queue worker" );
    }

    public void run() {
      DLog.debug( lctx , "Thread started" );
      boolean busyCur = false , busyNew = false;
      while ( dbouActive ) {

        if ( busyCur != busyNew ) {
          DLog.debug( lctx , "Switch the database output worker became "
              + ( busyNew ? "busy" : "idle" ) );
          busyCur = busyNew;
        }

        // delay process
        try {
          Thread.sleep( sleep( busyCur ) );
        } catch ( InterruptedException e ) {
        }

        // stop process when found suspended status
        if ( app.isSuspendMTWorker() ) {
          busyNew = true;
          continue;
        }

        // get perc size target queue
        int percSize = sizeOuQueue( true );

        // when perc size target queue > 90 %
        // than stop with delay the process
        if ( percSize > 90 ) {
          busyNew = true;
          continue;
        }

        // when perc size target queue > 50 %
        // than continue with delay the process
        if ( percSize > 50 ) {
          busyNew = true;
        }

        // otherwise continue the process without any delay
        else {
          busyNew = false;
        }

        // get the current available size of the queue
        int availSize = ouQueue.capacity() - ouQueue.size();

        // retrieve the list available records
        List listAvailRouterMTMessages = retrieveRouterMTMessagesFromTable( availSize );

        // when the list available records is empty, it will not doing anythings
        if ( ( listAvailRouterMTMessages == null )
            || ( listAvailRouterMTMessages.size() < 1 ) ) {
          continue;
        }

        // when the list available records is not empty
        // process the mt messages with flag as busy
        busyNew = true;

        // log debug
        if ( conf.isDebug() ) {
          RouterMessageCommon.debugLog( listAvailRouterMTMessages );
        }

        // process the available records
        List listUnStoredRouterMTMessages = processMTMessages( listAvailRouterMTMessages );

        // when found list failed records , it will
        // store back to the gateway log with failed status
        if ( ( listUnStoredRouterMTMessages != null )
            && ( listUnStoredRouterMTMessages.size() > 0 ) ) {
          processUnStoredMTMessages( listUnStoredRouterMTMessages );
        }

      } // end while database output active
      DLog.debug( lctx , "Thread stopped" );
    }

    private int sleep( boolean busy ) {
      return busy ? conf.getMtMinDBSleep() : conf.getMtMaxDBSleep();
    }

  }

  private class RouterMTOuWorker extends Thread implements ThrottleProcess {

    private int idxIter;
    private int maxIter;

    public RouterMTOuWorker() {
      super( "RouterMTOuWorker" );
      idxIter = 0;
      maxIter = 2500;
    }

    public void run() {
      DLog.debug( lctx , "Thread started" );

      // prepare throughput
      int burstSizeCur = 0;
      int maxLoadCur = 0;

      while ( ouActive ) {

        // prepare throughput
        int burstSizeNew = app.burstSizeSubmitMessage();
        int maxLoadNew = app.maxLoadSubmitMessage();

        // log when there is change
        if ( ( burstSizeCur != burstSizeNew ) || ( maxLoadCur != maxLoadNew ) ) {
          burstSizeCur = burstSizeNew;
          maxLoadCur = maxLoadNew;
          DLog.debug( lctx , "Start to process MT Messages , burstSize = "
              + burstSizeCur + " pipe(s) , maxLoad = " + maxLoadCur
              + " msg/sec" );
        }

        // process load
        long deltaTime = System.currentTimeMillis();
        Throttle throttle = new Throttle( burstSizeCur , maxLoadCur );
        if ( conf.isDebug() ) {
          DLog.debug( lctx , "Started throttle to process mt message(s) "
              + ", burstSize = " + burstSizeCur + " pipe(s) , maxLoad = "
              + maxLoadCur + " msg/sec" );
        }
        try {
          throttle.run( 1 , this );
        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to run the throttle , " + e );
        }
        deltaTime = System.currentTimeMillis() - deltaTime;
        if ( conf.isDebug() ) {
          DLog.debug( lctx , "Stopped throttle to process mt message(s) "
              + ", take = " + deltaTime + " ms" );
        }

      } // end while output active

      DLog.debug( lctx , "Thread stopped" );
    }

    public boolean process() {
      // take out router message from output queue , with timeout 15 sec(s)
      RouterMessage routerMTMessage = null;
      try {
        Object objectMessage = ouQueue.poll( 15000 );
        if ( objectMessage instanceof RouterMessage ) {
          routerMTMessage = (RouterMessage) objectMessage;
        }
      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to take out a router message "
            + "from output queue , " + e );
      }
      // refresh the throughput when found empty timeout output queue
      if ( routerMTMessage == null ) {
        return true;
      }
      // process mt message
      processMTMessage( routerMTMessage );
      // there is a maximum loop to refresh the throughput
      idxIter = idxIter + 1;
      if ( idxIter >= maxIter ) {
        idxIter = 0;
        return true;
      }
      // return as loop
      return false;
    }

  }

  private class RouterMTManagementWorker extends Thread {

    public RouterMTManagementWorker() {
      super( "RouterMTManagementWorker" );
    }

    public void run() {
      DLog.debug( lctx , "Thread started" );

      int sizeInBatchQueueTemp = 0;
      int cntrInBatchQueueIdle = 0;

      while ( managementActive ) {

        try {
          Thread.sleep( 5000 );
        } catch ( InterruptedException e ) {
        }

        { // when found input batch queue idle
          // will clean messages inside
          int sizeInBatchQueue = sizeInBatchQueue( false );
          if ( sizeInBatchQueue > 0 ) {
            if ( sizeInBatchQueue == sizeInBatchQueueTemp ) {
              cntrInBatchQueueIdle = cntrInBatchQueueIdle + 1;
            } else {
              cntrInBatchQueueIdle = 0;
            }
          } else {
            cntrInBatchQueueIdle = 0;
          }
          if ( cntrInBatchQueueIdle > 2 ) {
            DLog.debug( lctx , "Found input batch queue is idle "
                + ", clean total " + sizeInBatchQueue + " message(s) inside ." );
            insertMTBatchMessages( sizeInBatchQueue );
          }
          sizeInBatchQueueTemp = sizeInBatchQueue;
        }

      } // end while management active

      DLog.debug( lctx , "Thread stopped" );
    }

  }

}
