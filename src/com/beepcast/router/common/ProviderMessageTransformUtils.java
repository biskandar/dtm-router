package com.beepcast.router.common;

import java.util.Date;
import java.util.Map;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageFactory;
import com.beepcast.router.RouterConf;
import com.beepcast.router.RouterMessage;
import com.beepcast.router.RouterMessageCommon;
import com.beepcast.router.RouterMessageFactory;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProviderMessageTransformUtils {

  static final DLogContext lctx = new SimpleContext(
      "ProviderMessageTransformUtils" );

  public static RouterMessage transformProviderToRouterMTMessage(
      ProviderMessage providerMessage ) {
    RouterMessage routerMTMessage = null;

    // validate must be parameters

    if ( providerMessage == null ) {
      return routerMTMessage;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( providerMessage
        .getInternalMessageId() );

    // prepare parameters for router message

    String messageId = providerMessage.getInternalMessageId();
    int gatewayMessageType = MessageTypeUtils
        .transformProviderToRouterContentType( providerMessage.getContentType() );
    int messageCount = providerMessage.getMessageCount();
    String messageContent = providerMessage.getMessage();
    double debitAmount = providerMessage.getDebitAmount();
    String phoneNumber = providerMessage.getDestinationAddress();
    String providerId = providerMessage.getProviderId();
    int eventId = 0;
    int channelSessionId = 0;
    String senderId = providerMessage.getOriginAddrMask();
    int priority = providerMessage.getPriority();
    int retry = providerMessage.getRetry();
    Date dateSend = providerMessage.getSubmitDateTime();
    Map internalMapParams = providerMessage.getOptionalParams();
    Map externalMapParams = null;

    // verify parameters

    try {
      eventId = Integer.parseInt( providerMessage.getEventId() );
    } catch ( NumberFormatException e ) {
    }
    try {
      channelSessionId = Integer.parseInt( providerMessage
          .getChannelSessionId() );
    } catch ( NumberFormatException e ) {
    }

    // log it

    DLog.debug( lctx , headerLog + "Transforming provider message "
        + "to router mt message : gatewayMessageType = " + gatewayMessageType
        + " , messageCount = " + messageCount + " , debitAmount = "
        + debitAmount + " , providerId = " + providerId + " , eventId = "
        + eventId + " , channelSessionId = " + channelSessionId
        + " , priority = " + priority + " , retry = " + retry
        + " , dateSend = " + dateSend + " , internalMapParams = "
        + internalMapParams );

    // construct router message

    routerMTMessage = RouterMessageFactory.createRouterMTMessage( messageId ,
        gatewayMessageType , messageCount , messageContent , debitAmount ,
        phoneNumber , providerId , eventId , channelSessionId , senderId ,
        priority , retry , dateSend , internalMapParams , externalMapParams );

    return routerMTMessage;
  }

  public static ProviderMessage transformRouterToProviderMessage(
      RouterMessage routerMessage ) {
    ProviderMessage providerMessage = null;

    // validate must be parameters

    if ( routerMessage == null ) {
      return providerMessage;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( routerMessage );

    // prepare parameters for provider message

    String eventId = Integer.toString( routerMessage.getEventId() );
    String channelSessionId = Integer.toString( routerMessage
        .getChannelSessionId() );
    String providerId = routerMessage.getProviderId();
    String providerMessageType = MessageTypeUtils
        .transformRouterContentTypeToProviderMessageType( routerMessage
            .getContentType() );
    int contentType = ContentTypeUtils
        .transformRouterContentTypeToProviderContentType( routerMessage
            .getContentType() );
    String internalMessageId = routerMessage.getInternalMessageId();
    String destinationAddress = routerMessage.getDestinationAddress();
    String originAddress = null;
    String originAddrMask = routerMessage.getOriginAddrMask();
    String message = routerMessage.getMessage();
    int messageCount = routerMessage.getMessageCount();
    double debitAmount = routerMessage.getDebitAmount();
    int priority = RouterConf.DEFAULT_MESSAGE_PRIORITY;
    int retry = routerMessage.getRetry();
    String description = null;

    // log it

    DLog.debug( lctx , headerLog + "Transforming router mt message "
        + "to provider message : eventId = " + eventId
        + " , channelSessionId = " + channelSessionId + " , providerId = "
        + providerId + " , providerMessageType = " + providerMessageType
        + " , contentType = " + contentType + " , messageCount = "
        + messageCount + " , debitAmount = " + debitAmount + " , priority = "
        + priority + " , retry = " + retry + " , optionalParams = "
        + routerMessage.getInternalMapParams() );

    // construct provider message

    providerMessage = ProviderMessageFactory.createProviderMessage( eventId ,
        channelSessionId , providerId , providerMessageType , contentType ,
        internalMessageId , destinationAddress , originAddress ,
        originAddrMask , message , messageCount , debitAmount , priority ,
        retry , description );

    // copy from router internal params to provider optional params .

    Map routerInternalParams = routerMessage.getInternalMapParams();
    if ( routerInternalParams != null ) {
      Map providerOptionalParams = providerMessage.getOptionalParams();
      if ( providerOptionalParams != null ) {
        providerOptionalParams.putAll( routerInternalParams );
      }
    }

    return providerMessage;
  }

}
