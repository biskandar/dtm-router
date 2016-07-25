package com.beepcast.router.mt;

import java.util.Date;
import java.util.Map;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SendBufferBeanFactory {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "SendBufferBeanFactory" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static SendBufferBean createSendBufferBean( String messageId ,
      int messageType , int messageCount , String messageContent ,
      double debitAmount , String phoneNumber , String providerId ,
      int eventId , int channelSessionId , String senderId , int priority ,
      int retry , Map mapParams , Date dateSend ) {
    SendBufferBean bean = new SendBufferBean();
    bean.setMessageId( messageId );
    bean.setMessageType( messageType );
    bean.setMessageCount( messageCount );
    bean.setMessageContent( messageContent );
    bean.setDebitAmount( debitAmount );
    bean.setPhoneNumber( phoneNumber );
    bean.setProvider( providerId );
    bean.setEventId( eventId );
    bean.setChannelSessionId( channelSessionId );
    bean.setSenderId( senderId );
    bean.setPriority( priority );
    bean.setRetry( retry );
    if ( mapParams != null ) {
      bean.getMapParams().putAll( mapParams );
    }
    bean.setDateSend( dateSend );
    return bean;
  }

}
