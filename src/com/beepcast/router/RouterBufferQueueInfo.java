package com.beepcast.router;

import java.util.Iterator;
import java.util.List;

import com.beepcast.channel.ChannelApp;
import com.beepcast.channel.view.ChannelSessionInfoBean;
import com.beepcast.model.client.ClientType;
import com.beepcast.router.dr.DrBufferService;
import com.beepcast.router.mo.MoBufferService;
import com.beepcast.router.mt.SendBufferService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterBufferQueueInfo {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterBufferQueueInfo" );

  static final Object objectLock = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private RouterListProviderIds routerListProviderIds;

  private long totalSizeMoBuffer;
  private long totalSizeMtBuffer;
  private long totalSizeDrBuffer;

  private int totalSizeMoQueue;
  private int totalSizeMtQueue;
  private int totalSizeDrQueue;

  private long totalNumbersRunningBroadcasts;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterBufferQueueInfo( RouterListProviderIds routerListProviderIds ) {

    this.routerListProviderIds = routerListProviderIds;

    totalSizeMoBuffer = 0;
    totalSizeMtBuffer = 0;
    totalSizeDrBuffer = 0;

    totalSizeMoQueue = 0;
    totalSizeMtQueue = 0;
    totalSizeDrQueue = 0;

    totalNumbersRunningBroadcasts = 0;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void refreshBuffers() {
    synchronized ( objectLock ) {
      try {

        // list current active providers

        List listActiveProviderIds = routerListProviderIds
            .listActiveWorkerProviderIdsWithFilterSuspend();

        // update system mo buffer

        MoBufferService moBufferService = new MoBufferService();
        totalSizeMoBuffer = moBufferService.totalRecords();

        // update system mt buffer , based on active providers

        SendBufferService mtBufferService = new SendBufferService();
        totalSizeMtBuffer = mtBufferService
            .totalRecordsByListProviderIdsDateSendNow( listActiveProviderIds );

        // update system dr buffer

        DrBufferService drBufferService = new DrBufferService();
        totalSizeDrBuffer = drBufferService.totalRecords();

        // update total numbers of running broadcasts

        totalNumbersRunningBroadcasts = readTotalNumbersOfRunningBroadcasts();

      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to refresh buffers , " + e );
      }
    }
  }

  public void refreshQueues( int totalSizeMoQueue , int totalSizeMtQueue ,
      int totalSizeDrQueue ) {
    synchronized ( objectLock ) {
      try {

        // update system queues

        this.totalSizeMoQueue = totalSizeMoQueue;
        this.totalSizeMtQueue = totalSizeMtQueue;
        this.totalSizeDrQueue = totalSizeDrQueue;

      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to refresh queues , " + e );
      }
    }
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long getTotalSizeMoBuffer() {
    return totalSizeMoBuffer;
  }

  public long getTotalSizeMtBuffer() {
    return totalSizeMtBuffer;
  }

  public long getTotalSizeDrBuffer() {
    return totalSizeDrBuffer;
  }

  public int getTotalSizeMoQueue() {
    return totalSizeMoQueue;
  }

  public int getTotalSizeMtQueue() {
    return totalSizeMtQueue;
  }

  public int getTotalSizeDrQueue() {
    return totalSizeDrQueue;
  }

  public long getTotalNumbersRunningBroadcasts() {
    return totalNumbersRunningBroadcasts;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private long readTotalNumbersOfRunningBroadcasts() {
    long totalNumbers = 0;
    try {

      ChannelApp channelApp = ChannelApp.getInstance();
      List listRunningBroadcasts = channelApp.listRunningBroadcastsFromMaster(
          ClientType.SUPER , 0 );
      if ( ( listRunningBroadcasts == null )
          || ( listRunningBroadcasts.size() < 1 ) ) {
        return totalNumbers;
      }

      ChannelSessionInfoBean csiBean = null;
      Iterator iterRunningBroadcasts = listRunningBroadcasts.iterator();
      while ( iterRunningBroadcasts.hasNext() ) {
        csiBean = (ChannelSessionInfoBean) iterRunningBroadcasts.next();
        if ( csiBean == null ) {
          continue;
        }
        if ( csiBean.isUseDateSend() ) {
          continue;
        }
        totalNumbers = totalNumbers + csiBean.getTotalRemains();
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to read total numbers "
          + "of running broadcast , " + e );
    }
    return totalNumbers;
  }

}
