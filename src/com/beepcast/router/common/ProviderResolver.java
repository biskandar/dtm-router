package com.beepcast.router.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.model.gateway.GatewayLogBean;
import com.beepcast.model.transaction.TransactionOutputMessage;
import com.beepcast.model.transaction.provider.DestinationProviderSimulation;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.RouterDRWorker;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderResolver {

  static final DLogContext lctx = new SimpleContext( "ProviderResolver" );

  static final OnlinePropertiesApp opropsApp = OnlinePropertiesApp
      .getInstance();

  public static String resolveNewProvider( String headerLog ,
      GatewayLogBean gatewayLogBean , boolean switchProvider ) {
    String resolvedProviderId = null;

    // must be params

    if ( gatewayLogBean == null ) {
      return resolvedProviderId;
    }

    // resolve current and exclude provider id

    String currentProviderId = gatewayLogBean.getProvider();
    String excludeProviderId = switchProvider ? currentProviderId : null;

    // resolve new provider id

    resolvedProviderId = resolveNewProvider( headerLog ,
        gatewayLogBean.getPhone() , (int) gatewayLogBean.getEventID() ,
        currentProviderId , excludeProviderId );

    return resolvedProviderId;
  }

  public static String resolveNewProvider( String headerLog ,
      String phoneNumber , int eventId , String currentProviderId ,
      String excludeProviderId ) {
    String resolvedProviderId = null;

    // capture delta time

    long deltaTime = System.currentTimeMillis();

    // log it first

    DLog.debug( lctx , headerLog + "Resolving new provider "
        + ", with : phoneNumber = " + phoneNumber + " , eventId = " + eventId
        + " , currentProviderId = " + currentProviderId
        + " , excludeProviderId= " + excludeProviderId );

    // first , resolve new provider with destination provider simulation

    if ( StringUtils.isBlank( resolvedProviderId ) ) {
      DLog.debug( lctx , headerLog + "Resolving new provider based on "
          + "destination provider simulation" );
      List listExcludeProviderIds = new ArrayList();
      if ( !StringUtils.isBlank( excludeProviderId ) ) {
        listExcludeProviderIds.add( excludeProviderId );
      }
      TransactionOutputMessage omsg = DestinationProviderSimulation.execute(
          eventId , phoneNumber , listExcludeProviderIds );
      if ( omsg != null ) {
        resolvedProviderId = omsg.getDestinationProvider();
      }
    }

    // third , resolved based on global online properties : switchProvider

    if ( StringUtils.isBlank( resolvedProviderId ) ) {
      DLog.debug( lctx , headerLog + "Resolving new provider based on "
          + "global online properties : switchProvider property" );
      resolvedProviderId = opropsApp.getMapString(
          RouterDRWorker.OPROPS_RETRYSWITCHPROVIDER , currentProviderId , null );
    }

    // forth , resolved based on old provider

    if ( StringUtils.isBlank( resolvedProviderId ) ) {
      DLog.debug( lctx , headerLog + "Resolving new provider based on "
          + "old provider" );
      resolvedProviderId = currentProviderId;
    }

    // calculate delta time

    deltaTime = System.currentTimeMillis() - deltaTime;

    // log it

    DLog.debug( lctx , headerLog + "Performed switch provider id : "
        + currentProviderId + " -> " + resolvedProviderId + " , take = "
        + deltaTime + " ms" );

    return resolvedProviderId;
  }

}
