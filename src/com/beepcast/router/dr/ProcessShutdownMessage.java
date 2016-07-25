package com.beepcast.router.dr;

import java.util.ArrayList;
import java.util.List;

import com.beepcast.api.provider.common.ProviderStatusAlert;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.dbmanager.common.ProviderCommon;
import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.RouterApp;
import com.beepcast.router.RouterMessageCommon;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProcessShutdownMessage {

  static final DLogContext lctx = new SimpleContext( "ProcessShutdownMessage" );

  static final OnlinePropertiesApp opropsApp = OnlinePropertiesApp
      .getInstance();
  static final RouterApp routerApp = RouterApp.getInstance();

  public static boolean execute( ProviderMessage providerMessage ) {
    boolean result = false;

    // validate must be params
    if ( providerMessage == null ) {
      return result;
    }

    // header log
    String headerLog = RouterMessageCommon.headerLog( providerMessage
        .getInternalMessageId() );

    // log first
    DLog.debug( lctx , headerLog + "Found shutdown flag inside dr message "
        + ", will shutdown the provider [" + providerMessage.getProviderId()
        + "]" );

    // select active provider
    TProvider providerOut = ProviderCommon.getProvider( providerMessage
        .getProviderId() );
    if ( providerOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to shutdown provider "
          + ", found invalid provider id = " + providerMessage.getProviderId() );
      return result;
    }

    // set suspended provider if found not suspended
    String fieldName = "ProviderAgent.Suspend.".concat( providerOut
        .getProviderId() );
    if ( !opropsApp.getBoolean( fieldName , false ) ) {

      if ( !opropsApp.setBoolean( fieldName , true ) ) {
        DLog.warning( lctx ,
            headerLog + "Failed to shutdown provider "
                + ", found failed to update suspend provider agent "
                + providerOut.getProviderId() );
        return result;
      }

      // refresh list provider id with additional suspended provider id
      {
        List listAdditionalSuspendedProviderIds = new ArrayList();
        listAdditionalSuspendedProviderIds
            .add( providerMessage.getProviderId() );
        routerApp.getListProviderIds().refreshLists(
            listAdditionalSuspendedProviderIds , null );
        DLog.debug( lctx , headerLog
            + "Refreshed new list active provider ids : "
            + routerApp.getListProviderIds()
                .listActiveWorkerProviderIdsWithFilterSuspend() );
      }

      // send an alert provider suspended message
      String reason = "extMsgId = "
          + providerMessage.getExternalMessageId()
          + ", extStatusCode = "
          + providerMessage.getExternalStatus()
          + ", extStatusDate = "
          + DateTimeFormat
              .convertToString( providerMessage.getStatusDateTime() );
      ProviderStatusAlert.sendAlertProviderStatusSuspended(
          providerMessage.getProviderId() , null , reason );
      DLog.debug( lctx , headerLog + "Sent an alert provider "
          + "suspended message , with : reason = " + reason );

    }

    // log it
    DLog.debug( lctx , headerLog + "Successfully shutdown the provider ["
        + providerMessage.getProviderId() + "]" );

    result = true;
    return result;
  }
}
