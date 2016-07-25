package com.beepcast.router.common;

import java.util.Date;
import java.util.Map;

import com.beepcast.router.RouterMessage;
import com.beepcast.router.RouterMessageCommon;
import com.beepcast.router.RouterMessageFactory;
import com.beepcast.router.mt.SendBufferBean;
import com.beepcast.router.mt.SendBufferBeanFactory;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SendBufferBeanTransformUtils {

  static final DLogContext lctx = new SimpleContext(
      "SendBufferBeanTransformUtils" );

  public static RouterMessage transformSendBufferBeanToRouterMTMessage(
      SendBufferBean sendBufferBean ) {
    RouterMessage routerMTMessage = null;

    // validate must be parameters

    if ( sendBufferBean == null ) {
      return routerMTMessage;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( sendBufferBean
        .getMessageId() );

    // prepare parameters for router message

    String messageId = sendBufferBean.getMessageId();
    int gatewayMessageType = MessageTypeUtils
        .transformSendBufferToGatewayMessageType( sendBufferBean
            .getMessageType() );
    int messageCount = sendBufferBean.getMessageCount();
    String messageContent = sendBufferBean.getMessageContent();
    double debitAmount = sendBufferBean.getDebitAmount();
    String phoneNumber = sendBufferBean.getPhoneNumber();
    String providerId = sendBufferBean.getProvider();
    int eventId = sendBufferBean.getEventId();
    int channelSessionId = sendBufferBean.getChannelSessionId();
    String senderId = sendBufferBean.getSenderId();
    int priority = sendBufferBean.getPriority();
    int retry = sendBufferBean.getRetry();
    Date dateSend = sendBufferBean.getDateSend();
    Map internalMapParams = sendBufferBean.getMapParams();
    Map externalMapParams = null;

    // log it

    DLog.debug( lctx , headerLog + "Transforming send buffer bean "
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

  public static SendBufferBean transformRouterMTMessageToSendBufferBean(
      RouterMessage routerMTMessage ) {
    SendBufferBean sendBufferBean = null;

    // validate must be parameters

    if ( routerMTMessage == null ) {
      return sendBufferBean;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( routerMTMessage );

    // prepare parameters for send buffer bean

    String messageId = routerMTMessage.getInternalMessageId();

    int messageType = 0;
    try {
      messageType = Integer.parseInt( (String) routerMTMessage
          .getInternalMapParams().get( "gatewayMessageType" ) );
    } catch ( Exception e ) {
      messageType = MessageTypeUtils
          .transformGatewayToSendBufferMessageType( MessageTypeUtils
              .transformRouterContentTypeToGatewayMessageType( routerMTMessage
                  .getContentType() ) );
    }
    int messageCount = routerMTMessage.getMessageCount();
    String messageContent = routerMTMessage.getMessage();
    double debitAmount = routerMTMessage.getDebitAmount();
    String phoneNumber = routerMTMessage.getDestinationAddress();
    String providerId = routerMTMessage.getProviderId();
    int eventId = routerMTMessage.getEventId();
    int channelSessionId = routerMTMessage.getChannelSessionId();
    String senderId = routerMTMessage.getOriginAddrMask();
    int priority = routerMTMessage.getPriority();
    int retry = routerMTMessage.getRetry();
    Map mapParams = routerMTMessage.getInternalMapParams();
    Date dateSend = routerMTMessage.getSubmitDateTime();

    // log it

    DLog.debug( lctx , headerLog + "Transforming router mt message "
        + "to send buffer bean : messageType = " + messageType
        + " , messageCount = " + messageCount + " , debitAmount = "
        + debitAmount + " , providerId = " + providerId + " , eventId = "
        + eventId + " , channelSessionId = " + channelSessionId
        + " , priority = " + priority + " , retry = " + retry
        + " , dateSend = " + dateSend + " , mapParams = " + mapParams );

    // construct send buffer bean

    sendBufferBean = SendBufferBeanFactory.createSendBufferBean( messageId ,
        messageType , messageCount , messageContent , debitAmount ,
        phoneNumber , providerId , eventId , channelSessionId , senderId ,
        priority , retry , mapParams , dateSend );

    return sendBufferBean;
  }

}
