package com.beepcast.router;

import java.util.Date;
import java.util.Map;

import com.beepcast.api.client.ClientApp;
import com.beepcast.api.client.ClientConf;
import com.beepcast.api.provider.ProviderApp;
import com.beepcast.api.provider.ProviderConf;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.idgen.IdGenApp;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterApp implements Module , RouterApi {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterApp" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private IdGenApp idGenApp;
  private OnlinePropertiesApp oprops;

  private RouterListProviderIds listProviderIds;
  private RouterBufferQueueInfo bufferQueueInfo;

  private RouterConf conf;

  private BoundedLinkedQueue moQueue;
  private BoundedLinkedQueue drQueue;

  private RouterBackupRestoreEngine routerBackupRestoreEngine;

  private RouterMOWorker routerMOWorker;
  private RouterMTWorker routerMTWorker;
  private RouterDRWorker routerDRWorker;

  private RouterManagement routerManagement;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {

    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , not yet initialized" );
      return;
    }

    // start all workers

    if ( routerMOWorker != null ) {
      routerMOWorker.start();
    } else {
      DLog.error( lctx , "Found null object of "
          + "Router MO Worker , can not start " );
    }
    if ( routerMTWorker != null ) {
      routerMTWorker.start();
    } else {
      DLog.error( lctx , "Found null object of "
          + "Router MT Worker , can not start " );
    }
    if ( routerDRWorker != null ) {
      routerDRWorker.start();
    } else {
      DLog.error( lctx , "Found null object of "
          + "Router DR Worker , can not start " );
    }
    if ( routerManagement != null ) {
      routerManagement.start();
    } else {
      DLog.error( lctx , "Found null object of "
          + "Router Management , can not start " );
    }

    // restore message(s) history
    routerBackupRestoreEngine.restoreFromMsgHistory();

  }

  public void stop() {

    if ( !initialized ) {
      DLog.warning( lctx , "Failed to stop , not yet initialized" );
      return;
    }

    // backup message(s) history
    routerBackupRestoreEngine.backupToMsgHistory();

    if ( routerMOWorker != null ) {
      routerMOWorker.stop();
    } else {
      DLog.error( lctx , "Found null object of Router MO Worker "
          + ", can not stop " );
    }
    if ( routerMTWorker != null ) {
      routerMTWorker.stop();
    } else {
      DLog.error( lctx , "Found null object of Router MT Worker "
          + ", can not stop " );
    }
    if ( routerDRWorker != null ) {
      routerDRWorker.stop();
    } else {
      DLog.error( lctx , "Found null object of Router DR Worker "
          + ", can not stop " );
    }
    if ( routerManagement != null ) {
      routerManagement.stop();
    } else {
      DLog.error( lctx , "Found null object of Router Management "
          + ", can not stop " );
    }

    initialized = false;
  }

  public long totalSizeMoBuffer() {
    return bufferQueueInfo.getTotalSizeMoBuffer();
  }

  public long totalSizeMtBuffer() {
    return bufferQueueInfo.getTotalSizeMtBuffer();
  }

  public long totalSizeDrBuffer() {
    return bufferQueueInfo.getTotalSizeDrBuffer();
  }

  public boolean refreshListProviderIds() {
    return listProviderIds.refreshLists();
  }

  public boolean procMoMessage( int transProcType , int clientId , int eventId ,
      String eventCode , String messageId , String messageType , String origin ,
      String providerId , String originAddress , String messageContent ,
      String messageReply , String destinationAddress , Map externalMapParams ,
      Date deliverDateTime ) {
    boolean result = false;

    String headerLog = RouterMessageCommon.headerLog( messageId );

    // log first
    DLog.debug(
        lctx ,
        headerLog + "Process mo message : transProcType = " + transProcType
            + " , clientId = " + clientId + " , eventId = " + eventId
            + " , eventCode = " + eventCode + " , messageType = " + messageType
            + " , origin = " + origin + " , providerId = " + providerId
            + " , originAddress = " + originAddress
            + " , destinationAddress = " + destinationAddress
            + " , externalMapParams = " + externalMapParams
            + " , deliverDateTime = "
            + DateTimeFormat.convertToString( deliverDateTime ) );

    if ( routerMOWorker == null ) {
      DLog.warning( lctx , headerLog + "Failed to process mo message "
          + ", found router mo worker is not ready" );
      return result;
    }

    if ( !routerMOWorker.procMoMessage( transProcType , clientId , eventId ,
        eventCode , messageId , messageType , origin , providerId ,
        originAddress , messageContent , messageReply , destinationAddress ,
        externalMapParams , deliverDateTime ) ) {
      DLog.warning( lctx , headerLog + "Failed to process mo message "
          + ", found failed to process mo message thru router mo worker" );
      return result;
    }

    result = true;
    return result;
  }

  public boolean sendMtMessage( String messageId , int messageType ,
      int messageCount , String messageContent , double debitAmount ,
      String phoneNumber , String providerId , int eventId ,
      int channelSessionId , String senderId , int priority , int retry ,
      Date dateSend , Map params ) {
    boolean result = false;

    String headerLog = RouterMessageCommon.headerLog( messageId );

    if ( routerMTWorker == null ) {
      DLog.warning( lctx , headerLog + "Failed to send mt message "
          + ", found worker is not ready" );
      return result;
    }

    // log first
    if ( conf.isDebug() ) {
      DLog.debug( lctx ,
          headerLog + "Send mt message : messageId = " + messageId
              + " , messageType = " + messageType + " , messageCount = "
              + messageCount + " , debitAmount = " + debitAmount
              + " , providerId = " + providerId + " , eventId = " + eventId
              + " , channelSessionId = " + channelSessionId + " , senderId = "
              + senderId + " , priority = " + priority + " , retry = " + retry
              + " , dateSend = " + DateTimeFormat.convertToString( dateSend )
              + " , params = " + params );
    }

    result = routerMTWorker.sendMtMessage( messageId , messageType ,
        messageCount , messageContent , debitAmount , phoneNumber , providerId ,
        eventId , channelSessionId , senderId , priority , retry , dateSend ,
        params );

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void init( RouterConf conf ) {
    initialized = false;

    if ( conf == null ) {
      DLog.error( lctx , "Failed to init , found null conf" );
      return;
    }
    this.conf = conf;

    listProviderIds.setDebug( conf.isDebug() );
    DLog.debug( lctx , "Set list provider ids module : debug = "
        + listProviderIds.isDebug() );

    moQueue = new BoundedLinkedQueue( conf.getMoQueueSize() );
    DLog.debug( lctx ,
        "Created mo queue with max capacity = " + conf.getMoQueueSize()
            + " msg(s)" );

    drQueue = new BoundedLinkedQueue( conf.getDrQueueSize() );
    DLog.debug( lctx ,
        "Created dr queue with max capacity = " + conf.getDrQueueSize()
            + " msg(s)" );

    routerBackupRestoreEngine = new RouterBackupRestoreEngine( this );
    DLog.debug( lctx , "Created router backup restore engine" );

    DLog.debug( lctx , "Defined mo worker max load = "
        + maxLoadDeliveryMessage() + " msg/sec" );
    DLog.debug( lctx , "Defined mo worker max buffer = "
        + maxBufferDeliveryMessage() + " msg(s)" );
    routerMOWorker = new RouterMOWorker( this );
    DLog.debug( lctx , "Created mo worker thread" );

    DLog.debug( lctx , "Defined mt worker max load = " + maxLoadSubmitMessage()
        + " msg/sec" );
    DLog.debug( lctx , "Defined mt worker max buffer = "
        + maxBufferSubmitMessage() + " msg(s)" );
    routerMTWorker = new RouterMTWorker( this );
    DLog.debug( lctx , "Created mt worker thread" );

    DLog.debug( lctx , "Defined dr worker max load = "
        + maxLoadDeliveryStatus() + " msg/sec" );
    DLog.debug( lctx , "Defined dr worker max buffer = "
        + maxBufferDeliveryStatus() + " msg(s)" );
    routerDRWorker = new RouterDRWorker( this );
    DLog.debug( lctx , "Created dr worker thread" );

    routerManagement = new RouterManagement( this );
    DLog.debug( lctx , "Created management worker thread" );

    DLog.debug( lctx , "Defined message expiry = " + conf.getMsgExpiry()
        + " millis" );
    DLog.debug( lctx ,
        "Defined default priority value = " + conf.getMsgPriority() );

    DLog.debug( lctx , "Defined retryTries = " + conf.getRetryTries()
        + " times" );
    DLog.debug( lctx , "Defined retryDuration = " + conf.getRetryDuration()
        + " millis" );

    // validate is the client and provider module is ready , optional only
    {
      ClientApp clientApp = ClientApp.getInstance();
      ClientConf clientConf = clientApp.getConf();
      if ( clientConf == null ) {
        DLog.warning( lctx , "Found client app is not ready yet "
            + ", found null client conf" );
      }
      ProviderApp providerApp = ProviderApp.getInstance();
      ProviderConf providerConf = providerApp.getProviderConf();
      if ( providerConf == null ) {
        DLog.warning( lctx , "Found provider app is not ready yet "
            + ", found null provider conf" );
      }
    }

    initialized = true;
  }

  public boolean isDebug() {
    boolean result = false;
    if ( conf != null ) {
      result = conf.isDebug();
    }
    return result;
  }

  public String generateMsgId( String prefix ) {
    String strMsgId = null;

    if ( !initialized ) {
      DLog.warning( lctx , "Failed to generate messageId "
          + ", found app not yet initialized" );
      return strMsgId;
    }

    if ( idGenApp == null ) {
      DLog.warning( lctx , "Failed to generate messageId "
          + ", found null mig engine" );
      return strMsgId;
    }

    // if empty just trigger as internal
    if ( ( prefix == null ) || ( prefix.equals( "" ) ) ) {
      prefix = "INT";
    }

    StringBuffer sbMsgId = new StringBuffer();
    sbMsgId.append( prefix );
    sbMsgId.append( idGenApp.nextIdentifier() );
    strMsgId = sbMsgId.toString();

    return strMsgId;
  }

  public boolean verifyRetryValue( String headerLog , int curRetryVal ) {
    boolean result = false;

    // get maximum retry value from conf
    int maxRetryVal = (int) oprops.getLong( "Router.RetryTries" ,
        conf.getRetryTries() );

    // validate maximum retry value must have a value
    if ( maxRetryVal < 1 ) {
      DLog.debug( lctx , headerLog + "Failed to verify retry value "
          + ", found zero maximum retry value" );
      return result;
    }

    // the current retry value must less than the max retry value
    result = curRetryVal < maxRetryVal;

    // log it
    DLog.debug( lctx , headerLog + "Verified retry value : "
        + "currentRetryValue = " + curRetryVal
        + " is less than maximumRetyValue = " + maxRetryVal );

    return result;
  }

  public long readRetryDuration( String headerLog , int curRetryVal ) {
    long duration = 0;
    try {
      // read retry duration from configuration
      duration = Long.parseLong( oprops.getMapString( "Router.RetryMap" ,
          Integer.toString( curRetryVal ) ,
          (String) conf.getRetryMap().get( Integer.toString( curRetryVal ) ) ) );
      DLog.debug( lctx , headerLog + "Read retry duration from "
          + "router map : retry = " + curRetryVal + " , duration = " + duration
          + " ms" );
    } catch ( Exception e ) {
      duration = oprops.getLong( "Router.RetryDuration" ,
          conf.getRetryDuration() );
      DLog.debug( lctx , headerLog + "Read retry duration from "
          + " configuration : duration = " + duration + " ms" );
    }
    return duration;
  }

  public int maxLoadDeliveryStatus() {
    int maxLoad = conf.getDrMaxLoad();
    if ( oprops == null ) {
      return maxLoad;
    }
    maxLoad = (int) oprops.getLong( "Router.DeliveryStatusMaxLoad" , maxLoad );
    return maxLoad;
  }

  public int burstSizeDeliveryStatus() {
    int burstSize = conf.getDrBurstSize();
    return burstSize;
  }

  public int maxBufferDeliveryStatus() {
    int maxBuffer = 100;
    if ( oprops == null ) {
      return maxBuffer;
    }
    maxBuffer = (int) oprops.getLong( "Router.DeliveryStatusMaxBuffer" ,
        maxBuffer );
    return maxBuffer;
  }

  public int maxLoadSubmitMessage() {
    int maxLoad = conf.getMtMaxLoad();
    if ( oprops == null ) {
      return maxLoad;
    }
    maxLoad = (int) oprops.getLong( "Router.SubmitMessageMaxLoad" , maxLoad );
    return maxLoad;
  }

  public int burstSizeSubmitMessage() {
    int burstSize = conf.getMtBurstSize();
    return burstSize;
  }

  public int maxBufferSubmitMessage() {
    int maxBuffer = 100;
    if ( oprops == null ) {
      return maxBuffer;
    }
    maxBuffer = (int) oprops.getLong( "Router.SubmitMessageMaxBuffer" ,
        maxBuffer );
    return maxBuffer;
  }

  public int maxLoadDeliveryMessage() {
    int maxLoad = conf.getMoMaxLoad();
    if ( oprops == null ) {
      return maxLoad;
    }
    maxLoad = (int) oprops.getLong( "Router.DeliveryMessageMaxLoad" , maxLoad );
    return maxLoad;
  }

  public int burstSizeDeliveryMessage() {
    int burstSize = conf.getMoBurstSize();
    return burstSize;
  }

  public int maxBufferDeliveryMessage() {
    int maxBuffer = 100;
    if ( oprops == null ) {
      return maxBuffer;
    }
    maxBuffer = (int) oprops.getLong( "Router.DeliveryMessageMaxBuffer" ,
        maxBuffer );
    return maxBuffer;
  }

  public boolean isSuspendM0Worker() {
    boolean result = false;
    if ( oprops == null ) {
      return result;
    }
    result = oprops.getBoolean( "Router.SuspendMOWorker" , false );
    return result;
  }

  public boolean isSuspendMTWorker() {
    boolean result = false;
    if ( oprops == null ) {
      return result;
    }
    result = oprops.getBoolean( "Router.SuspendMTWorker" , false );
    return result;
  }

  public boolean isSuspendDRWorker() {
    boolean result = false;
    if ( oprops == null ) {
      return result;
    }
    result = oprops.getBoolean( "Router.SuspendDRWorker" , false );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void init() {

    idGenApp = IdGenApp.getInstance();
    oprops = OnlinePropertiesApp.getInstance();

    listProviderIds = new RouterListProviderIds( false );
    bufferQueueInfo = new RouterBufferQueueInfo( listProviderIds );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterListProviderIds getListProviderIds() {
    return listProviderIds;
  }

  public RouterBufferQueueInfo getBufferQueueInfo() {
    return bufferQueueInfo;
  }

  public RouterConf getConf() {
    return conf;
  }

  public BoundedLinkedQueue getMoQueue() {
    return moQueue;
  }

  public BoundedLinkedQueue getDrQueue() {
    return drQueue;
  }

  public RouterMOWorker getRouterMOWorker() {
    return routerMOWorker;
  }

  public RouterMTWorker getRouterMTWorker() {
    return routerMTWorker;
  }

  public RouterDRWorker getRouterDRWorker() {
    return routerDRWorker;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Singleton Pattern
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static final RouterApp INSTANCE = new RouterApp();

  private RouterApp() {
    init();
  }

  public static final RouterApp getInstance() {
    return INSTANCE;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Helper
  //
  // ////////////////////////////////////////////////////////////////////////////

}
