package com.beepcast.router;

import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.onm.OnmApp;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterManagement {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterManagement" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnmApp onmApp;
  private OnlinePropertiesApp opropsApp;

  private RouterApp routerApp;
  private RouterConf routerConf;

  private RouterBufferQueueInfo routerBufferQueueInfo;
  private RouterListProviderIds routerListProviderIds;

  private RouterMOWorker routerMOWorker;
  private RouterMTWorker routerMTWorker;
  private RouterDRWorker routerDRWorker;

  private BoundedLinkedQueue moQueue;
  private BoundedLinkedQueue drQueue;

  private BoundedLinkedQueue moBatchQueue;

  private boolean activeRouterManagementThread;
  private Thread routerManagementThread;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterManagement( RouterApp app ) {
    initialized = false;

    onmApp = OnmApp.getInstance();
    opropsApp = OnlinePropertiesApp.getInstance();

    if ( app == null ) {
      DLog.error( lctx , "Found null in object of RouterApp" );
      return;
    }
    routerApp = app;

    routerConf = app.getConf();
    if ( routerConf == null ) {
      DLog.error( lctx , "Found null in object of RouterConf" );
      return;
    }

    routerListProviderIds = routerApp.getListProviderIds();
    routerBufferQueueInfo = routerApp.getBufferQueueInfo();

    routerMOWorker = routerApp.getRouterMOWorker();
    routerMTWorker = routerApp.getRouterMTWorker();
    routerDRWorker = routerApp.getRouterDRWorker();

    if ( routerMOWorker != null ) {
      moBatchQueue = (BoundedLinkedQueue) routerMOWorker.getBatchQueue();
    }

    moQueue = (BoundedLinkedQueue) routerApp.getMoQueue();
    drQueue = (BoundedLinkedQueue) routerApp.getDrQueue();

    activeRouterManagementThread = false;
    routerManagementThread = new RouterManagementThread();

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

    routerManagementThread.start();

  }

  public void stop() {

    if ( !initialized ) {
      DLog.warning( lctx , "Can not stop the thread(s) "
          + ", found not yet initialized" );
      return;
    }

    activeRouterManagementThread = false;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void refreshSystemLoads() {
    try {

      int load = 0;

      load = RouterLoad.hitMo();
      load = load > 0 ? load : 0;
      onmApp.setModuleValue( "loadRouter.mo" , load );

      load = RouterLoad.hitMtIn();
      load = load > 0 ? load : 0;
      onmApp.setModuleValue( "loadRouter.mt.in" , load );

      load = RouterLoad.hitMtOu();
      load = load > 0 ? load : 0;
      onmApp.setModuleValue( "loadRouter.mt.ou" , load );

      load = RouterLoad.hitDr();
      load = load > 0 ? load : 0;
      onmApp.setModuleValue( "loadRouter.dr" , load );

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to refresh system loads , " + e );
    }
  }

  private void refreshSystemQueues() {
    try {

      int percMoQueue = routerMOWorker.sizeMoQueue( true );
      int percMtProcessQueue = routerMTWorker.sizeProcessQueue( true );
      int percDrQueue = routerDRWorker.sizeDrQueue( true );
      int percMoBatchQueue = 0;
      int percMtInBatchQueue = routerMTWorker.sizeInBatchQueue( true );
      int percMtOuQueue = routerMTWorker.sizeOuQueue( true );
      int percDrBatchQueue = 0;

      onmApp.setModuleValue( "queueRouter.mo" , percMoQueue );
      onmApp.setModuleValue( "queueRouter.mt" , percMtProcessQueue );
      onmApp.setModuleValue( "queueRouter.mt.in" , percMtInBatchQueue );
      onmApp.setModuleValue( "queueRouter.mt.ou" , percMtOuQueue );
      onmApp.setModuleValue( "queueRouter.dr" , percDrQueue );

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to refresh system queues , " + e );
    }
  }

  private void refreshSystemBuffers() {
    try {

      // trap delta time

      long deltaTime = System.currentTimeMillis();

      // refresh router buffer info

      routerBufferQueueInfo.refreshBuffers();

      // read params

      int totalSizeMoBuffer = (int) routerBufferQueueInfo
          .getTotalSizeMoBuffer();
      int totalSizeMtBuffer = (int) routerBufferQueueInfo
          .getTotalSizeMtBuffer();
      int totalSizeDrBuffer = (int) routerBufferQueueInfo
          .getTotalSizeDrBuffer();
      int totalNumbersRunningBroadcasts = (int) routerBufferQueueInfo
          .getTotalNumbersRunningBroadcasts();

      // set into onm

      onmApp.setModuleValue( "holderRouter.buffer.MO" , totalSizeMoBuffer );
      onmApp.setModuleValue( "holderRouter.buffer.MT" , totalSizeMtBuffer );
      onmApp.setModuleValue( "holderRouter.buffer.DR" , totalSizeDrBuffer );
      onmApp.setModuleValue( "holderRouter.buffer.BC" ,
          totalNumbersRunningBroadcasts );

      // calculate delta time and log it

      deltaTime = System.currentTimeMillis() - deltaTime;
      if ( routerConf.isDebug() || ( totalSizeMoBuffer > 0 )
          || ( totalSizeMtBuffer > 0 ) || ( totalSizeDrBuffer > 0 )
          || ( totalNumbersRunningBroadcasts > 0 ) ) {
        DLog.debug( lctx , "MO Buffer = " + totalSizeMoBuffer
            + " msg(s) , MT Buffer = " + totalSizeMtBuffer
            + " msg(s) , DR Buffer = " + totalSizeDrBuffer
            + " msg(s) , BC Buffer = " + totalNumbersRunningBroadcasts
            + " number(s) , took " + deltaTime + " ms" );
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to refresh system buffers , " + e );
    }
  }

  private void refreshSystemWorkers() {
    try {

      // is there any broadcast running ?
      boolean isBroadcastRunning = routerBufferQueueInfo
          .getTotalNumbersRunningBroadcasts() > 0;
      // total processing mo messages
      long totalProcessingMoMessages = routerBufferQueueInfo
          .getTotalSizeMoBuffer() + routerBufferQueueInfo.getTotalSizeMoQueue();
      // total processing mt messages
      long totalProcessingMtMessages = routerBufferQueueInfo
          .getTotalNumbersRunningBroadcasts()
          + routerBufferQueueInfo.getTotalSizeMtBuffer()
          + routerBufferQueueInfo.getTotalSizeMtQueue();
      // total dr messages
      long totalProcessingDrMessages = routerBufferQueueInfo
          .getTotalSizeDrBuffer() + routerBufferQueueInfo.getTotalSizeDrQueue();

      // refresh dr worker threads
      if ( routerDRWorker != null ) {
        routerDRWorker.refreshWorkers( totalProcessingDrMessages ,
            totalProcessingMtMessages , isBroadcastRunning );
      }

      // refresh mo worker threads
      if ( routerMOWorker != null ) {
        // nothing to do yet ...
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to refresh system workers , " + e );
    }
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class RouterManagementThread extends Thread {

    private int tempSizeMoQueue;
    private int tempSizeMtProcessQueue;
    private int tempSizeDrQueue;
    private int tempSizeMoBatchQueue;
    private int tempSizeMtInBatchQueue;
    private int tempSizeMtOuQueue;
    private int tempSizeDrBatchQueue;

    private int cleanIdleCtr;
    private int cleanIdleMax;

    public RouterManagementThread() {
      super( "RouterManagementThread" );

      tempSizeMoQueue = 0;
      tempSizeMtProcessQueue = 0;
      tempSizeDrQueue = 0;
      tempSizeMoBatchQueue = 0;
      tempSizeMtInBatchQueue = 0;
      tempSizeMtOuQueue = 0;
      tempSizeDrBatchQueue = 0;

      cleanIdleCtr = 0;
      cleanIdleMax = routerConf.getManagementCleanIdle();
    }

    public void run() {

      int sizeMoQueue = 0;
      int sizeMtProcessQueue = 0;
      int sizeDrQueue = 0;
      int sizeMoBatchQueue = 0;
      int sizeMtInBatchQueue = 0;
      int sizeMtOuQueue = 0;
      int sizeDrBatchQueue = 0;

      long delayCtr = 0 , delay1s = 1000;

      activeRouterManagementThread = true;
      DLog.debug( lctx , "Thread started" );
      while ( activeRouterManagementThread ) {

        try {
          Thread.sleep( delay1s );
        } catch ( InterruptedException e ) {
        }
        delayCtr = delayCtr + delay1s;

        // refresh system load and queue ( make it per 5 second(s) )
        if ( delayCtr % 5000 == 0 ) {
          refreshSystemLoads();
          refreshSystemQueues();
        }

        // execute per sleep millis
        if ( delayCtr < opropsApp.getLong( "Router.Management.sleepMillis" ,
            routerConf.getManagementSleep() ) ) {
          continue;
        }
        delayCtr = 0;

        // refresh system buffer
        refreshSystemBuffers();

        // read total queue inside mt , dr , and mo workers
        if ( routerMTWorker != null ) {
          sizeMtProcessQueue = routerMTWorker.sizeProcessQueue( false );
          sizeMtInBatchQueue = routerMTWorker.sizeInBatchQueue( false );
          sizeMtOuQueue = routerMTWorker.sizeOuQueue( false );
        }
        if ( moQueue != null ) {
          sizeMoQueue = moQueue.size();
        }
        if ( drQueue != null ) {
          sizeDrQueue = drQueue.size();
        }
        if ( moBatchQueue != null ) {
          sizeMoBatchQueue = moBatchQueue.size();
        }
        if ( routerDRWorker != null ) {
          sizeDrBatchQueue = routerDRWorker.sizeDrBatchQueue();
        }

        // when there is new queue size
        if ( ( routerConf.isDebug() ) || ( sizeMoQueue != tempSizeMoQueue )
            || ( sizeMtProcessQueue != tempSizeMtProcessQueue )
            || ( sizeDrQueue != tempSizeDrQueue )
            || ( sizeMoBatchQueue != tempSizeMoBatchQueue )
            || ( sizeMtInBatchQueue != tempSizeMtInBatchQueue )
            || ( sizeMtOuQueue != tempSizeMtOuQueue )
            || ( sizeDrBatchQueue != tempSizeDrBatchQueue ) ) {

          DLog.info( lctx , "MO Queue = " + sizeMoQueue
              + " msg(s) , MT Process Queue = " + sizeMtProcessQueue
              + " msg(s) , DR Queue = " + sizeDrQueue
              + " msg(s) , MO Batch Queue = " + sizeMoBatchQueue
              + " msg(s) , MT In Batch Queue = " + sizeMtInBatchQueue
              + " msg(s) , MT Ou Queue = " + sizeMtOuQueue
              + " msg(s) , DR Batch Queue = " + sizeDrBatchQueue + " msg(s) " );

          int totalSizeMoQueue = sizeMoQueue + sizeMoBatchQueue;
          int totalSizeMtQueue = sizeMtProcessQueue + sizeMtInBatchQueue
              + sizeMtOuQueue;
          int totalSizeDrQueue = sizeDrQueue + sizeDrBatchQueue;

          // refresh system queue
          routerBufferQueueInfo.refreshQueues( totalSizeMoQueue ,
              totalSizeMtQueue , totalSizeDrQueue );

        }

        // refresh all the workers
        refreshSystemWorkers();

        // refresh list providers
        routerListProviderIds.refreshLists();

        // clean queue when found message in the queue and idle
        if ( ( sizeMoQueue > 0 ) || ( sizeMtProcessQueue > 0 )
            || ( sizeDrQueue > 0 ) || ( sizeMoBatchQueue > 0 )
            || ( sizeMtInBatchQueue > 0 ) || ( sizeMtOuQueue > 0 )
            || ( sizeDrBatchQueue > 0 ) ) {
          if ( ( tempSizeMoQueue == sizeMoQueue )
              && ( tempSizeMtProcessQueue == sizeMtProcessQueue )
              && ( tempSizeDrQueue == sizeDrQueue )
              && ( tempSizeMoBatchQueue == sizeMoBatchQueue )
              && ( tempSizeMtInBatchQueue == sizeMtInBatchQueue )
              && ( tempSizeMtOuQueue == sizeMtOuQueue )
              && ( tempSizeDrBatchQueue == sizeDrBatchQueue ) ) {
            cleanIdleCtr = cleanIdleCtr + 1;
            if ( cleanIdleCtr >= cleanIdleMax ) {
              DLog.debug( lctx , "Found idle , perform clean service" );
              // nothing to do yet ...
            }
          } else {
            cleanIdleCtr = 0;
          }
        }

        // keep queue size history
        tempSizeMoQueue = sizeMoQueue;
        tempSizeMtProcessQueue = sizeMtProcessQueue;
        tempSizeDrQueue = sizeDrQueue;
        tempSizeMoBatchQueue = sizeMoBatchQueue;
        tempSizeMtInBatchQueue = sizeMtInBatchQueue;
        tempSizeMtOuQueue = sizeMtOuQueue;
        tempSizeDrBatchQueue = sizeDrBatchQueue;

      } // while ( activeRouterManagementThread )
      DLog.debug( lctx , "Thread stopped" );
    }

  } // class RouterManagementThread

}
