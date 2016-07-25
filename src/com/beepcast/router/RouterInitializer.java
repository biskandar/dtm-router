package com.beepcast.router;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.beepcast.onm.OnmApp;
import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterInitializer implements ServletContextListener {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static final String PROPERTY_FILE_ROUTER = "router.config.file";

  static final DLogContext lctx = new SimpleContext( "RouterInitializer" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void contextInitialized( ServletContextEvent sce ) {

    ServletContext context = sce.getServletContext();
    String logStr = "";

    GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

    RouterConf routerConf = RouterConfFactory
        .generateRouterConf( PROPERTY_FILE_ROUTER );
    logStr = this.getClass() + " : initialized " + routerConf;
    context.log( logStr );
    System.out.println( logStr );
    DLog.debug( lctx , logStr );

    RouterApp routerApp = RouterApp.getInstance();
    routerApp.init( routerConf );
    routerApp.start();
    logStr = this.getClass() + " : initialized " + routerApp;
    context.log( logStr );
    System.out.println( logStr );
    DLog.debug( lctx , logStr );

    DLog.debug( lctx , "Linking routerApp -> onmApp" );
    OnmApp onmApp = OnmApp.getInstance();
    onmApp.setRouterApp( routerApp );

  }

  public void contextDestroyed( ServletContextEvent sce ) {

    ServletContext context = sce.getServletContext();
    String logStr = "";

    GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

    RouterApp routerApp = RouterApp.getInstance();
    routerApp.stop();
    logStr = this.getClass() + " : destroyed ";
    context.log( logStr );
    System.out.println( logStr );
    DLog.debug( lctx , logStr );

  }

}
