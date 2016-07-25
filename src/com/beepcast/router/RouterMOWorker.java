package com.beepcast.router;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.common.util.concurrent.Channel;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.mo.MoBufferBean;
import com.beepcast.router.mo.MoBufferBeanFactory;
import com.beepcast.router.mo.MoBufferService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterMOWorker extends RouterWorker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterMOWorker" );
  static final Object lockObject = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private RouterApp app;
  private RouterConf conf;
  private OnlinePropertiesApp opropsApp;

  private RouterMOProcessor moProcessor;

  private int totalRouterMOWorkerThread;
  private boolean activeRouterMOWorkerThread;
  private Thread[] routerMOWorkerThreads;
  private Thread routerMODBWorkerThread;

  private boolean isMOWorkerBusy = false;
  private boolean isMODBWorkerBusy = false;

  private MoBufferService moBufferService;

  private int moLimitRecord;
  private BoundedLinkedQueue moQueue;

  private int moBatchThreshold;
  private BoundedLinkedQueue moBatchQueue;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterMOWorker( RouterApp app ) {
    super();
    initialized = false;

    if ( app == null ) {
      DLog.error( lctx , "Found null in object of RouterApp" );
      return;
    }
    this.app = app;

    conf = app.getConf();
    if ( conf == null ) {
      DLog.error( lctx , "Found null in object of RouterConf" );
      return;
    }

    opropsApp = OnlinePropertiesApp.getInstance();
    DLog.debug( lctx , "Prepared online properties app" );

    moProcessor = new RouterMOProcessor();
    DLog.debug( lctx , "Prepared router mo processor" );

    routerMODBWorkerThread = new RouterMODBWorkerThread();
    DLog.debug( lctx , "Created router mo db worker thread" );

    totalRouterMOWorkerThread = conf.getMoWorker();
    routerMOWorkerThreads = new Thread[totalRouterMOWorkerThread];
    for ( int i = 0 ; i < totalRouterMOWorkerThread ; i++ ) {
      routerMOWorkerThreads[i] = new RouterMOWorkerThread( i );
    }
    DLog.debug( lctx , "Created total " + totalRouterMOWorkerThread
        + " mo worker thread(s)" );

    moBufferService = new MoBufferService();
    DLog.debug( lctx , "Created mo buffer service" );

    moLimitRecord = conf.getMoLimitRecord();
    DLog.debug( lctx , "Defined mo query limit = " + moLimitRecord
        + " record(s)" );

    moQueue = (BoundedLinkedQueue) app.getMoQueue();
    if ( moQueue == null ) {
      DLog.error( lctx , "Failed to initialized , found null mo queue" );
      return;
    }
    DLog.debug( lctx , "Loaded mo queue , max size = " + moQueue.capacity()
        + " msg(s)" );

    moBatchThreshold = conf.getMoLimitRecord();
    DLog.debug( lctx , "Defined mo batch threshold = " + moBatchThreshold
        + " record(s)" );

    moBatchQueue = new BoundedLinkedQueue( conf.getMoQueueSize() );
    DLog.debug( lctx ,
        "Created mo batch queue , max size = " + moBatchQueue.capacity()
            + " msg(s)" );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {

    if ( !initialized ) {
      DLog.warning( lctx , "Can not start the thread(s) "
          + ", found not yet initialized" );
      return;
    }

    activeRouterMOWorkerThread = true;

    routerMODBWorkerThread.start();

    for ( int i = 0 ; i < totalRouterMOWorkerThread ; i++ ) {
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
      }
      routerMOWorkerThreads[i].start();
    }

  }

  public void stop() {

    if ( !initialized ) {
      DLog.warning( lctx , "Can not stop the thread(s) "
          + ", found not yet initialized" );
      return;
    }

    activeRouterMOWorkerThread = false;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int sizeMoQueue( boolean percentage ) {
    int size = 0;
    if ( moQueue != null ) {
      if ( percentage ) {
        size = (int) ( (double) moQueue.size() / (double) moQueue.capacity() * 100.0 );
      } else {
        size = moQueue.size();
      }
    }
    return size;
  }

  public boolean procMoMessage( int transProcType , int clientId , int eventId ,
      String eventCode , String messageId , String messageType , String origin ,
      String providerId , String originAddress , String messageContent ,
      String messageReply , String destinationAddress , Map externalMapParams ,
      Date deliverDateTime ) {
    boolean result = false;

    String headerLog = RouterMessageCommon.headerLog( messageId );

    if ( !initialized ) {
      DLog.warning( lctx , headerLog + "Failed to process mo message "
          + ", found worker is not ready yet" );
      return result;
    }

    try {

      // trace mo load

      RouterLoad.hitMo( 1 );

      // read and verify the send date value , need to send now ?

      boolean sendNow = true;
      Date sendDate = DateTimeFormat.convertToDate( (String) externalMapParams
          .get( MoBufferBean.HDRMAPEXTPARAM_SENDDATESTR ) );
      if ( sendDate != null ) {
        Date nowDate = new Date();
        sendNow = sendDate.getTime() <= nowDate.getTime();
      }
      DLog.debug( lctx , headerLog + "Process mo message : sendDate = "
          + DateTimeFormat.convertToString( sendDate ) + " , sendNow = "
          + sendNow );

      // read the global param is the mo message need go to cache first ?

      if ( opropsApp.getBoolean( "Router.CachedMOMessage" , false ) && sendNow ) {

        // log it

        DLog.debug(
            lctx ,
            headerLog + "Queue mo message : transProcType = " + transProcType
                + " , eventId = " + eventId + " , eventCode = " + eventCode
                + " , messageType = " + messageType + " , origin = " + origin
                + " , providerId = " + providerId + " , originAddress = "
                + originAddress + " , destinationAddress = "
                + destinationAddress + " , externalMapParams = "
                + externalMapParams + " , deliverDateTime = "
                + DateTimeFormat.convertToString( deliverDateTime ) );

        // create and verify router mo message

        RouterMessage routerMessage = RouterMessageFactory
            .createRouterMOMessage( transProcType , clientId , eventId ,
                eventCode , messageId , messageType , origin , providerId ,
                originAddress , messageContent , messageReply ,
                destinationAddress , externalMapParams , deliverDateTime );
        if ( routerMessage == null ) {
          DLog.warning( lctx , headerLog + "Failed to process mo message "
              + ", found failed to build a router mo message from factory" );
          return result;
        }

        // queue into mo queue

        if ( moQueue.offer( routerMessage , 2500 ) ) {
          result = true;
          return result;
        }

        DLog.warning( lctx , headerLog + "Found failed to store mo message "
            + "into the router mo queue , trying to persist into table" );

      }

      // log it

      DLog.debug(
          lctx ,
          headerLog + "Persist mo message : transProcType = " + transProcType
              + " , eventId = " + eventId + " , eventCode = " + eventCode
              + " , messageType = " + messageType + " , origin = " + origin
              + " , providerId = " + providerId + " , originAddress = "
              + originAddress + " , destinationAddress = " + destinationAddress
              + " , externalMapParams = " + externalMapParams
              + " , deliverDateTime = "
              + DateTimeFormat.convertToString( deliverDateTime ) );

      // create and verify mo buffer bean

      MoBufferBean bean = MoBufferBeanFactory.createMoBufferBean(
          transProcType , clientId , eventId , eventCode , messageId ,
          messageType , origin , providerId , originAddress , messageContent ,
          messageReply , destinationAddress , externalMapParams , sendDate ,
          false , deliverDateTime );
      if ( bean == null ) {
        DLog.warning( lctx , headerLog + "Failed to process mo message "
            + ", found failed create mo buffer bean" );
        return result;
      }

      // store mo buffer bean into table

      if ( moBufferService.insert( bean ) ) {
        result = true;
        return result;
      }

      DLog.warning( lctx , headerLog + "Failed to process mo message "
          + ", found failed to persist mo message into the table" );

    } catch ( Exception e ) {

      DLog.warning( lctx , headerLog + "Failed to process mo message , " + e );

    }

    return result;
  }

  public Channel getBatchQueue() {
    return moBatchQueue;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private int loadMoRecords() {
    int totalRecords = 0;

    // trap delta time
    long deltaTime = System.currentTimeMillis();

    // read total free space in the mo queue
    int limitRecord = moQueue.capacity() - moQueue.size();
    limitRecord = ( moLimitRecord < limitRecord ) ? moLimitRecord : limitRecord;

    // read records from mo buffer
    String criteria = "WHERE ( send_date IS NULL ) OR ( send_date < NOW() ) ";
    List listMoRecords = moBufferService.select( criteria , limitRecord );
    if ( ( listMoRecords == null ) || ( listMoRecords.size() < 1 ) ) {
      return totalRecords;
    }
    DLog.debug( lctx , "Prepared " + listMoRecords.size()
        + " record(s) from mo buffer ready to process" );

    // prepare to store all mo buffer ids
    StringBuffer sbMoBufferIds = null;

    // iterate and process all mo records
    Iterator iterMoRecords = listMoRecords.iterator();
    while ( iterMoRecords.hasNext() ) {
      MoBufferBean moBufferBean = (MoBufferBean) iterMoRecords.next();
      if ( moBufferBean == null ) {
        continue;
      }
      if ( !loadMoRecord( moBufferBean ) ) {
        continue;
      }
      if ( sbMoBufferIds == null ) {
        sbMoBufferIds = new StringBuffer();
      } else {
        sbMoBufferIds.append( "," );
      }
      sbMoBufferIds.append( moBufferBean.getMoId() );
    }

    if ( sbMoBufferIds == null ) {
      // found empty record(s) , bypass the process
      return totalRecords;
    }

    // deleted records in mo buffer
    totalRecords = moBufferService.deleteRecords( sbMoBufferIds.toString() );

    // trap delta time and log the result
    deltaTime = System.currentTimeMillis() - deltaTime;
    if ( totalRecords > 0 ) {
      DLog.debug( lctx , "Successfully moved " + totalRecords
          + " record(s) from mo buffer into mo queue ( take = " + deltaTime
          + " ms )" );
    }

    return totalRecords;
  }

  private boolean loadMoRecord( MoBufferBean moBufferBean ) {
    boolean result = false;
    if ( moBufferBean == null ) {
      return result;
    }

    String headerLog = RouterMessageCommon.headerLog( moBufferBean
        .getMessageId() );

    try {

      RouterMessage msg = transformIntoRouterMessage( moBufferBean );

      DLog.debug(
          lctx ,
          headerLog + "Queue mo message : " + msg.getProviderId() + ","
              + msg.getOriginAddress() + "," + msg.getDestinationAddress()
              + "," + StringEscapeUtils.escapeJava( msg.getMessage() ) + ","
              + StringEscapeUtils.escapeJava( msg.getReplyMessage() ) + ","
              + msg.getExternalMapParams() + ","
              + DateTimeFormat.convertToString( msg.getDeliverDateTime() ) );

      if ( !moQueue.offer( msg , 2500 ) ) {
        DLog.debug( lctx , headerLog + "Failed to queue mo message "
            + ", timeout occurs" );
        return result;
      }

      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to load mo record , " + e );
    }
    return result;
  }

  private RouterMessage transformIntoRouterMessage( MoBufferBean moBufferBean ) {
    RouterMessage routerMessage = null;
    if ( moBufferBean == null ) {
      return routerMessage;
    }
    routerMessage = new RouterMessage();
    routerMessage.setRecordId( Integer.toString( moBufferBean.getMoId() ) );
    routerMessage.setTransProcType( moBufferBean.getTrnprcType() );
    routerMessage.setClientId( moBufferBean.getClientId() );
    routerMessage.setEventId( moBufferBean.getEventId() );
    routerMessage.setEventCode( moBufferBean.getEventCode() );
    routerMessage.setInternalMessageId( moBufferBean.getMessageId() );
    routerMessage.setMessageType( moBufferBean.getMessageType() );
    routerMessage.setOrigin( moBufferBean.getSourceNode() );
    routerMessage.setProviderId( moBufferBean.getProvider() );
    routerMessage.setOriginAddress( moBufferBean.getPhone() );
    routerMessage.setMessage( moBufferBean.getMessage() );
    routerMessage.setReplyMessage( moBufferBean.getFixedMessage() );
    routerMessage.setDestinationAddress( moBufferBean.getSenderId() );
    routerMessage.getExternalMapParams().putAll(
        moBufferBean.getMapExternalParams() );
    routerMessage.setDeliverDateTime( moBufferBean.getDateInserted() );
    return routerMessage;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class RouterMODBWorkerThread extends Thread {

    private long delay = conf.getMoMinDBSleep();

    private void setupDelay() {
      long newdelay;
      if ( isMODBWorkerBusy ) {
        newdelay = conf.getMoMaxDBSleep();
      } else {
        newdelay = conf.getMoMinDBSleep();
      }
      if ( delay != newdelay ) {
        if ( delay < newdelay ) {
          DLog.debug( lctx , "Slow down the process" );
        } else {
          DLog.debug( lctx , "Make fast the process" );
        }
      }
      delay = newdelay;
    }

    public RouterMODBWorkerThread() {
      super( "RouterMODBWorkerThread" );
      setupDelay();
    }

    public void run() {
      DLog.debug( lctx , "RouterMODBWorkerThread Running ..." );

      int percentageMoQueueSize;
      long deltaTime;
      while ( activeRouterMOWorkerThread ) {

        try {
          setupDelay();
          Thread.sleep( delay );
        } catch ( InterruptedException e ) {
        }

        if ( app.isSuspendM0Worker() ) {
          continue;
        }

        percentageMoQueueSize = ( moQueue.size() * 100 ) / moQueue.capacity();
        synchronized ( lockObject ) {
          if ( percentageMoQueueSize > 50 ) {
            isMODBWorkerBusy = true;
          } else {
            isMODBWorkerBusy = false;
          }
          if ( percentageMoQueueSize > 75 ) {
            continue;
          }
        }

        deltaTime = System.currentTimeMillis();
        loadMoRecords();
        deltaTime = System.currentTimeMillis() - deltaTime;
        synchronized ( lockObject ) {
          if ( deltaTime > ( moLimitRecord * 1000 ) ) {
            isMODBWorkerBusy = true;
          }
        }

      }

    }
  } // RouterMODBWorkerThread

  class RouterMOWorkerThread extends Thread {

    private long delay = conf.getMoMinSleep();

    private void setupDelay() {
      long newdelay;
      if ( isMOWorkerBusy ) {
        newdelay = conf.getMoMaxSleep();
      } else {
        newdelay = conf.getMoMinSleep();
      }
      if ( delay != newdelay ) {
        if ( delay < newdelay ) {
          DLog.debug( lctx , "Slow down the process" );
        } else {
          DLog.debug( lctx , "Make fast the process" );
        }
      }
      delay = newdelay;
    }

    public RouterMOWorkerThread( int index ) {
      super( "RouterMOWorkerThread-" + index );
      setupDelay();
    }

    public void run() {
      DLog.debug( lctx , "RouterMOWorkerThread Running ..." );

      int percentageMoClientQueueSize;
      Object object = null;
      long deltaTime;
      while ( activeRouterMOWorkerThread ) {

        try {
          setupDelay();
          Thread.sleep( delay );
        } catch ( InterruptedException e ) {
        }

        // always assume found empty queue in order to make it faster
        percentageMoClientQueueSize = 0;
        synchronized ( lockObject ) {
          if ( percentageMoClientQueueSize > 50 ) {
            isMOWorkerBusy = true;
          } else {
            isMOWorkerBusy = false;
          }
          if ( percentageMoClientQueueSize > 75 ) {
            continue;
          }
        } // end synchronized

        RouterMessage routerMessage = null;
        try {
          object = moQueue.take();
          if ( object instanceof RouterMessage ) {
            routerMessage = (RouterMessage) object;
          }
        } catch ( InterruptedException e ) {
          DLog.warning( lctx , "Failed to take object from the drQueue , " + e );
        }
        if ( routerMessage == null ) {
          continue;
        }

        deltaTime = System.currentTimeMillis();
        moProcessor.processMoMessage( routerMessage , conf );
        deltaTime = System.currentTimeMillis() - deltaTime;
        synchronized ( lockObject ) {
          if ( deltaTime > 2000 ) {
            DLog.warning( lctx , "Found delay to process mo message "
                + ", take = " + deltaTime + " ms" );
            isMOWorkerBusy = true;
          }
        } // end synchronized

      }

    }

  } // RouterMOWorkerThread

}
