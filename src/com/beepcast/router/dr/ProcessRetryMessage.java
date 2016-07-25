package com.beepcast.router.dr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.beepcast.api.provider.ProviderPayment;
import com.beepcast.api.provider.common.MessageTypeUtils;
import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.model.gateway.GatewayLogBean;
import com.beepcast.model.transaction.MessageType;
import com.beepcast.model.transaction.TransactionMessageParam;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.RouterApp;
import com.beepcast.router.RouterDRWorker;
import com.beepcast.router.RouterMessageCommon;
import com.beepcast.router.common.DebitAmountResolver;
import com.beepcast.router.common.ProviderResolver;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ProcessRetryMessage {

  static final DLogContext lctx = new SimpleContext( "ProcessRetryMessage" );

  public static boolean execute( ProviderMessage providerMessage ,
      boolean switchProvider ) {
    boolean result = false;

    // trap delta time

    long deltaTime = System.currentTimeMillis();

    // validate must be params

    if ( providerMessage == null ) {
      return result;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( providerMessage
        .getInternalMessageId() );

    // log first

    DLog.debug( lctx , headerLog + "Retrying to send the message back "
        + ": retry value = " + providerMessage.getRetry()
        + " , submit date time = " + providerMessage.getSubmitDateTime()
        + " , switchProvider = " + switchProvider );

    try {

      // prepare the apps

      OnlinePropertiesApp opropsApp = OnlinePropertiesApp.getInstance();
      RouterApp routerApp = RouterApp.getInstance();

      // validate submit date time

      Date submitDateTime = providerMessage.getSubmitDateTime();
      if ( submitDateTime == null ) {
        DLog.warning( lctx , headerLog + "Bypass to retry send message back "
            + ", found unknown submitDateTime" );
        return result;
      }

      // validate if the message is already expired ?

      Date dateNow = new Date();
      long msDuration = dateNow.getTime() - submitDateTime.getTime();
      long msgExpiry = opropsApp.getLong( RouterDRWorker.OPROPS_EXPIRYMESSAGE ,
          routerApp.getConf().getMsgExpiry() );
      if ( msDuration >= msgExpiry ) {
        DLog.warning( lctx , headerLog + "Bypass to retry send message back "
            + ", found message is already expired , message duration = "
            + msDuration + " ms , maximum duration = " + msgExpiry + " ms" );
        return result;
      }

      // validate if the message is reached the maximum retries ?

      if ( !routerApp.verifyRetryValue( headerLog , providerMessage.getRetry() ) ) {
        DLog.warning( lctx , headerLog + "Bypass to retry send message back "
            + ", found the current retry value is reached the limit" );
        return result;
      }

      // read the next submit date time duration ( in millis )

      long retryDuration = routerApp.readRetryDuration( headerLog ,
          providerMessage.getRetry() );
      if ( retryDuration < 1 ) {
        DLog.warning( lctx , headerLog + "Bypass to retry send message back "
            + ", found failed to read retry duration configuration" );
        return result;
      }

      // set date send

      Date dateSend = new Date( dateNow.getTime() + retryDuration );

      // read gateway log bean

      GatewayLogBean gatewayLogBean = (GatewayLogBean) providerMessage
          .getOptionalParam( "gatewayLogBean" );

      // prepare default value for mt message message

      String messageId = routerApp.generateMsgId( "INT" );
      int messageType = 0;
      int messageCount = 0;
      String message = null;
      double debitAmount = 0;
      String phoneNumber = null;
      String providerId = providerMessage.getProviderId();
      int eventId = 0;
      int channelSessionId = 0;
      String senderId = null;
      int priority = routerApp.getConf().getMsgPriority();
      int retry = providerMessage.getRetry() + 1;
      Map params = new HashMap();
      params.put( TransactionMessageParam.HDR_ORI_PROVIDER , providerId );

      // compose mt message params based on gatewayLogBean or providerMtMessage

      if ( gatewayLogBean != null ) {

        messageType = MessageType.messageTypeToInt( gatewayLogBean
            .getMessageType() );
        messageCount = Integer.parseInt( gatewayLogBean.getMessageCount() );
        message = gatewayLogBean.getMessage();
        phoneNumber = gatewayLogBean.getPhone();
        eventId = (int) gatewayLogBean.getEventID();
        channelSessionId = (int) gatewayLogBean.getChannelSessionID();
        senderId = gatewayLogBean.getSenderID();

      } else {

        messageType = MessageTypeUtils
            .transformProviderContentTypeIntToGatewayMessageType( providerMessage
                .getContentType() );
        messageCount = providerMessage.getMessageCount();
        message = providerMessage.getMessage();
        phoneNumber = providerMessage.getDestinationAddress();
        eventId = Integer.parseInt( providerMessage.getEventId() );
        channelSessionId = Integer.parseInt( providerMessage
            .getChannelSessionId() );
        senderId = providerMessage.getOriginAddrMask();

      }

      // resolving remaining params

      debitAmount = DebitAmountResolver.resolveDebitAmount( headerLog ,
          messageCount , phoneNumber );
      providerId = ProviderResolver.resolveNewProvider( headerLog ,
          phoneNumber , eventId , providerId , ( switchProvider ? providerId
              : null ) );

      // log it first

      DLog.debug( lctx , headerLog + "Resolved re-send parameters "
          + ": messageId = " + messageId + " , messageCount = " + messageCount
          + " , debitAmount = " + debitAmount + " , providerId = " + providerId
          + " , priority = " + priority + " , retry = " + retry
          + " , dateSend = " + DateTimeFormat.convertToString( dateSend )
          + " , params = " + params );

      // deduct balance before sending
      {
        DLog.debug( lctx , headerLog + "Deducting amount " + debitAmount
            + " credit(s) from client balance" );
        providerMessage.setDebitAmount( debitAmount );
        if ( !ProviderPayment.doDebit( providerMessage ) ) {
          DLog.warning( lctx , "Failed to re-send mt message "
              + ", found failed to deduct client balance" );
          return result;
        }
      }

      // re-send mt messsage thru router app

      if ( !routerApp.sendMtMessage( messageId , messageType , messageCount ,
          message , debitAmount , phoneNumber , providerId , eventId ,
          channelSessionId , senderId , priority , retry , dateSend , params ) ) {
        DLog.warning( lctx , headerLog + "Failed to re-send mt message "
            + "thru router app" );
        return result;
      }

      // log it

      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Successfuly re-sent mt message "
          + "thru router app ( take " + deltaTime + " ms ) " );

      // result as true
      result = true;

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to re-send mt message , " + e );
    }

    return result;
  }

}
