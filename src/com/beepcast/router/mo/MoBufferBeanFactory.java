package com.beepcast.router.mo;

import java.util.Date;
import java.util.Map;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MoBufferBeanFactory {

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

  public static MoBufferBean createMoBufferBean( int trnprcType , int clientId ,
      int eventId , String eventCode , String messageId , String messageType ,
      String sourceNode , String provider , String phone , String message ,
      String fixedMessage , String senderId , Map mapExternalParams ,
      Date sendDate , boolean simulation , Date dateInserted ) {
    MoBufferBean bean = new MoBufferBean();
    bean.setTrnprcType( trnprcType );
    bean.setClientId( clientId );
    bean.setEventId( eventId );
    bean.setEventCode( eventCode );
    bean.setMessageId( messageId );
    bean.setMessageType( messageType );
    bean.setSourceNode( sourceNode );
    bean.setProvider( provider );
    bean.setPhone( phone );
    bean.setMessage( message );
    bean.setFixedMessage( fixedMessage );
    bean.setSenderId( senderId );
    bean.setMapExternalParams( mapExternalParams );
    bean.setSendDate( sendDate );
    bean.setSimulation( simulation );
    bean.setDateInserted( dateInserted );
    return bean;
  }

}
