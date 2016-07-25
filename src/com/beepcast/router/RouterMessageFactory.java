package com.beepcast.router;

import java.util.Date;
import java.util.Map;

import com.beepcast.router.common.MessageTypeUtils;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterMessageFactory {

  static final DLogContext lctx = new SimpleContext( "RouterMessageFactory" );

  public static RouterMessage createRouterMOMessage( int transProcType ,
      int clientId , int eventId , String eventCode , String messageId ,
      String messageType , String origin , String providerId ,
      String originAddress , String messageContent , String messageReply ,
      String destinationAddress , Map externalMapParams , Date deliverDateTime ) {
    RouterMessage routerMessage = new RouterMessage();

    routerMessage.setMessageDirection( RouterMessage.MSGDIR_MO );

    routerMessage.setTransProcType( transProcType );
    routerMessage.setClientId( clientId );
    routerMessage.setEventId( eventId );
    routerMessage.setEventCode( eventCode );
    routerMessage.setInternalMessageId( messageId );
    routerMessage.setMessageType( messageType );
    routerMessage.setOrigin( origin );
    routerMessage.setProviderId( providerId );
    routerMessage.setOriginAddress( originAddress );
    routerMessage.setMessage( messageContent );
    routerMessage.setReplyMessage( messageReply );
    routerMessage.setDestinationAddress( destinationAddress );
    if ( externalMapParams != null ) {
      routerMessage.getExternalMapParams().putAll( externalMapParams );
    }
    routerMessage.setDeliverDateTime( deliverDateTime );

    return routerMessage;
  }

  public static RouterMessage createRouterMTMessage( String messageId ,
      int gatewayMessageType , int messageCount , String messageContent ,
      double debitAmount , String phoneNumber , String providerId ,
      int eventId , int channelSessionId , String senderId , int priority ,
      int retry , Date dateSend , Map internalMapParams , Map externalMapParams ) {
    RouterMessage routerMessage = createRouterMTMessage( messageId ,
        RouterMessage.MSGTYPE_SMS , RouterMessage.CONTENTTYPE_SMSTEXT ,
        messageCount , messageContent , debitAmount , phoneNumber , providerId ,
        eventId , channelSessionId , senderId , priority , retry , dateSend ,
        internalMapParams , externalMapParams );

    routerMessage.setMessageType( MessageTypeUtils
        .transformGatewayToRouterMessageType( gatewayMessageType ) );
    routerMessage.setContentType( MessageTypeUtils
        .transformGatewayMessageTypeToRouterContentType( gatewayMessageType ) );
    routerMessage.getInternalMapParams().put( "gatewayMessageType" ,
        Integer.toString( gatewayMessageType ) );

    String headerLog = RouterMessageCommon.headerLog( messageId );
    DLog.debug( lctx , headerLog
        + "Resolved message / content type : routerMessageType = "
        + routerMessage.getMessageType() + " , routerContentType = "
        + routerMessage.getContentType() + " , gatewayMessageType = "
        + gatewayMessageType );

    return routerMessage;
  }

  public static RouterMessage createRouterMTMessage( String messageId ,
      String messageType , int contentType , int messageCount ,
      String messageContent , double debitAmount , String phoneNumber ,
      String providerId , int eventId , int channelSessionId , String senderId ,
      int priority , int retry , Date dateSend , Map internalMapParams ,
      Map externalMapParams ) {
    RouterMessage routerMessage = new RouterMessage();

    routerMessage.setMessageDirection( RouterMessage.MSGDIR_MT );

    routerMessage.setInternalMessageId( messageId );
    routerMessage.setMessageType( messageType );
    routerMessage.setContentType( contentType );
    routerMessage.setMessageCount( messageCount );
    routerMessage.setMessage( messageContent );
    routerMessage.setDebitAmount( debitAmount );
    routerMessage.setDestinationAddress( phoneNumber );
    routerMessage.setProviderId( providerId );
    routerMessage.setEventId( eventId );
    routerMessage.setChannelSessionId( channelSessionId );
    routerMessage.setOriginAddrMask( senderId );
    routerMessage.setPriority( priority );
    routerMessage.setRetry( retry );
    routerMessage.setSubmitDateTime( dateSend );
    if ( internalMapParams != null ) {
      routerMessage.getInternalMapParams().putAll( internalMapParams );
    }
    if ( externalMapParams != null ) {
      routerMessage.getExternalMapParams().putAll( externalMapParams );
    }

    return routerMessage;
  }

}
