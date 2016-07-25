package com.beepcast.router;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.dbmanager.DBManagerApp;
import com.beepcast.loadmng.LoadManagement;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.subscriber.SubscriberApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public abstract class RouterWorker implements Module {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterWorker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  protected DatabaseLibrary dbLib;
  protected DBManagerApp dbMan;
  protected LoadManagement loadMng;
  protected OnlinePropertiesApp opropsApp;
  protected SubscriberApp subscriberApp;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterWorker() {

    dbLib = DatabaseLibrary.getInstance();
    dbMan = DBManagerApp.getInstance();
    loadMng = LoadManagement.getInstance();
    opropsApp = OnlinePropertiesApp.getInstance();
    subscriberApp = SubscriberApp.getInstance();

    DLog.debug( lctx , "Initialized : dbLib , dbMan "
        + ", loadMng , onlinePropertiesApp , subscriberApp " );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public abstract void start();

  public abstract void stop();

}
