package com.beepcast.router;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.beepcast.encrypt.EncryptApp;
import com.beepcast.router.common.BufferCommon;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterDRWorkerDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterDRWorkerDAO" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DatabaseLibrary dbLib;
  private final String keyPhoneNumber;
  private final String keyMessage;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterDRWorkerDAO() {
    dbLib = DatabaseLibrary.getInstance();

    EncryptApp encryptApp = EncryptApp.getInstance();
    keyPhoneNumber = encryptApp.getKeyValue( EncryptApp.KEYNAME_PHONENUMBER );
    keyMessage = encryptApp.getKeyValue( EncryptApp.KEYNAME_MESSAGE );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ArrayList readProviderMessagesFromDrBuffer( int limitRecords ,
      boolean debug ) {
    ArrayList providerMessages = new ArrayList();

    // compose sql

    String sqlSelect = "SELECT dn_id , provider_id , message_id "
        + ", external_status , external_status_date , internal_status "
        + ", description , internal_params , external_params , retry ";
    String sqlFrom = "FROM dn_buffer ";
    String sqlOrder = "ORDER BY priority DESC , dn_id ASC ";
    String sqlLimit = "LIMIT " + limitRecords + " ";

    String sql = sqlSelect + sqlFrom + sqlOrder + sqlLimit;

    // execute sql
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult queryResult = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( queryResult == null ) || ( queryResult.size() < 1 ) ) {
      return providerMessages;
    }

    // populate records

    ProviderMessage providerMessage = null;
    Iterator iterQ = queryResult.getIterator();
    while ( iterQ.hasNext() ) {
      QueryItem queryItem = (QueryItem) iterQ.next();
      if ( queryItem == null ) {
        continue;
      }
      providerMessage = extractToProviderMessage( queryItem );
      if ( providerMessage == null ) {
        continue;
      }
      providerMessages.add( providerMessage );
    }

    return providerMessages;
  }

  public boolean updateGatewayLogStatusMessage( ArrayList providerMessages ,
      boolean debug ) {
    boolean result = false;

    if ( ( providerMessages == null ) || ( providerMessages.size() < 1 ) ) {
      return result;
    }

    // prepare variable
    ProviderMessage providerMessage;
    String internalStatus , externalStatus , logId;
    Date statusDateTime;
    Integer retry;

    // prepare for batch
    Object[][] params = new Object[providerMessages.size()][];
    String[] arrMsgIds = new String[providerMessages.size()];

    // compose sql

    String sqlUpdate = "UPDATE gateway_log ";
    String sqlSet = "SET status = ? , external_status = ? ";
    sqlSet += ", status_date_tm = ? , retry = ? ";
    String sqlWhere = "WHERE log_id = ? ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    // iterate all provider message(s)
    int j = 0;
    Iterator iterator = providerMessages.iterator();
    while ( iterator.hasNext() ) {
      providerMessage = (ProviderMessage) iterator.next();
      if ( providerMessage == null ) {
        continue;
      }

      internalStatus = providerMessage.getInternalStatus();
      externalStatus = providerMessage.getExternalStatus();
      statusDateTime = providerMessage.getStatusDateTime();
      retry = new Integer( providerMessage.getRetry() );
      logId = providerMessage.getRecordId();

      internalStatus = ( internalStatus == null ) ? "" : internalStatus;
      externalStatus = ( externalStatus == null ) ? "" : externalStatus;
      statusDateTime = ( statusDateTime == null ) ? new Date() : statusDateTime;
      logId = ( logId == null ) ? "" : logId;

      params[j] = new Object[5];
      params[j][0] = internalStatus;
      params[j][1] = externalStatus;
      params[j][2] = DateTimeFormat.convertToString( statusDateTime );
      params[j][3] = retry;
      params[j][4] = logId;

      arrMsgIds[j] = providerMessage.getInternalMessageId();

      j = j + 1;
    }

    if ( j < 1 ) {
      return result;
    }

    // execute the sql batch

    DLog.debug( lctx , "Perform update status in the gateway log table "
        + ", total = " + j + " record(s)" );

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    int[] arrResults = dbLib.executeBatchStatement( "transactiondb" , sql ,
        params );
    if ( arrResults == null ) {
      return result;
    }

    StringBuffer sbFailedResult = null;
    for ( int i = 0 ; i < arrResults.length ; i++ ) {
      if ( arrResults[i] < 1 ) {
        if ( sbFailedResult == null ) {
          sbFailedResult = new StringBuffer();
        } else {
          sbFailedResult.append( "," );
        }
        sbFailedResult.append( arrMsgIds[i] );
      }
    }
    if ( sbFailedResult != null ) {
      DLog.warning( lctx , "List of failed to update status "
          + "in the gateway log table : " + sbFailedResult.toString() );
    }

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ProviderMessage extractToProviderMessage( QueryItem queryItem ) {
    ProviderMessage providerMessage = null;
    if ( queryItem == null ) {
      return providerMessage;
    }

    String stemp;

    providerMessage = new ProviderMessage();
    providerMessage.setMessageType( "SMS.DR" );
    stemp = (String) queryItem.get( 0 ); // dn_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setRecordId( stemp );
    }
    stemp = (String) queryItem.get( 1 ); // provider_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setProviderId( stemp );
    }
    stemp = (String) queryItem.get( 2 ); // message_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setExternalMessageId( stemp );
    }
    stemp = (String) queryItem.get( 3 ); // external_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setExternalStatus( stemp );
    }
    stemp = (String) queryItem.get( 4 ); // external_status_date
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setStatusDateTime( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) queryItem.get( 5 ); // internal_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setInternalStatus( stemp );
    }
    stemp = (String) queryItem.get( 6 ); // description
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setDescription( stemp );
    }
    stemp = (String) queryItem.get( 7 ); // internal_params
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setOptionalParams( BufferCommon
          .convertStrParamsToMap( stemp ) );
    }
    stemp = (String) queryItem.get( 8 ); // external_params
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      providerMessage.setExternalParams( stemp );
    }
    stemp = (String) queryItem.get( 9 ); // retry
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        providerMessage.setRetry( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    return providerMessage;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Util Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String sqlEncryptPhoneNumber( String phoneNumber ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_ENCRYPT('" );
    sb.append( phoneNumber );
    sb.append( "','" );
    sb.append( keyPhoneNumber );
    sb.append( "')" );
    return sb.toString();
  }

  private String sqlDecryptPhoneNumber() {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(encrypt_phone,'" );
    sb.append( keyPhoneNumber );
    sb.append( "') AS phone" );
    return sb.toString();
  }

  private String sqlEncryptShortCode( String shortCode ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_ENCRYPT('" );
    sb.append( shortCode );
    sb.append( "','" );
    sb.append( keyPhoneNumber );
    sb.append( "')" );
    return sb.toString();
  }

  private String sqlDecryptShortCode() {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(encrypt_short_code,'" );
    sb.append( keyPhoneNumber );
    sb.append( "') AS short_code" );
    return sb.toString();
  }

  private String sqlEncryptSenderId( String senderId ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_ENCRYPT('" );
    sb.append( senderId );
    sb.append( "','" );
    sb.append( keyPhoneNumber );
    sb.append( "')" );
    return sb.toString();
  }

  private String sqlDecryptSenderId() {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(encrypt_senderID,'" );
    sb.append( keyPhoneNumber );
    sb.append( "') AS senderID" );
    return sb.toString();
  }

  private String sqlEncryptMessage( String message ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_ENCRYPT('" );
    sb.append( StringEscapeUtils.escapeSql( message ) );
    sb.append( "','" );
    sb.append( keyMessage );
    sb.append( "')" );
    return sb.toString();
  }

  private String sqlDecryptMessage() {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(encrypt_message,'" );
    sb.append( keyMessage );
    sb.append( "') AS message" );
    return sb.toString();
  }

}
