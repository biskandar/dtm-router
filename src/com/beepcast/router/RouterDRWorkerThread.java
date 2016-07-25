package com.beepcast.router;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.ProviderApp;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.util.ProviderMessageOptionalMapParamUtils;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.model.gateway.GatewayLogBean;
import com.beepcast.model.gateway.GatewayLogService;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.onm.OnmApp;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.common.DrBufferBeanTransformUtils;
import com.beepcast.router.dr.DrBufferService;
import com.beepcast.router.dr.ProcessRetryMessage;
import com.beepcast.router.dr.ProcessShutdownMessage;
import com.beepcast.subscriber.SubscriberApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterDRWorkerThread extends Thread {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterDRWorkerThread" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnmApp onmApp;
  private OnlinePropertiesApp opropsApp;
  private ProviderApp providerApp;
  private SubscriberApp subscriberApp;

  private Random random;

  private RouterApp routerApp;
  private RouterConf routerConf;
  private BoundedLinkedQueue drQueue;
  private BoundedLinkedQueue drBatchQueue;

  private DrBufferService drBufferService;
  private GatewayLogService gatewayLogService;
  private RouterDRWorkerDAO routerDRWorkerDAO;
  private RouterToClient routerToClient;

  private boolean activeThread;

  private long delay;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterDRWorkerThread( int index , RouterApp app , RouterConf conf ) {
    super( "RouterDRWorkerThread-" + index );

    String headerLog = "[RouterDRWorkerThread-" + index + "] ";

    initialized = false;

    onmApp = OnmApp.getInstance();
    opropsApp = OnlinePropertiesApp.getInstance();
    providerApp = ProviderApp.getInstance();
    subscriberApp = SubscriberApp.getInstance();

    random = new Random();

    routerApp = app;
    routerConf = conf;

    if ( ( routerApp == null ) || ( routerConf == null ) ) {
      DLog.error( lctx , headerLog
          + "Failed to initialized router dr worker thread "
          + ", found null router app and/or conf" );
      return;
    }

    drQueue = (BoundedLinkedQueue) routerApp.getDrQueue();
    DLog.debug( lctx , headerLog + "Defined dr queue , with max capacity = "
        + drQueue.capacity() + " msg(s)" );

    drBatchQueue = new BoundedLinkedQueue( drQueue.capacity() );
    DLog.debug( lctx , headerLog + "Created dr batch queue , max capacity = "
        + drBatchQueue.capacity() + " msg(s)" );

    drBufferService = new DrBufferService();
    DLog.debug( lctx , headerLog + "Created dr buffer service object" );

    gatewayLogService = new GatewayLogService();
    DLog.debug( lctx , headerLog + "Created gateway log service object" );

    routerDRWorkerDAO = new RouterDRWorkerDAO();
    DLog.debug( lctx , headerLog + "Created RouterDRWorkerDAO object" );

    routerToClient = new RouterToClient();
    DLog.debug( lctx , headerLog + "Created router to client object "
        + ", to handle the client dr message" );

    activeThread = false;
    DLog.debug( lctx , headerLog + "Set active thread = " + activeThread );

    delay = routerConf.getDrMinSleep();
    DLog.debug( lctx , headerLog + "Init delay latency = " + delay + " ms" );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void stopThread() {
    activeThread = false;
  }

  public int sizeDrBatchQueue( boolean percentage ) {
    int size = 0;
    if ( drBatchQueue != null ) {
      if ( percentage ) {
        size = (int) ( (double) drBatchQueue.size()
            / (double) drBatchQueue.capacity() * 100.0 );
      } else {
        size = drBatchQueue.size();
      }
    }
    return size;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void run() {

    if ( !initialized ) {
      DLog.warning( lctx , "Failed to run the thread "
          + ", found not yet initialized" );
      return;
    }

    activeThread = true;
    long delay250ms = 250 , delayCtr = 0;
    DLog.debug( lctx , "Thread started" );
    while ( activeThread ) {

      try {
        Thread.sleep( delay250ms );
      } catch ( InterruptedException e ) {
      }
      delayCtr = delayCtr + delay250ms;
      if ( delayCtr < opropsApp.getLong( "Router.DRWorker.workerSleepMillis" ,
          5000 ) ) {
        continue;
      }
      delayCtr = 0;

      // take out dr message from the queue and extract it
      ProviderMessage providerMessage = extractDrMessage( takeProviderMessageFromDrQueue() );

      // store the valid dr message into batch queue
      storeProviderMessageIntoBatchQueue( providerMessage , true );

      // persist into transaction table ( with/out provider message )
      int drBatchQueueThreshold = ( providerMessage != null ) ? (int) opropsApp
          .getLong( "Router.DRWorker.queueBatchThreshold" ,
              routerConf.getDrLimitRecord() ) : 0;
      persistProviderMessagesFromDrBatchQueue( drBatchQueueThreshold );

    }

    // clean the remaining dr messages from dr batch queue
    persistProviderMessagesFromDrBatchQueue( 0 );

    DLog.debug( lctx , "Thread stopped" );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ProviderMessage takeProviderMessageFromDrQueue() {
    ProviderMessage providerMessage = null;
    try {
      Object object = drQueue.poll( opropsApp.getLong(
          "Router.DRWorker.queueTimeoutMillis" , 5000 ) );
      if ( object == null ) {
        return providerMessage;
      }
      if ( object instanceof ProviderMessage ) {
        providerMessage = (ProviderMessage) object;
      }
    } catch ( InterruptedException e ) {
      DLog.warning( lctx , "Failed to take provider message "
          + "from the dr queue , " + e );
    }
    return providerMessage;
  }

  private boolean persistProviderMessagesFromDrBatchQueue(
      int drBatchQueueThreshold ) {
    boolean result = false;
    try {
      int drBatchQueueSize = drBatchQueue.size();
      if ( drBatchQueueSize < 1 ) {
        return result;
      }
      if ( ( drBatchQueueThreshold > 0 )
          && ( drBatchQueueSize < drBatchQueueThreshold ) ) {
        return result;
      }
      DLog.debug( lctx , "Found total dr message "
          + "in the batch queue is reached " + drBatchQueueSize
          + " msg(s) , will persist them into gateway log table" );
      result = routerDRWorkerDAO.updateGatewayLogStatusMessage(
          takeProviderMessagesFromBatchQueue( drBatchQueueSize ) ,
          routerConf.isDebug() );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to persist provider messages "
          + "from dr batch queue ," + e );
    }
    return result;
  }

  private ProviderMessage extractDrMessage( ProviderMessage providerMessage ) {
    ProviderMessage providerMessageResult = null;

    if ( providerMessage == null ) {
      return providerMessageResult;
    }

    // load based parameter : providerId & externalMessageId
    String providerId = providerMessage.getProviderId();
    String externalMessageId = providerMessage.getExternalMessageId();

    // compose header log
    String headerLog = RouterMessageCommon.headerLog( providerId + "-"
        + externalMessageId );

    // hit the dr count ( for load management )
    RouterLoad.hitDr( 1 );

    // enrich provider message parameter(s)
    Boolean enrichedStatus = enrichProviderMessage( providerMessage );
    if ( enrichedStatus == null ) {
      DLog.warning( lctx , headerLog + "Failed to extract dr message "
          + ", found failed to enrich provider message , remove it." );
      return providerMessageResult;
    }
    if ( !enrichedStatus.booleanValue() ) {
      DLog.warning( lctx , headerLog + "Failed to extract dr message "
          + ", found not matched with any gateway log record" );
      // when found dr unmatch to the gateway log , it will try to retry
      // to store back into dr buffer table
      int curSynchRetry = ProviderMessageOptionalMapParamUtils.getSynchRetry(
          providerMessage , 0 );
      int maxSynchRetry = (int) opropsApp.getLong( "Router.RetryDRSynch" , 0 );
      if ( ( maxSynchRetry > 0 ) && ( curSynchRetry < maxSynchRetry ) ) {
        if ( ( curSynchRetry < 1 ) && ( providerMessage.getPriority() > 0 ) ) {
          providerMessage.setPriority( providerMessage.getPriority() - 1 );
        }
        DLog.debug( lctx , headerLog + "Found synch retry value = "
            + curSynchRetry + " , trying to store delivery report back "
            + "with lower priority = " + providerMessage.getPriority() );
        ProviderMessageOptionalMapParamUtils.setSynchRetry( providerMessage ,
            curSynchRetry + 1 );
        if ( drBufferService.insert( DrBufferBeanTransformUtils
            .transformProviderMessageToDrBufferBean( providerMessage ) ) ) {
          DLog.debug( lctx , headerLog + "Stored delivery report back into "
              + "dr buffer table" );
        } else {
          DLog.warning( lctx , headerLog + "Failed to store delivery report "
              + "back into dr buffer table" );
        }
      } else {
        DLog.warning( lctx , headerLog + "Found current dr message's retry "
            + curSynchRetry + " time(s) has reached the limit" );
      }
      return providerMessageResult;
    }

    // re update the header log
    String internalMessageId = providerMessage.getInternalMessageId();
    if ( !StringUtils.isBlank( internalMessageId ) ) {
      headerLog = RouterMessageCommon.headerLog( internalMessageId );
    }

    // provider broker process report
    if ( !providerApp.processReportEachBroker( providerMessage ) ) {
      DLog.warning( lctx , headerLog + "Failed to process report "
          + "each provider broker" );
    }

    // trap for delivered status
    trapDeliveredStatus( providerMessage );

    // verify number of message
    if ( verifyInvalidNumberMessage( providerMessage ) ) {
      DLog.debug( lctx , headerLog + "Verified message has invalid number" );
    }

    // verify for retry message
    if ( ProviderMessageOptionalMapParamUtils.isCommandRetry( providerMessage ,
        true ) ) {
      if ( ProcessRetryMessage.execute( providerMessage , false ) ) {
        DLog.debug( lctx , headerLog + "Processed retry message" );
      }
    }

    // verify for shutdown message
    if ( ProviderMessageOptionalMapParamUtils.isCommandShutdown(
        providerMessage , true ) ) {
      if ( ProcessShutdownMessage.execute( providerMessage ) ) {
        DLog.debug( lctx , headerLog + "Processed shutdown message" );
      }
    }

    // store messages to client queue
    if ( routerToClient.storeClientDRMessage( providerMessage ) ) {
      DLog.debug( lctx , headerLog + "Stored dr message into "
          + "dr client queue" );
    }

    // return as provider message
    providerMessageResult = providerMessage;
    return providerMessageResult;
  }

  private Boolean enrichProviderMessage( ProviderMessage providerDrMessage ) {
    Boolean result = null;

    if ( providerDrMessage == null ) {
      return result;
    }

    // load based parameter : providerId & externalMessageId
    String providerId = providerDrMessage.getProviderId();
    String externalMessageId = providerDrMessage.getExternalMessageId();

    // verify the must be parameter
    if ( StringUtils.isBlank( providerId ) ) {
      DLog.warning( lctx , "Failed to enrich provider message "
          + ", found providerId is null " );
      return result;
    }
    if ( StringUtils.isBlank( externalMessageId ) ) {
      DLog.warning( lctx , "Failed to enrich provider message "
          + ", found externalMessageId is null " );
      return result;
    }

    // compose header log
    String headerLog = RouterMessageCommon.headerLog( providerId + "-"
        + externalMessageId );

    try {

      // query gateway log based on providerId and externalMessageId
      GatewayLogBean gatewayLogBean = gatewayLogService
          .selectByProviderAndExternalMessageId( providerId , externalMessageId );
      if ( gatewayLogBean == null ) {
        DLog.warning( lctx , headerLog + "Failed to enrich provider message "
            + ", failed to match dr message inside the gateway log table" );
        result = new Boolean( false );
        return result;
      }

      // load record(s)
      long logId = gatewayLogBean.getLogID();
      String internalMessageId = gatewayLogBean.getMessageID();
      long eventId = gatewayLogBean.getEventID();
      long channelSessionId = gatewayLogBean.getChannelSessionID();
      String phone = gatewayLogBean.getPhone();
      String senderID = gatewayLogBean.getSenderID();
      String messageContent = gatewayLogBean.getMessage();
      int messageCount = Integer.parseInt( gatewayLogBean.getMessageCount() );
      int retry = gatewayLogBean.getRetry();
      Date submitDateTime = gatewayLogBean.getDateTm();

      // clean and validate record(s)
      if ( logId < 1 ) {
        DLog.warning( lctx , headerLog + "Can not find logId in "
            + "the gateway log table , disposed the dr message ." );
        return result;
      }
      internalMessageId = StringUtils.trimToEmpty( internalMessageId );
      if ( eventId < 1 ) {
        DLog.warning( lctx , headerLog + "Can not find eventId in "
            + "the gateway log table , disposed the dr message ." );
        return result;
      }
      if ( StringUtils.isBlank( phone ) ) {
        DLog.warning( lctx , headerLog + "Can not find phone in "
            + "the gateway log table , disposed the dr message ." );
        return result;
      }
      if ( senderID != null ) {
        if ( ( senderID.equals( "" ) ) || ( senderID.equals( "''" ) ) ) {
          senderID = null;
        }
      }

      // set additional information
      providerDrMessage.setRecordId( Long.toString( logId ) );
      providerDrMessage.setInternalMessageId( internalMessageId );
      providerDrMessage.setEventId( Long.toString( eventId ) );
      providerDrMessage.setChannelSessionId( Long.toString( channelSessionId ) );
      providerDrMessage.setDestinationAddress( phone );
      providerDrMessage.setOriginAddrMask( senderID );
      providerDrMessage.setMessage( messageContent );
      providerDrMessage.setMessageCount( messageCount );
      providerDrMessage.setRetry( retry );
      providerDrMessage.setSubmitDateTime( submitDateTime );
      providerDrMessage.getOptionalParams().put( "gatewayLogBean" ,
          gatewayLogBean );

      // log it
      DLog.debug(
          lctx ,
          headerLog
              + "Enriched provider message from gateway log table : recordId = "
              + providerDrMessage.getRecordId()
              + " , internalMessageId = "
              + providerDrMessage.getInternalMessageId()
              + " , destinationAddress = "
              + providerDrMessage.getDestinationAddress()
              + " , messageCount = "
              + providerDrMessage.getMessageCount()
              + " , retry = "
              + providerDrMessage.getRetry()
              + " , submitDate = "
              + DateTimeFormat.convertToString( providerDrMessage
                  .getSubmitDateTime() ) );

      result = new Boolean( true );

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to enrich provider message , "
          + e );
    }

    return result;
  }

  private void trapDeliveredStatus( ProviderMessage providerMessage ) {

    String internalStatus = providerMessage.getInternalStatus();
    if ( !StringUtils.equals( internalStatus ,
        RouterDRWorker.DN_STATUS_DELIVERED ) ) {
      return;
    }

    // validate must be parameters

    if ( onmApp == null ) {
      return;
    }

    String providerId = providerMessage.getProviderId();
    if ( StringUtils.isBlank( providerId ) ) {
      return;
    }
    Date submitDate = providerMessage.getSubmitDateTime();
    if ( submitDate == null ) {
      return;
    }
    Date statusDate = providerMessage.getStatusDateTime();
    if ( statusDate == null ) {
      return;
    }

    // prepare module name

    String loadProviderModule = "loadProvider.mt.providerId." + providerId;
    String latencyProviderModule = "holderProvider.latency.providerId."
        + providerId;

    // calculate latency in minutes

    long latencyInMillis = statusDate.getTime() - submitDate.getTime();
    if ( latencyInMillis < 0 ) {
      return;
    }
    int latencyInMinutes = (int) ( latencyInMillis / 60000.0 );

    // compose header log

    String internalMessageId = providerMessage.getInternalMessageId();
    String externalMessageId = providerMessage.getExternalMessageId();
    String headerLog = RouterMessageCommon.headerLog( internalMessageId );

    try {

      // when load is busy than use random as probability

      int load = onmApp.getModuleValue( loadProviderModule );
      if ( ( load > 1 ) && ( random.nextInt( load * 2 ) != 0 ) ) {
        return;
      }

      // set latency for specific provider

      boolean trap = onmApp.setModuleValue( latencyProviderModule ,
          latencyInMinutes );
      if ( !trap ) {
        DLog.warning( lctx , headerLog + "Failed to trap "
            + "delivered status with latency = " + latencyInMinutes
            + " minute(s)" );
        return;
      }

      // log it

      DLog.debug(
          lctx ,
          headerLog + "Trapped a delivered status : " + providerId + ", "
              + externalMessageId + " with total latency in "
              + latencyInMinutes + " minute(s) , submitDate = "
              + DateTimeFormat.convertToString( submitDate )
              + " , statusDate = "
              + DateTimeFormat.convertToString( statusDate ) );

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to trap "
          + "delivered status , " + e );
      return;
    }

  }

  private boolean verifyInvalidNumberMessage( ProviderMessage providerMessage ) {
    boolean result = false;

    // load based parameter

    String internalMessageId = providerMessage.getInternalMessageId();
    String internalStatus = providerMessage.getInternalStatus();
    String phoneNumber = providerMessage.getDestinationAddress();

    // compose header log

    String headerLog = RouterMessageCommon.headerLog( internalMessageId );

    // extract event id

    int eventId = 0;
    try {
      eventId = Integer.parseInt( providerMessage.getEventId() );
    } catch ( NumberFormatException e ) {
      DLog.warning( lctx , headerLog + "Failed to parse event id , " + e );
      return result;
    }
    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to parse event id" );
      return result;
    }

    // extract client id

    int clientId = 0;
    TEvent eventOut = EventCommon.getEvent( eventId );
    if ( eventOut != null ) {
      clientId = eventOut.getClientId();
    }
    if ( clientId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to get client id" );
      return result;
    }

    // when found status as failed invalid

    if ( StringUtils.equals( internalStatus ,
        RouterDRWorker.DN_STATUS_FAILED_INVALID ) ) {
      if ( subscriberApp.doValidated( clientId , phoneNumber , false ) ) {
        DLog.debug( lctx , headerLog + "Set phone number " + phoneNumber
            + " as invalid number for client id = " + clientId );
        result = true;
      }
    }

    // when found status as delivered

    if ( StringUtils.equals( internalStatus ,
        RouterDRWorker.DN_STATUS_DELIVERED ) ) {
      if ( subscriberApp.doValidated( clientId , phoneNumber , true ) ) {
        DLog.debug( lctx , headerLog + "Set phone number " + phoneNumber
            + " as valid number for client id = " + clientId );
        result = true;
      }
    }

    return result;
  }

  private boolean storeProviderMessageIntoBatchQueue(
      ProviderMessage providerMessage , boolean filter ) {
    boolean result = false;

    if ( providerMessage == null ) {
      return result;
    }

    // load based parameters
    String providerId = providerMessage.getProviderId();
    String internalMessageId = providerMessage.getInternalMessageId();
    String externalMessageId = providerMessage.getExternalMessageId();
    String externalStatus = providerMessage.getExternalStatus();

    // compose header log
    String headerLog = RouterMessageCommon.headerLog( internalMessageId );

    try {

      // is filter enabled ?
      if ( filter ) {
        if ( ProviderMessageOptionalMapParamUtils.isInternalMessage(
            providerMessage , true ) ) {
          DLog.debug( lctx , headerLog + "Found as internal dr message "
              + ", bypass to store into batch queue" );
          return result;
        }
        if ( ProviderMessageOptionalMapParamUtils.isCommandIgnored(
            providerMessage , true ) ) {
          DLog.debug( lctx , headerLog + "Found as ignored dr message "
              + ", bypass to store into batch queue" );
          return result;
        }
      }

      // trying to store in the batch channel
      if ( !drBatchQueue.offer( providerMessage , 2500 ) ) {
        DLog.warning( lctx , headerLog + "Failed to put dr message "
            + "in the batch queue : " + providerId + "," + externalMessageId
            + "," + externalStatus );
      }

      // result as inserted
      result = true;

    } catch ( InterruptedException e ) {
      DLog.warning( lctx , headerLog + "Failed to put dr message "
          + "into the batch queue , " + e );
    }
    return result;
  }

  private ArrayList takeProviderMessagesFromBatchQueue( int limit ) {
    ArrayList providerMessages = new ArrayList();
    for ( int i = 0 ; i < limit ; i++ ) {
      try {
        Object objectMessage = drBatchQueue.poll( 2000 );
        if ( objectMessage == null ) {
          continue;
        }
        if ( !( objectMessage instanceof ProviderMessage ) ) {
          continue;
        }
        providerMessages.add( (ProviderMessage) objectMessage );
      } catch ( InterruptedException e ) {
        DLog.warning( lctx , "Failed to take the message "
            + "from DR Batch Queue , " + e );
      }
    }
    return providerMessages;
  }

}
