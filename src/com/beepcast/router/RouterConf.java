package com.beepcast.router;

import java.util.HashMap;
import java.util.Map;

public class RouterConf {

  public static final int DEFAULT_MESSAGE_PRIORITY = 0;

  private boolean debug;

  private int msgExpiry;
  private int msgPriority;

  private int retryTries;
  private int retryDuration;
  private Map retryMap;

  private int drWorker;
  private int drQueueSize;
  private int drMinSleep , drMaxSleep;
  private int drMinDBSleep , drMaxDBSleep;
  private int drLimitRecord;
  private int drBurstSize , drMaxLoad;

  private int mtWorker;
  private int mtQueueSize;
  private int mtMinSleep , mtMaxSleep;
  private int mtMinDBSleep , mtMaxDBSleep;
  private int mtLimitRecord;
  private int mtBurstSize , mtMaxLoad;

  private int moWorker;
  private int moQueueSize;
  private int moMinSleep , moMaxSleep;
  private int moMinDBSleep , moMaxDBSleep;
  private int moLimitRecord;
  private int moBurstSize , moMaxLoad;

  private int managementSleep;
  private int managementCleanIdle;

  public RouterConf() {

    debug = false;

    msgExpiry = 6 * 60 * 60 * 1000; // a half day(s)
    msgPriority = DEFAULT_MESSAGE_PRIORITY;

    retryTries = 0;
    retryDuration = 15 * 60 * 1000; // 15 minutes
    retryMap = new HashMap();

    drWorker = 2;
    drQueueSize = 2000;
    drMinSleep = 5000;
    drMaxSleep = 10000;
    drMinDBSleep = 5000;
    drMaxDBSleep = 10000;
    drLimitRecord = 10;
    drBurstSize = 2;
    drMaxLoad = 10;

    mtWorker = 2;
    mtQueueSize = 2000;
    mtMinSleep = 5000;
    mtMaxSleep = 10000;
    mtMinDBSleep = 5000;
    mtMaxDBSleep = 10000;
    mtLimitRecord = 10;
    mtBurstSize = 2;
    mtMaxLoad = 10;

    moWorker = 2;
    moQueueSize = 2000;
    moMinSleep = 5000;
    moMaxSleep = 10000;
    moMinDBSleep = 5000;
    moMaxDBSleep = 10000;
    moLimitRecord = 10;
    moBurstSize = 2;
    moMaxLoad = 10;

    managementSleep = 30000;
    managementCleanIdle = 3;

  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug( boolean debug ) {
    this.debug = debug;
  }

  public int getMsgExpiry() {
    return msgExpiry;
  }

  public void setMsgExpiry( int msgExpiry ) {
    this.msgExpiry = msgExpiry;
  }

  public int getMsgPriority() {
    return msgPriority;
  }

  public void setMsgPriority( int msgPriority ) {
    this.msgPriority = msgPriority;
  }

  public int getRetryTries() {
    return retryTries;
  }

  public void setRetryTries( int retryTries ) {
    this.retryTries = retryTries;
  }

  public int getRetryDuration() {
    return retryDuration;
  }

  public void setRetryDuration( int retryDuration ) {
    this.retryDuration = retryDuration;
  }

  public Map getRetryMap() {
    return retryMap;
  }

  public int getDrWorker() {
    return drWorker;
  }

  public void setDrWorker( int drWorker ) {
    this.drWorker = drWorker;
  }

  public int getDrQueueSize() {
    return drQueueSize;
  }

  public void setDrQueueSize( int drQueueSize ) {
    this.drQueueSize = drQueueSize;
  }

  public int getDrMinSleep() {
    return drMinSleep;
  }

  public void setDrMinSleep( int drMinSleep ) {
    this.drMinSleep = drMinSleep;
  }

  public int getDrMaxSleep() {
    return drMaxSleep;
  }

  public void setDrMaxSleep( int drMaxSleep ) {
    this.drMaxSleep = drMaxSleep;
  }

  public int getDrMinDBSleep() {
    return drMinDBSleep;
  }

  public void setDrMinDBSleep( int drMinDBSleep ) {
    this.drMinDBSleep = drMinDBSleep;
  }

  public int getDrMaxDBSleep() {
    return drMaxDBSleep;
  }

  public void setDrMaxDBSleep( int drMaxDBSleep ) {
    this.drMaxDBSleep = drMaxDBSleep;
  }

  public int getDrLimitRecord() {
    return drLimitRecord;
  }

  public void setDrLimitRecord( int drLimitRecord ) {
    this.drLimitRecord = drLimitRecord;
  }

  public int getDrBurstSize() {
    return drBurstSize;
  }

  public void setDrBurstSize( int drBurstSize ) {
    this.drBurstSize = drBurstSize;
  }

  public int getDrMaxLoad() {
    return drMaxLoad;
  }

  public void setDrMaxLoad( int drMaxLoad ) {
    this.drMaxLoad = drMaxLoad;
  }

  public int getMtWorker() {
    return mtWorker;
  }

  public void setMtWorker( int mtWorker ) {
    this.mtWorker = mtWorker;
  }

  public int getMtQueueSize() {
    return mtQueueSize;
  }

  public void setMtQueueSize( int mtQueueSize ) {
    this.mtQueueSize = mtQueueSize;
  }

  public int getMtMinSleep() {
    return mtMinSleep;
  }

  public void setMtMinSleep( int mtMinSleep ) {
    this.mtMinSleep = mtMinSleep;
  }

  public int getMtMaxSleep() {
    return mtMaxSleep;
  }

  public void setMtMaxSleep( int mtMaxSleep ) {
    this.mtMaxSleep = mtMaxSleep;
  }

  public int getMtMinDBSleep() {
    return mtMinDBSleep;
  }

  public void setMtMinDBSleep( int mtMinDBSleep ) {
    this.mtMinDBSleep = mtMinDBSleep;
  }

  public int getMtMaxDBSleep() {
    return mtMaxDBSleep;
  }

  public void setMtMaxDBSleep( int mtMaxDBSleep ) {
    this.mtMaxDBSleep = mtMaxDBSleep;
  }

  public int getMtLimitRecord() {
    return mtLimitRecord;
  }

  public void setMtLimitRecord( int mtLimitRecord ) {
    this.mtLimitRecord = mtLimitRecord;
  }

  public int getMtBurstSize() {
    return mtBurstSize;
  }

  public void setMtBurstSize( int mtBurstSize ) {
    this.mtBurstSize = mtBurstSize;
  }

  public int getMtMaxLoad() {
    return mtMaxLoad;
  }

  public void setMtMaxLoad( int mtMaxLoad ) {
    this.mtMaxLoad = mtMaxLoad;
  }

  public int getMoWorker() {
    return moWorker;
  }

  public void setMoWorker( int moWorker ) {
    this.moWorker = moWorker;
  }

  public int getMoQueueSize() {
    return moQueueSize;
  }

  public void setMoQueueSize( int moQueueSize ) {
    this.moQueueSize = moQueueSize;
  }

  public int getMoMinSleep() {
    return moMinSleep;
  }

  public void setMoMinSleep( int moMinSleep ) {
    this.moMinSleep = moMinSleep;
  }

  public int getMoMaxSleep() {
    return moMaxSleep;
  }

  public void setMoMaxSleep( int moMaxSleep ) {
    this.moMaxSleep = moMaxSleep;
  }

  public int getMoMinDBSleep() {
    return moMinDBSleep;
  }

  public void setMoMinDBSleep( int moMinDBSleep ) {
    this.moMinDBSleep = moMinDBSleep;
  }

  public int getMoMaxDBSleep() {
    return moMaxDBSleep;
  }

  public void setMoMaxDBSleep( int moMaxDBSleep ) {
    this.moMaxDBSleep = moMaxDBSleep;
  }

  public int getMoLimitRecord() {
    return moLimitRecord;
  }

  public void setMoLimitRecord( int moLimitRecord ) {
    this.moLimitRecord = moLimitRecord;
  }

  public int getMoBurstSize() {
    return moBurstSize;
  }

  public void setMoBurstSize( int moBurstSize ) {
    this.moBurstSize = moBurstSize;
  }

  public int getMoMaxLoad() {
    return moMaxLoad;
  }

  public void setMoMaxLoad( int moMaxLoad ) {
    this.moMaxLoad = moMaxLoad;
  }

  public int getManagementSleep() {
    return managementSleep;
  }

  public void setManagementSleep( int managementSleep ) {
    this.managementSleep = managementSleep;
  }

  public int getManagementCleanIdle() {
    return managementCleanIdle;
  }

  public void setManagementCleanIdle( int managementCleanIdle ) {
    this.managementCleanIdle = managementCleanIdle;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "RouterConf ( " + "debug = " + this.debug + TAB + "msgExpiry = "
        + this.msgExpiry + TAB + "msgPriority = " + this.msgPriority + TAB
        + "retryTries = " + this.retryTries + TAB + "retryDuration = "
        + this.retryDuration + TAB + "retryMap = " + this.retryMap + TAB
        + "drWorker = " + this.drWorker + TAB + "drQueueSize = "
        + this.drQueueSize + TAB + "drMinSleep = " + this.drMinSleep + TAB
        + "drMaxSleep = " + this.drMaxSleep + TAB + "drMinDBSleep = "
        + this.drMinDBSleep + TAB + "drMaxDBSleep = " + this.drMaxDBSleep + TAB
        + "drLimitRecord = " + this.drLimitRecord + TAB + "drBurstSize = "
        + this.drBurstSize + TAB + "drMaxLoad = " + this.drMaxLoad + TAB
        + "mtWorker = " + this.mtWorker + TAB + "mtQueueSize = "
        + this.mtQueueSize + TAB + "mtMinSleep = " + this.mtMinSleep + TAB
        + "mtMaxSleep = " + this.mtMaxSleep + TAB + "mtMinDBSleep = "
        + this.mtMinDBSleep + TAB + "mtMaxDBSleep = " + this.mtMaxDBSleep + TAB
        + "mtLimitRecord = " + this.mtLimitRecord + TAB + "mtBurstSize = "
        + this.mtBurstSize + TAB + "mtMaxLoad = " + this.mtMaxLoad + TAB
        + "moWorker = " + this.moWorker + TAB + "moQueueSize = "
        + this.moQueueSize + TAB + "moMinSleep = " + this.moMinSleep + TAB
        + "moMaxSleep = " + this.moMaxSleep + TAB + "moMinDBSleep = "
        + this.moMinDBSleep + TAB + "moMaxDBSleep = " + this.moMaxDBSleep + TAB
        + "moLimitRecord = " + this.moLimitRecord + TAB + "moBurstSize = "
        + this.moBurstSize + TAB + "moMaxLoad = " + this.moMaxLoad + TAB
        + "managementSleep = " + this.managementSleep + TAB
        + "managementCleanIdle = " + this.managementCleanIdle + TAB + " )";
    return retValue;
  }

}
