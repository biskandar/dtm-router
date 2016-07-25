package com.beepcast.router.common;

import com.beepcast.router.RouterMessage;

public class MessageTypeUtils {

  public static String transformGatewayToRouterMessageType(
      int gatewayMessageType ) {
    String routerMessageType = null;

    switch ( gatewayMessageType ) {
    case com.beepcast.model.transaction.MessageType.TEXT_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_SMS;
      break;
    case com.beepcast.model.transaction.MessageType.RINGTONE_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_SMS;
      break;
    case com.beepcast.model.transaction.MessageType.PICTURE_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_SMS;
      break;
    case com.beepcast.model.transaction.MessageType.UNICODE_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_SMS;
      break;
    case com.beepcast.model.transaction.MessageType.WAPPUSH_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_SMS;
      break;
    case com.beepcast.model.transaction.MessageType.MMS_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_MMS;
      break;
    case com.beepcast.model.transaction.MessageType.QRPNG_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_QR;
      break;
    case com.beepcast.model.transaction.MessageType.QRGIF_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_QR;
      break;
    case com.beepcast.model.transaction.MessageType.QRJPG_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_QR;
      break;
    case com.beepcast.model.transaction.MessageType.WEBHOOK_TYPE :
      routerMessageType = RouterMessage.MSGTYPE_WEBHOOK;
      break;
    }

    return routerMessageType;
  }

  public static int transformGatewayMessageTypeToRouterContentType(
      int gatewayMessageType ) {
    int routerContentType = 0;

    switch ( gatewayMessageType ) {
    case com.beepcast.model.transaction.MessageType.TEXT_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_SMSTEXT;
      break;
    case com.beepcast.model.transaction.MessageType.RINGTONE_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_SMSBINARY;
      break;
    case com.beepcast.model.transaction.MessageType.PICTURE_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_SMSBINARY;
      break;
    case com.beepcast.model.transaction.MessageType.UNICODE_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_SMSUNICODE;
      break;
    case com.beepcast.model.transaction.MessageType.WAPPUSH_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_SMSBINARY;
      break;
    case com.beepcast.model.transaction.MessageType.MMS_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_MMS;
      break;
    case com.beepcast.model.transaction.MessageType.QRPNG_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_QRPNG;
      break;
    case com.beepcast.model.transaction.MessageType.QRGIF_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_QRGIF;
      break;
    case com.beepcast.model.transaction.MessageType.QRJPG_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_QRJPG;
      break;
    case com.beepcast.model.transaction.MessageType.WEBHOOK_TYPE :
      routerContentType = RouterMessage.CONTENTTYPE_WEBHOOK;
      break;
    }

    return routerContentType;
  }

  public static int transformRouterContentTypeToGatewayMessageType(
      int routerContentType ) {
    int gatewayMessageType = 0;

    switch ( routerContentType ) {
    case RouterMessage.CONTENTTYPE_SMSTEXT :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.TEXT_TYPE;
      break;
    case RouterMessage.CONTENTTYPE_SMSUNICODE :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.UNICODE_TYPE;
      break;
    case RouterMessage.CONTENTTYPE_SMSBINARY :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.PICTURE_TYPE;
      break;
    case RouterMessage.CONTENTTYPE_MMS :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.MMS_TYPE;
      break;
    case RouterMessage.CONTENTTYPE_QRPNG :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.QRPNG_TYPE;
      break;
    case RouterMessage.CONTENTTYPE_QRGIF :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.QRGIF_TYPE;
      break;
    case RouterMessage.CONTENTTYPE_QRJPG :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.QRJPG_TYPE;
      break;
    case RouterMessage.CONTENTTYPE_WEBHOOK :
      gatewayMessageType = com.beepcast.model.transaction.MessageType.WEBHOOK_TYPE;
      break;
    }

    return gatewayMessageType;
  }

  public static int transformProviderToRouterContentType(
      int providerContentType ) {
    int routerContentType = 0;
    switch ( providerContentType ) {
    case com.beepcast.api.provider.data.ContentType.SMSTEXT :
      routerContentType = RouterMessage.CONTENTTYPE_SMSTEXT;
      break;
    case com.beepcast.api.provider.data.ContentType.SMSBINARY :
      routerContentType = RouterMessage.CONTENTTYPE_SMSBINARY;
      break;
    case com.beepcast.api.provider.data.ContentType.SMSUNICODE :
      routerContentType = RouterMessage.CONTENTTYPE_SMSUNICODE;
      break;
    case com.beepcast.api.provider.data.ContentType.MMS :
      routerContentType = RouterMessage.CONTENTTYPE_MMS;
      break;
    case com.beepcast.api.provider.data.ContentType.WAPPUSH :
      routerContentType = RouterMessage.CONTENTTYPE_WAPPUSH;
      break;
    case com.beepcast.api.provider.data.ContentType.QRPNG :
      routerContentType = RouterMessage.CONTENTTYPE_QRPNG;
      break;
    case com.beepcast.api.provider.data.ContentType.QRGIF :
      routerContentType = RouterMessage.CONTENTTYPE_QRGIF;
      break;
    case com.beepcast.api.provider.data.ContentType.QRJPG :
      routerContentType = RouterMessage.CONTENTTYPE_QRJPG;
      break;
    case com.beepcast.api.provider.data.ContentType.WEBHOOK :
      routerContentType = RouterMessage.CONTENTTYPE_WEBHOOK;
      break;
    }
    return routerContentType;
  }

  public static int transformSendBufferToGatewayMessageType(
      int sendBufferMessageType ) {
    return sendBufferMessageType;
  }

  public static int transformGatewayToSendBufferMessageType(
      int gatewayMessageType ) {
    return gatewayMessageType;
  }

  public static String transformRouterContentTypeToProviderMessageType(
      int routerContentType ) {
    String providerMessageType = null;

    switch ( routerContentType ) {
    case RouterMessage.CONTENTTYPE_SMSTEXT :
      providerMessageType = com.beepcast.api.provider.data.MessageType.SMSMT;
      break;
    case RouterMessage.CONTENTTYPE_SMSBINARY :
      providerMessageType = com.beepcast.api.provider.data.MessageType.SMSMT;
      break;
    case RouterMessage.CONTENTTYPE_SMSUNICODE :
      providerMessageType = com.beepcast.api.provider.data.MessageType.SMSMT;
      break;
    case RouterMessage.CONTENTTYPE_WAPPUSH :
      providerMessageType = com.beepcast.api.provider.data.MessageType.SMSMT;
      break;
    case RouterMessage.CONTENTTYPE_MMS :
      providerMessageType = com.beepcast.api.provider.data.MessageType.MMSMT;
      break;
    case RouterMessage.CONTENTTYPE_QRPNG :
      providerMessageType = com.beepcast.api.provider.data.MessageType.QRMT;
      break;
    case RouterMessage.CONTENTTYPE_QRGIF :
      providerMessageType = com.beepcast.api.provider.data.MessageType.QRMT;
      break;
    case RouterMessage.CONTENTTYPE_QRJPG :
      providerMessageType = com.beepcast.api.provider.data.MessageType.QRMT;
      break;
    case RouterMessage.CONTENTTYPE_WEBHOOK :
      providerMessageType = com.beepcast.api.provider.data.MessageType.WEBHOOKMT;
      break;
    }

    return providerMessageType;
  }

}
