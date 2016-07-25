package com.beepcast.router.mo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.router.common.BufferCommon;
import com.beepcast.router.common.BufferQuery;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MoBufferDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "MoBufferDAO" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DatabaseLibrary dbLib;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public MoBufferDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( MoBufferBean bean ) {
    boolean result = false;

    if ( bean == null ) {
      return result;
    }

    // read params

    int trnprcType = bean.getTrnprcType();
    int clientId = bean.getClientId();
    int eventId = bean.getEventId();
    String eventCode = bean.getEventCode();
    String messageId = bean.getMessageId();
    String messageType = bean.getMessageType();
    String sourceNode = bean.getSourceNode();
    String provider = bean.getProvider();
    String phone = bean.getPhone();
    String message = bean.getMessage();
    String fixedMessage = bean.getFixedMessage();
    String senderId = bean.getSenderId();
    Map mapExternalParams = bean.getMapExternalParams();
    String strExternalParams = BufferCommon
        .convertMapParamsToStr( mapExternalParams );
    Date sendDate = bean.getSendDate();
    String strSendDate = DateTimeFormat.convertToString( sendDate );
    boolean simulation = bean.isSimulation();
    int intSimulation = simulation ? 1 : 0;
    Date dateInserted = bean.getDateInserted();
    String strDateInserted = DateTimeFormat.convertToString( dateInserted );

    // clean params

    eventCode = ( eventCode == null ) ? "" : eventCode.trim();
    messageId = ( messageId == null ) ? "" : messageId.trim();
    messageType = ( messageType == null ) ? "" : messageType.trim();
    sourceNode = ( sourceNode == null ) ? "" : sourceNode.trim();
    provider = ( provider == null ) ? "" : provider.trim();
    phone = ( phone == null ) ? "" : phone.trim();
    message = ( message == null ) ? "" : message.trim();
    fixedMessage = ( fixedMessage == null ) ? "" : fixedMessage.trim();
    senderId = ( senderId == null ) ? "" : senderId.trim();
    strExternalParams = ( strExternalParams == null ) ? "" : strExternalParams
        .trim();
    strSendDate = ( strSendDate == null ) ? "NULL" : "'" + strSendDate + "'";
    strDateInserted = ( strDateInserted == null ) ? DateTimeFormat
        .convertToString( new Date() ) : strDateInserted;

    // compose sql

    String sqlInsert = "INSERT INTO mo_buffer (trnprc_type";
    sqlInsert += ",client_id,event_id,event_code";
    sqlInsert += ",message_id,message_type,source_node,provider";
    sqlInsert += ",phone,message,fixed_message,senderID";
    sqlInsert += ",external_params,send_date,simulation";
    sqlInsert += ",date_inserted,date_updated) ";

    String sqlValues = "VALUES (" + trnprcType + "," + clientId + "," + eventId
        + ",'" + StringEscapeUtils.escapeSql( eventCode ) + "','"
        + StringEscapeUtils.escapeSql( messageId ) + "','"
        + StringEscapeUtils.escapeSql( messageType ) + "','"
        + StringEscapeUtils.escapeSql( sourceNode ) + "','"
        + StringEscapeUtils.escapeSql( provider ) + "','"
        + StringEscapeUtils.escapeSql( phone ) + "','"
        + StringEscapeUtils.escapeSql( message ) + "','"
        + StringEscapeUtils.escapeSql( fixedMessage ) + "','"
        + StringEscapeUtils.escapeSql( senderId ) + "','" + strExternalParams
        + "'," + strSendDate + "," + intSimulation + "," + "'"
        + strDateInserted + "',NOW() ) ";

    String sql = sqlInsert + sqlValues;

    // execute sql

    DLog.debug( lctx , "Perform " + StringEscapeUtils.escapeJava( sql ) );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public List select( String criteria , int limit ) {
    List list = new ArrayList();

    // compose sql
    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "";
    if ( StringUtils.isBlank( criteria ) ) {
      sqlWhere = criteria;
    }
    String sqlOrder = "ORDER BY mo_id ASC ";
    String sqlLimit = "LIMIT " + limit + " ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    // execute sql
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return list;
    }

    // populate records
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      MoBufferBean bean = populateRecord( qi );
      if ( bean == null ) {
        continue;
      }
      list.add( bean );
    }

    return list;
  }

  public int deleteRecords( String strMoIds ) {
    int result = 0;

    if ( strMoIds == null ) {
      return result;
    }

    // compose sql
    String sqlDelete = "DELETE FROM mo_buffer ";
    String sqlWhere = "WHERE mo_id IN (" + strMoIds + ") ";
    String sql = sqlDelete + sqlWhere;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      result = irslt.intValue();
    }

    return result;
  }

  public long totalRecords( String criteria ) {
    long totalRecords = 0;

    String sqlSelect = "SELECT COUNT(mo_id) AS total ";
    String sqlFrom = "FROM mo_buffer ";
    String sqlWhere = "";
    if ( !StringUtils.isBlank( criteria ) ) {
      sqlWhere = "WHERE " + criteria + " ";
    }
    String sql = sqlSelect + sqlFrom + sqlWhere;

    totalRecords = BufferQuery.getFirstValueLong( sql , 0 );
    return totalRecords;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String sqlSelectFrom() {

    String sqlSelect = "SELECT mo_id , trnprc_type , client_id ";
    sqlSelect += ", event_id , event_code , message_id ";
    sqlSelect += ", message_type , source_node , provider , phone ";
    sqlSelect += ", message , fixed_message , senderID ";
    sqlSelect += ", external_params , send_date , simulation , date_inserted ";

    String sqlFrom = "FROM mo_buffer ";

    String sqlSelectFrom = sqlSelect + sqlFrom;
    return sqlSelectFrom;
  }

  private MoBufferBean populateRecord( QueryItem queryItem ) {
    MoBufferBean bean = null;

    if ( queryItem == null ) {
      return bean;
    }

    String stemp = null;

    bean = new MoBufferBean();

    stemp = (String) queryItem.get( 0 ); // mo_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setMoId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 1 ); // trnprc_type
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTrnprcType( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 2 ); // client_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setClientId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 3 ); // event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setEventId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 4 ); // event_code
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setEventCode( stemp );
    }
    stemp = (String) queryItem.get( 5 ); // message_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMessageId( stemp );
    }
    stemp = (String) queryItem.get( 6 ); // message_type
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMessageType( stemp );
    }
    stemp = (String) queryItem.get( 7 ); // source_node
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSourceNode( stemp );
    }
    stemp = (String) queryItem.get( 8 ); // provider
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setProvider( stemp );
    }
    stemp = (String) queryItem.get( 9 ); // phone
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setPhone( stemp );
    }
    stemp = (String) queryItem.get( 10 ); // message
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMessage( stemp );
    }
    stemp = (String) queryItem.get( 11 ); // fixed_message
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setFixedMessage( stemp );
    }
    stemp = (String) queryItem.get( 12 ); // senderID
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSenderId( stemp );
    }
    stemp = (String) queryItem.get( 13 ); // external_params
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMapExternalParams( BufferCommon.convertStrParamsToMap( stemp ) );
    }
    stemp = (String) queryItem.get( 14 ); // send_date
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSendDate( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) queryItem.get( 15 ); // simulation
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSimulation( stemp.equals( "1" ) );
    }
    stemp = (String) queryItem.get( 16 ); // date_inserted
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateInserted( DateTimeFormat.convertToDate( stemp ) );
    }

    return bean;
  }

}
