package com.beepcast.router;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.model.transaction.MessageType;
import com.beepcast.model.transaction.Node;
import com.beepcast.model.transaction.ProcessCode;
import com.beepcast.model.transaction.TransactionInputMessage;
import com.beepcast.model.transaction.TransactionMessageFactory;
import com.beepcast.model.transaction.TransactionMessageParam;
import com.beepcast.model.transaction.TransactionProcess;
import com.beepcast.model.transaction.TransactionProcessFactory;
import com.beepcast.model.transaction.TransactionProcessType;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.router.mo.MoBufferBean;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterMOProcessor {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterMOProcessor" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterMOProcessor() {

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean processMoMessage( RouterMessage msg , RouterConf cnf ) {
    boolean result = false;

    // validate must be param

    if ( msg == null ) {
      DLog.warning( lctx , "Failed to process mo message "
          + ", found null router message" );
      return result;
    }

    // header log

    String headerLog = RouterMessageCommon.headerLog( msg );

    try {

      // calculated latency start

      long deltaTime = System.currentTimeMillis();

      // prepare variables

      int tranProcType = msg.getTransProcType();
      int clientId = msg.getClientId();
      int eventId = msg.getEventId();
      String eventCode = msg.getEventCode();
      String provider = msg.getProviderId();
      String originNumber = msg.getOriginAddress();
      String destinationNumber = msg.getDestinationAddress();
      String messageId = msg.getInternalMessageId();
      String message = msg.getMessage();
      String replyMessage = msg.getReplyMessage();
      Map externalMapParams = msg.getExternalMapParams();
      Date deliveryDateTime = msg.getDeliverDateTime();

      // log first

      DLog.debug(
          lctx ,
          headerLog + "Process mo message : transProcType = " + tranProcType
              + " , eventId = " + eventId + " , eventCode = " + eventCode
              + " , provider = " + provider + " , originNumber = "
              + originNumber + " , destinationNumber = " + destinationNumber
              + " , externalMapParams = " + externalMapParams
              + " , deliverDateTime = "
              + DateTimeFormat.convertToString( deliveryDateTime ) );

      // validate must be params

      if ( StringUtils.isBlank( originNumber ) ) {
        DLog.warning( lctx , headerLog + "Failed to process mo message "
            + ", found blank phone" );
        return result;
      }
      if ( StringUtils.isBlank( message ) ) {
        DLog.warning( lctx , headerLog + "Found blank mo message content" );
      }

      // get default priority for mo message

      int priority = cnf.getMsgPriority();

      // generate input message with the default params

      TransactionInputMessage imsg = TransactionMessageFactory
          .createInputMessage( messageId , MessageType.TEXT_TYPE , message ,
              Node.MGW , originNumber , provider , destinationNumber , 0 ,
              priority );
      if ( imsg == null ) {
        DLog.warning( lctx , headerLog + "Failed to process mo message "
            + ", found failed to create input message" );
        return result;
      }

      // store preset client and event profile if any

      if ( msg.getClientId() > 0 ) {
        imsg.setClientId( msg.getClientId() );
        DLog.debug( lctx , headerLog + "Define pre-set transaction input "
            + "message params : clientId = " + imsg.getClientId() );
      }
      if ( msg.getEventId() > 0 ) {
        imsg.setEventId( msg.getEventId() );
        DLog.debug( lctx , headerLog + "Define pre-set transaction input "
            + "message params : eventId = " + imsg.getEventId() );
      }

      // set optional params of input message

      if ( ( replyMessage != null ) && ( !replyMessage.equals( "" ) ) ) {
        imsg.setReplyMessageContent( replyMessage );
      }

      // need to send later ?

      String paramSendDateStr = (String) externalMapParams
          .remove( MoBufferBean.HDRMAPEXTPARAM_SENDDATESTR );
      if ( ( paramSendDateStr != null ) && ( !paramSendDateStr.equals( "" ) ) ) {
        imsg.addMessageParam( TransactionMessageParam.HDR_SET_SENDDATESTR ,
            paramSendDateStr );
        DLog.debug( lctx , headerLog + "Added message param : "
            + TransactionMessageParam.HDR_SET_SENDDATESTR + " = "
            + paramSendDateStr );
      }

      // need to mask sender id ?

      String paramOriMaskAddr = (String) externalMapParams
          .remove( MoBufferBean.HDRMAPEXTPARAM_ORIMASKADDR );
      if ( ( paramOriMaskAddr != null ) && ( !paramOriMaskAddr.equals( "" ) ) ) {
        imsg.addMessageParam( TransactionMessageParam.HDR_SET_ORIMASKADDR ,
            paramOriMaskAddr );
        DLog.debug( lctx , headerLog + "Added message param : "
            + TransactionMessageParam.HDR_SET_ORIMASKADDR + " = "
            + paramOriMaskAddr );
      }

      // store all the remaining params

      if ( externalMapParams.size() > 0 ) {
        String key1 = null , key2 = null , val = null;
        Set setKeys = externalMapParams.keySet();
        Iterator iterKeys = setKeys.iterator();
        while ( iterKeys.hasNext() ) {
          key1 = (String) iterKeys.next();
          if ( key1 == null ) {
            continue;
          }

          // read the param's value

          val = (String) externalMapParams.get( key1 );

          // store as reserved variable ?

          if ( key1.startsWith( MoBufferBean.HDRMAPEXTPARAM_PREFRESDVAR ) ) {
            key2 = key1.substring( MoBufferBean.HDRMAPEXTPARAM_PREFRESDVAR
                .length() );
            if ( ( key2 != null ) && ( !key2.equals( "" ) ) ) {
              key2 = TransactionMessageParam.HDR_PREFIX_SET_RESERVED_VARIABLE
                  .concat( key2 );
              imsg.addMessageParam( key2 , val );
              DLog.debug( lctx , headerLog + "Added message param : " + key2
                  + " = " + val );
            }
          }

          // keep the params clean

          iterKeys.remove();

        }
      }

      // log first

      DLog.debug(
          lctx ,
          headerLog + "Build transaction input message : tranProcType = "
              + tranProcType + " , priority = " + imsg.getPriority()
              + " , originalProvider = " + imsg.getOriginalProvider()
              + " , originalAddress = " + imsg.getOriginalAddress()
              + " , destinationAddress = " + imsg.getDestinationAddress()
              + " , messageContent = "
              + StringEscapeUtils.escapeJava( imsg.getMessageContent() )
              + " , replyMessageContent = "
              + StringEscapeUtils.escapeJava( imsg.getReplyMessageContent() ) );

      // create transaction process based on tranProcType

      TransactionProcess transProcess = null;
      switch ( tranProcType ) {
      case TransactionProcessType.STANDARD :
        DLog.debug( lctx , headerLog + "Created transaction process object "
            + "with process type : standard" );
        transProcess = TransactionProcessFactory
            .generateTransactionProcessStandard();
        break;
      case TransactionProcessType.SIMULATION :
        DLog.debug( lctx , headerLog + "Created transaction process object "
            + "with process type : simulation" );
        transProcess = TransactionProcessFactory
            .generateTransactionProcessSimulation();
        break;
      case TransactionProcessType.INTERACT :
        DLog.debug( lctx , headerLog + "Created transaction process object "
            + "with process type : interact" );
        transProcess = TransactionProcessFactory
            .generateTransactionProcessInteract();
        break;
      case TransactionProcessType.BULK :

        DLog.debug( lctx , headerLog + "Created transaction process object "
            + "with process type : bulk" );
        transProcess = TransactionProcessFactory
            .generateTransactionProcessBulk();

        DLog.debug(
            lctx ,
            headerLog + "Found as bulk type , exchange between original "
                + "and destination address on input transaction message : "
                + imsg.getOriginalAddress() + " <-> "
                + imsg.getDestinationAddress() );
        String tempAddress = imsg.getOriginalAddress();
        imsg.setOriginalAddress( imsg.getDestinationAddress() );
        imsg.setDestinationAddress( tempAddress );

        break;
      default :
        DLog.debug( lctx , headerLog + "Created trans object "
            + "with process type standard" );
        transProcess = TransactionProcessFactory
            .generateTransactionProcessStandard();
        break;
      }
      if ( transProcess == null ) {
        DLog.warning( lctx , headerLog + "Failed to process mo message "
            + ", found can not create trans process object" );
        return result;
      }

      // run the transaction

      DLog.debug( lctx , headerLog + "Run the transaction process" );
      int processCode = transProcess.main( imsg );

      // get result , calculated latency finish , and log it

      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx ,
          headerLog + "Finish transaction process , resultCode = "
              + ProcessCode.processCodeToString( processCode ) + " , take = "
              + deltaTime + " ms" );

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to process mo message  , " + e );
    }

    return result;
  }
  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
