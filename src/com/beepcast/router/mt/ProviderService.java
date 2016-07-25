package com.beepcast.router.mt;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.ProviderApp;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.router.RouterApp;
import com.beepcast.router.RouterConf;
import com.beepcast.router.RouterMessage;
import com.beepcast.router.RouterMessageCommon;
import com.beepcast.router.common.ProviderMessageTransformUtils;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ProviderService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private RouterApp routerApp;
  private RouterConf routerConf;

  private ProviderApp providerApp;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ProviderService( RouterApp routerApp , RouterConf routerConf ) {
    initialized = false;

    // define router app

    if ( routerApp == null ) {
      DLog.warning( lctx , "Failed to initialized provider service "
          + ", failed to define router app" );
      return;
    }
    this.routerApp = routerApp;

    // define router conf

    if ( routerConf == null ) {
      DLog.warning( lctx , "Failed to initialized provider service "
          + ", failed to define router conf" );
      return;
    }
    this.routerConf = routerConf;

    // define provider app

    providerApp = ProviderApp.getInstance();
    if ( providerApp == null ) {
      DLog.warning( lctx , "Failed to initialized provider service "
          + ", failed to define provider app" );
      return;
    }

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean sendMessage( RouterMessage routerMessage ) {
    boolean result = false;

    if ( routerMessage == null ) {
      return result;
    }
    if ( !initialized ) {
      return result;
    }

    // compose header log
    String headerLog = RouterMessageCommon.headerLog( routerMessage );

    try {

      // transform router to provider message
      ProviderMessage providerMessage = ProviderMessageTransformUtils
          .transformRouterToProviderMessage( routerMessage );
      if ( providerMessage == null ) {
        DLog.warning( lctx , headerLog + "Failed to transform message format "
            + "from router message to provider message" );
        return result;
      }

      // persist internal message id
      if ( !persistInternalMessageId( providerMessage ) ) {
        DLog.debug( lctx , headerLog + "Failed to process mt message "
            + ", found failed to resolve internal message id" );
        return result;
      }

      // validate provider id
      if ( StringUtils.isBlank( providerMessage.getProviderId() ) ) {
        DLog.warning( lctx , headerLog + "Failed to process mt message "
            + ", found blank/invalid provider id" );
        return result;
      }

      // send mt message thru provider app
      if ( !providerApp.sendMessage( providerMessage ) ) {
        DLog.warning( lctx , headerLog + "Failed to process mt message "
            + ", found failed to send message thru provider app" );
        return result;
      }

      // result as true
      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to process mt message , " + e );
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean persistInternalMessageId( ProviderMessage providerMessage ) {
    boolean result = false;
    if ( providerMessage == null ) {
      return result;
    }
    String messageId = providerMessage.getInternalMessageId();
    if ( StringUtils.isBlank( messageId ) ) {
      messageId = generateMessageId();
      if ( StringUtils.isBlank( messageId ) ) {
        return result;
      }
      providerMessage.setInternalMessageId( messageId );
    }
    result = true;
    return result;
  }

  private String generateMessageId() {
    String messageId = "";
    if ( routerApp == null ) {
      return messageId;
    }
    messageId = routerApp.generateMsgId( null );
    return messageId;
  }

}
