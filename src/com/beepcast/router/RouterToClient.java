package com.beepcast.router;

import java.util.Date;

import com.beepcast.api.client.ClientApp;
import com.beepcast.api.client.data.Channel;
import com.beepcast.api.client.data.MessageType;
import com.beepcast.api.provider.data.ProviderMessage;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterToClient {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterToClient" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ClientApp clientApp;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterToClient() {

    clientApp = ClientApp.getInstance();
    if ( clientApp == null ) {
      DLog.error( lctx , "Failed to initialized , found null client app" );
      return;
    }

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean storeClientDRMessage( ProviderMessage providerMessage ) {
    boolean result = false;

    // verify is provider message is empty ?

    if ( providerMessage == null ) {
      DLog.warning( lctx , "Failed to store client dr message "
          + ", found null provider message" );
      return result;
    }

    // compose header log

    String headerLog = RouterMessageCommon.headerLog( providerMessage
        .getInternalMessageId() );

    // prepare for client dr message

    String messageId = providerMessage.getInternalMessageId();
    int eventId = -1;
    try {
      eventId = Integer.parseInt( providerMessage.getEventId() );
    } catch ( NumberFormatException e ) {
      DLog.warning( lctx , headerLog + "Failed to store client dr message "
          + ", found invalid event id" );
      return result;
    }
    String phoneNumber = providerMessage.getDestinationAddress();
    String channel = Channel.PROVIDER;
    String providerId = providerMessage.getProviderId();
    String messageType = MessageType.SMS;
    String statusCode = providerMessage.getInternalStatus();
    String statusDescription = providerMessage.getProviderId() + " : "
        + providerMessage.getExternalStatus();
    Date statusDateTime = providerMessage.getStatusDateTime();

    // store into client msg api

    int storeMsgStatus = clientApp.storeDrMessage( messageId , eventId ,
        phoneNumber , channel , providerId , messageType , statusCode ,
        statusDescription , statusDateTime );

    // log it

    DLog.debug( lctx ,
        headerLog + "Stored dr message into client msg api , result = "
            + clientApp.storeMsgStatusToString( storeMsgStatus ) );

    result = ( storeMsgStatus == ClientApp.STORE_MSG_STATUS_SUCCEED );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
