package com.beepcast.router;

import java.util.Iterator;
import java.util.List;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.router.common.DrBufferBeanTransformUtils;
import com.beepcast.router.dr.DrBufferBean;
import com.beepcast.router.dr.DrBuffersService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterDRWorker extends RouterWorker {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterDRWorker" );

  public static final String DN_STATUS_FAILED_INVALID = "FAILED-INVALID";
  public static final String DN_STATUS_DELIVERED = "DELIVERED";

  public static final String OPROPS_RETRYSWITCHPROVIDER = "Router.RetrySwitchProvider";
  public static final String OPROPS_EXPIRYMESSAGE = "Router.ExpiryMessage";

  public static final Object lockRouterDRWorkerThreads = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private RouterApp routerApp;
  private RouterConf routerConf;

  private DrBuffersService drBuffersService;
  private RouterDRWorkerDAO routerDRWorkerDAO;

  private BoundedLinkedQueue drQueue;

  private RouterDRDBWorkerThread routerDRDBWorkerThread;

  private RouterDRWorkerThread[] arrRouterDRWorkerThreads;
  private int sizeRouterDRWorkerThreads;

  // private List routerDRWorkerThreads;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterDRWorker( RouterApp app ) {
    super();
    initialized = false;

    if ( app == null ) {
      DLog.error( lctx , "Found null in object of RouterApp" );
      return;
    }

    routerApp = app;
    DLog.debug( lctx , "Defined router app" );

    routerConf = routerApp.getConf();
    if ( routerConf == null ) {
      DLog.error( lctx , "Found null in object of RouterConf" );
      return;
    }
    DLog.debug( lctx , "Defined router conf" );

    drBuffersService = new DrBuffersService();
    DLog.debug( lctx , "Created DrBuffersService object" );

    routerDRWorkerDAO = new RouterDRWorkerDAO();
    DLog.debug( lctx , "Created RouterDRWorkerDAO object" );

    drQueue = (BoundedLinkedQueue) app.getDrQueue();
    DLog.debug( lctx ,
        "Defined dr queue , with max capacity = " + drQueue.capacity()
            + " msg(s)" );

    routerDRDBWorkerThread = new RouterDRDBWorkerThread();
    DLog.debug( lctx , "Created RouterDRDBWorkerThread object" );

    // initialized thread workers
    arrRouterDRWorkerThreads = new RouterDRWorkerThread[100];
    for ( int idx = 0 ; idx < arrRouterDRWorkerThreads.length ; idx++ ) {
      arrRouterDRWorkerThreads[idx] = null;
    }

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

    DLog.debug( lctx , "Actived routerDRDBWorker" );
    routerDRDBWorkerThread.start();

    DLog.debug( lctx , "Actived routerDRWorker" );
    refreshArrRouterDRWorkerThreads( (int) opropsApp.getLong(
        "Router.DRWorker.workerSizeMin" , 1 ) );

  }

  public void stop() {

    if ( !initialized ) {
      DLog.warning( lctx , "Can not stop the thread(s) "
          + ", found not yet initialized" );
      return;
    }

    DLog.debug( lctx , "Deactived routerDRDBWorker" );
    routerDRDBWorkerThread.stopThread();

    DLog.debug( lctx , "Deactived routerDRWorker" );
    refreshArrRouterDRWorkerThreads( 0 );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int sizeDrQueue( boolean percentage ) {
    int size = 0;
    if ( drQueue != null ) {
      if ( percentage ) {
        size = (int) ( (double) drQueue.size() / (double) drQueue.capacity() * 100.0 );
      } else {
        size = drQueue.size();
      }
    }
    return size;
  }

  public int sizeDrBatchQueue() {
    int size = 0;
    synchronized ( lockRouterDRWorkerThreads ) {
      for ( int idx = 0 ; idx < sizeRouterDRWorkerThreads ; idx++ ) {
        RouterDRWorkerThread routerDRWorkerThread = arrRouterDRWorkerThreads[idx];
        if ( routerDRWorkerThread == null ) {
          continue;
        }
        size = size + routerDRWorkerThread.sizeDrBatchQueue( false );
      }
    }
    return size;
  }

  public void refreshWorkers( long totalProcessingDrMessages ,
      long totalProcessingMtMessages , boolean isBroadcastRunning ) {

    boolean dbWorkerBusy = false;
    boolean dbWorkerRun = true;
    {

      // setup busy db worker status
      if ( totalProcessingMtMessages > (int) opropsApp.getLong(
          "Router.DRDBWorker.workerBusyMtSizeThreshold" , 50 ) ) {
        dbWorkerBusy = true;
      } else {
        if ( isBroadcastRunning
            && opropsApp.getBoolean(
                "Router.DRDBWorker.workerBusyForRunningBroadcast" , false ) ) {
          dbWorkerBusy = true;
        }
      }
      routerDRDBWorkerThread.setExternalBusy( dbWorkerBusy );

      // setup running db worker status
      if ( totalProcessingMtMessages > (int) opropsApp.getLong(
          "Router.DRDBWorker.workerSuspendMtSizeThreshold" , 50 ) ) {
        dbWorkerRun = false;
      } else {
        if ( isBroadcastRunning
            && opropsApp.getBoolean(
                "Router.DRDBWorker.workerSuspendForRunningBroadcast" , false ) ) {
          dbWorkerRun = false;
        }
      }
      routerDRDBWorkerThread.setExternalRun( dbWorkerRun );

    }

    int workerSizeNew = (int) opropsApp.getLong(
        "Router.DRWorker.workerSizeMin" , 1 );
    {

      // setup new worker size
      if ( totalProcessingDrMessages > (int) opropsApp.getLong(
          "Router.DRWorker.workerSizeRefreshDrSizeThreshold" , 500 ) ) {
        if ( totalProcessingMtMessages <= (int) opropsApp.getLong(
            "Router.DRWorker.workerSizeRefreshMtSizeThreshold" , 50 ) ) {
          if ( !isBroadcastRunning ) {
            synchronized ( lockRouterDRWorkerThreads ) {
              workerSizeNew = sizeRouterDRWorkerThreads;
              int threadSizeMax = (int) opropsApp.getLong(
                  "Router.DRWorker.workerSizeMax" , routerConf.getDrWorker() );
              if ( workerSizeNew < threadSizeMax ) {
                workerSizeNew = workerSizeNew + 1;
              }
            }
          }
        }
      }
      refreshArrRouterDRWorkerThreads( workerSizeNew );

    }

    // log it if need it
    if ( ( routerConf.isDebug() ) || ( totalProcessingDrMessages > 0 )
        || ( totalProcessingMtMessages > 0 ) || isBroadcastRunning ) {
      DLog.debug( lctx , "Refreshed : dbWorkerBusy = " + dbWorkerBusy
          + " , dbWorkerRun = " + dbWorkerRun + " , totalWorkers = "
          + workerSizeNew + " : totalProcessingDrMessages = "
          + totalProcessingDrMessages + " , totalProcessingMtMessages = "
          + totalProcessingMtMessages + " , isBroadcastRunning = "
          + isBroadcastRunning );
    }

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private int moveDrRecordsFromDnBufferToDrQueue( int limitRecords ) {
    int totalRecords = 0;

    // trap delta time
    long deltaTime = System.currentTimeMillis();

    // select available records from dn buffer table
    List listDrBufferBeans = drBuffersService.select( limitRecords );
    if ( ( listDrBufferBeans == null ) || ( listDrBufferBeans.size() < 1 ) ) {
      return totalRecords;
    }

    // prepare string drBufferBeanIds
    StringBuffer sbDrBufferBeanIds = null;

    // iterate and process all the dr buffer beans
    Iterator iterDrBufferBeans = listDrBufferBeans.iterator();
    while ( iterDrBufferBeans.hasNext() ) {
      try {
        DrBufferBean drBufferBean = (DrBufferBean) iterDrBufferBeans.next();
        if ( drBufferBean == null ) {
          continue;
        }
        // convert from dr buffer bean into provider message
        ProviderMessage providerMessage = DrBufferBeanTransformUtils
            .transformDrBufferBeanToProviderMessage( drBufferBean );
        if ( providerMessage == null ) {
          continue;
        }
        // load based parameter : providerId & externalMessageId
        String providerId = providerMessage.getProviderId();
        String externalMessageId = providerMessage.getExternalMessageId();
        // header log
        String headerLog = RouterMessageCommon.headerLog( providerId + "-"
            + externalMessageId );
        // log it
        StringBuffer sbLog = new StringBuffer();
        sbLog.append( headerLog );
        sbLog.append( "Queue provider dr message : recId = " );
        sbLog.append( providerMessage.getRecordId() );
        sbLog.append( ", extStatus = " );
        sbLog.append( providerMessage.getExternalStatus() );
        sbLog.append( ", intStatus = " );
        sbLog.append( providerMessage.getInternalStatus() );
        sbLog.append( ", dtmStatus = " );
        sbLog.append( DateTimeFormat.convertToString( providerMessage
            .getStatusDateTime() ) );
        sbLog.append( ", optParams = " );
        sbLog.append( providerMessage.getOptionalParams() );
        DLog.debug( lctx , sbLog.toString() );
        // store into the queue
        if ( drQueue.offer( providerMessage , 2500 ) ) {
          if ( sbDrBufferBeanIds == null ) {
            sbDrBufferBeanIds = new StringBuffer();
          } else {
            sbDrBufferBeanIds.append( "," );
          }
          sbDrBufferBeanIds.append( drBufferBean.getDnId() );
        }
      } catch ( Exception e ) {
      }
    } // while ( iterDrBufferBeans.hasNext() )

    // deleted after read buffer bean records
    if ( sbDrBufferBeanIds != null ) {
      totalRecords = drBuffersService.delete( sbDrBufferBeanIds.toString() );
    }

    // calculate total process time and log it
    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , "Successfully moved " + totalRecords
        + " dr record(s) from dn_buffer table into dr queue ( take "
        + deltaTime + " ms )" );

    return totalRecords;
  }

  private boolean refreshArrRouterDRWorkerThreads( int threadSizeNew ) {
    boolean refreshed = false;
    synchronized ( lockRouterDRWorkerThreads ) {
      if ( ( threadSizeNew < 0 )
          || ( threadSizeNew > arrRouterDRWorkerThreads.length ) ) {
        DLog.warning( lctx , "Failed to refresh router dr workers thread  "
            + ", found invalid thread size new = " + threadSizeNew );
        return refreshed;
      }
      refreshed = true;
      if ( threadSizeNew == sizeRouterDRWorkerThreads ) {
        // bypass if found the same value
        return refreshed;
      }
      if ( threadSizeNew > sizeRouterDRWorkerThreads ) {
        for ( int idx = sizeRouterDRWorkerThreads ; idx < threadSizeNew ; idx++ ) {
          if ( idx < 0 ) {
            continue;
          }
          if ( arrRouterDRWorkerThreads[idx] != null ) {
            continue;
          }
          arrRouterDRWorkerThreads[idx] = new RouterDRWorkerThread( idx ,
              routerApp , routerConf );
          arrRouterDRWorkerThreads[idx].start();
        }
      } else { // threadSizeNew < sizeRouterDRWorkerThreads
        for ( int idx = sizeRouterDRWorkerThreads - 1 ; idx >= threadSizeNew ; idx-- ) {
          if ( idx < 0 ) {
            continue;
          }
          if ( arrRouterDRWorkerThreads[idx] == null ) {
            continue;
          }
          arrRouterDRWorkerThreads[idx].stopThread();
          arrRouterDRWorkerThreads[idx] = null;
        }
      }
      DLog.debug( lctx , "Refreshed router dr workers thread size : "
          + sizeRouterDRWorkerThreads + " -> " + threadSizeNew );
      sizeRouterDRWorkerThreads = threadSizeNew;
    }
    return refreshed;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class RouterDRDBWorkerThread extends Thread {

    private final Object lockObject;

    private boolean activeThread;
    private long delay;
    private boolean drQueueFullBusy;
    private boolean drQueueFullRun;
    private boolean externalBusy;
    private boolean externalRun;

    public RouterDRDBWorkerThread() {
      super( "RouterDRDBWorkerThread" );
      lockObject = new Object();
    }

    public void stopThread() {
      activeThread = false;
    }

    public void setExternalBusy( boolean externalBusy ) {
      synchronized ( lockObject ) {
        if ( this.externalBusy != externalBusy ) {
          DLog.debug( lctx , "Updated external busy : " + this.externalBusy
              + " -> " + externalBusy );
          this.externalBusy = externalBusy;
        }
      }
    }

    public void setExternalRun( boolean externalRun ) {
      synchronized ( lockObject ) {
        if ( this.externalRun != externalRun ) {
          DLog.debug( lctx , "Updated external run : " + this.externalRun
              + " -> " + externalRun );
          this.externalRun = externalRun;
        }
      }
    }

    public void run() {

      activeThread = true; // activated worker
      drQueueFullBusy = false; // initialized dr queue full busy
      drQueueFullRun = true; // initialized dr queue full run
      externalBusy = false; // initialized external busy
      externalRun = true; // initialized external run

      long delay250ms = 250 , delayCtr = 0;
      DLog.debug( lctx , "Thread started" );
      while ( activeThread ) {

        try {
          Thread.sleep( delay250ms );
        } catch ( InterruptedException e ) {
        }
        delayCtr = delayCtr + delay250ms;
        if ( delayCtr < setupDelay() ) {
          continue;
        }
        delayCtr = 0;

        if ( routerApp.isSuspendDRWorker() ) {
          continue;
        }
        if ( !refreshParamsAndNeedToRun() ) {
          continue;
        }

        // move records from dn buffer table into dr queue
        moveDrRecordsFromDnBufferToDrQueue( (int) opropsApp.getLong(
            "Router.DRDBWorker.limitRecords" , routerConf.getDrLimitRecord() ) );

      }
      DLog.debug( lctx , "Thread stopped" );
    }

    private long setupDelay() {
      synchronized ( lockObject ) {
        long delayNew = opropsApp.getLong( "Router.DRDBWorker.minSleepMillis" ,
            routerConf.getDrMinDBSleep() );
        if ( drQueueFullBusy || externalBusy ) {
          delayNew = opropsApp.getLong( "Router.DRDBWorker.maxSleepMillis" ,
              routerConf.getDrMaxDBSleep() );
        }
        if ( delay != delayNew ) {
          if ( delay < delayNew ) {
            DLog.debug( lctx , "Slow down dr db worker , sleep : " + delay
                + " -> " + delayNew + " ms" );
          } else {
            DLog.debug( lctx , "Make fast dr db worker , sleep : " + delay
                + " -> " + delayNew + " ms" );
          }
        }
        delay = delayNew;
      }
      return delay;
    }

    private boolean refreshParamsAndNeedToRun() {
      boolean needToRun = false;
      synchronized ( lockObject ) {

        // percentage of dr queue size
        int pdqs = sizeDrQueue( true );

        // is the server busy ?
        if ( ( pdqs <= 5 ) || ( pdqs >= 35 ) ) {
          if ( !drQueueFullBusy ) {
            DLog.warning( lctx , "The percentage of dr queue size reached "
                + pdqs + " % , set flag to slow down the dr db process" );
            drQueueFullBusy = true;
          }
        } else {
          if ( drQueueFullBusy ) {
            DLog.debug( lctx , "The percentage of dr queue size reached "
                + pdqs + " % , set flag to make dr db process faster" );
            drQueueFullBusy = false;
          }
        }

        // is the server need to run ?
        if ( pdqs >= 85 ) {
          if ( drQueueFullRun ) {
            DLog.warning( lctx , "The percentage of dr queue size reached "
                + pdqs + " % , stop dr db process for a while" );
            drQueueFullRun = false;
          }
        } else {
          if ( !drQueueFullRun ) {
            DLog.debug( lctx , "The percentage of dr queue size reached "
                + pdqs + " % , re-run stopped dr db process" );
            drQueueFullRun = true;
          }
        }

        // is need to run next ?
        needToRun = ( drQueueFullRun && externalRun );

      }
      return needToRun;
    }

  } // class RouterDRDBWorkerThread

}
