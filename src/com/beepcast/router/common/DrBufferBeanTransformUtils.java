package com.beepcast.router.common;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.api.provider.data.ProviderMessageFactory;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.router.RouterMessageCommon;
import com.beepcast.router.dr.DrBufferBean;
import com.beepcast.router.dr.DrBufferBeanFactory;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DrBufferBeanTransformUtils {

  static final DLogContext lctx = new SimpleContext(
      "DrBufferBeanTransformUtils" );

  public static DrBufferBean transformProviderMessageToDrBufferBean(
      ProviderMessage providerMessage ) {
    DrBufferBean drBufferBean = null;

    // validate must be parameters

    if ( providerMessage == null ) {
      return drBufferBean;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( providerMessage
        .getExternalMessageId() );

    // prepare parameters for dr buffer bean

    String providerId = providerMessage.getProviderId();
    String messageId = providerMessage.getExternalMessageId();
    String externalStatus = providerMessage.getExternalStatus();
    Date externalStatusDate = providerMessage.getStatusDateTime();
    String internalStatus = providerMessage.getInternalStatus();
    int priority = providerMessage.getPriority();
    String description = providerMessage.getDescription();
    Map internalMapParams = providerMessage.getOptionalParams();
    String externalParams = providerMessage.getExternalParams();
    int retry = providerMessage.getRetry();

    // log it

    DLog.debug(
        lctx ,
        headerLog + "Transforming provider dr message "
            + "to dr buffer bean : providerId = " + providerId
            + " , externalStatus = " + externalStatus
            + " , externalStatusDate = " + externalStatusDate
            + " , internalStatus = " + internalStatus + " , priority = "
            + priority + " , description = " + description
            + " , internalMapParams = " + internalMapParams
            + " , externalParams = "
            + StringEscapeUtils.escapeJava( externalParams ) + " , retry = "
            + retry );

    // construct dr buffer bean

    drBufferBean = DrBufferBeanFactory.createDrBufferBean( providerId ,
        messageId , externalStatus , externalStatusDate , internalStatus ,
        priority , description , internalMapParams , externalParams , retry );

    return drBufferBean;
  }

  public static ProviderMessage transformDrBufferBeanToProviderMessage(
      DrBufferBean drBufferBean ) {
    ProviderMessage providerMessage = null;

    if ( drBufferBean == null ) {
      return providerMessage;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( Integer
        .toString( drBufferBean.getDnId() ) );

    // prepare parameters for provider message

    String recordId = Integer.toString( drBufferBean.getDnId() );
    String eventId = null;
    String channelSessionId = null;
    String providerId = drBufferBean.getProviderId();
    String messageType = "SMS.DR";
    int contentType = 0;
    String internalMessageId = null;
    String externalMessageId = drBufferBean.getMessageId();
    String destinationAddress = null;
    String originAddress = null;
    String originAddrMask = null;
    String destinationNode = null;
    String originNode = null;
    String message = null;
    int messageCount = 0;
    String internalStatus = drBufferBean.getInternalStatus();
    String externalStatus = drBufferBean.getExternalStatus();
    double debitAmount = 0;
    int priority = drBufferBean.getPriority();
    int retry = drBufferBean.getRetry();
    String description = drBufferBean.getDescription();
    Date deliverDateTime = null;
    Date submitDateTime = null;
    Date statusDateTime = drBufferBean.getExternalStatusDate();

    // construct provider message

    providerMessage = ProviderMessageFactory.createProviderMessage( recordId ,
        eventId , channelSessionId , providerId , messageType , contentType ,
        internalMessageId , externalMessageId , destinationAddress ,
        originAddress , originAddrMask , destinationNode , originNode ,
        message , messageCount , internalStatus , externalStatus , debitAmount ,
        priority , retry , description , deliverDateTime , submitDateTime ,
        statusDateTime );

    // additional params

    providerMessage.setOptionalParams( drBufferBean.getInternalMapParams() );
    providerMessage.setExternalParams( drBufferBean.getExternalParams() );

    // log it

    DLog.debug(
        lctx ,
        headerLog + "Transforming dr buffer bean "
            + "to provider dr message : recordId = " + recordId
            + " , providerId = " + providerId + " , externalMessageId = "
            + externalMessageId + " , internalStatus = " + internalStatus
            + " , externalStatus = " + externalStatus + " , priority = "
            + priority + " , retry = " + retry + " , description = "
            + description + " , statusDateTime = "
            + DateTimeFormat.convertToString( statusDateTime )
            + " , optionalParams = " + providerMessage.getOptionalParams()
            + " , externalParams = " + providerMessage.getExternalParams() );

    return providerMessage;
  }

}
