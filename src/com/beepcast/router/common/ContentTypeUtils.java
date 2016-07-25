package com.beepcast.router.common;

import com.beepcast.router.RouterMessage;

public class ContentTypeUtils {

  public static int transformRouterContentTypeToProviderContentType(
      int routerMessageContentType ) {
    int providerMessageContentType = -1;

    switch ( routerMessageContentType ) {
    case RouterMessage.CONTENTTYPE_SMSTEXT :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.SMSTEXT;
      break;
    case RouterMessage.CONTENTTYPE_SMSBINARY :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.SMSBINARY;
      break;
    case RouterMessage.CONTENTTYPE_SMSUNICODE :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.SMSUNICODE;
      break;
    case RouterMessage.CONTENTTYPE_MMS :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.MMS;
      break;
    case RouterMessage.CONTENTTYPE_WAPPUSH :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.WAPPUSH;
      break;
    case RouterMessage.CONTENTTYPE_QRPNG :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.QRPNG;
      break;
    case RouterMessage.CONTENTTYPE_QRGIF :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.QRGIF;
      break;
    case RouterMessage.CONTENTTYPE_QRJPG :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.QRJPG;
      break;
    case RouterMessage.CONTENTTYPE_WEBHOOK :
      providerMessageContentType = com.beepcast.api.provider.data.ContentType.WEBHOOK;
      break;
    }

    return providerMessageContentType;
  }

}
