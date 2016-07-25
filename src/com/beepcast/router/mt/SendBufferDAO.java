package com.beepcast.router.mt;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.beepcast.router.RouterApp;
import com.beepcast.router.common.BufferCommon;
import com.beepcast.router.common.BufferQuery;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

import edu.emory.mathcs.backport.java.util.Arrays;

public class SendBufferDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "SendBufferDAO" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private RouterApp routerApp;
  private DatabaseLibrary dbLib;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public SendBufferDAO() {
    routerApp = RouterApp.getInstance();
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( SendBufferBean bean ) {
    boolean result = false;

    if ( bean == null ) {
      return result;
    }

    // read params

    String messageId = bean.getMessageId();
    int messageType = bean.getMessageType();
    int messageCount = bean.getMessageCount();
    String messageContent = bean.getMessageContent();
    double debitAmount = bean.getDebitAmount();
    String phone = bean.getPhoneNumber();
    String provider = bean.getProvider();
    int eventId = bean.getEventId();
    int channelSessionId = bean.getChannelSessionId();
    String senderId = bean.getSenderId();
    int priority = bean.getPriority();
    int suspended = bean.isSuspended() ? 1 : 0;
    int simulation = bean.isSimulation() ? 1 : 0;
    int retry = bean.getRetry();
    String params = BufferCommon.convertMapParamsToStr( bean.getMapParams() );
    Date dateSend = bean.getDateSend();

    // clean params

    messageId = ( messageId == null ) ? "" : messageId.trim();
    messageContent = ( messageContent == null ) ? "" : messageContent.trim();
    provider = ( provider == null ) ? "" : provider.trim();
    senderId = ( senderId == null ) ? "" : senderId.trim();
    params = ( params == null ) ? "" : params.trim();
    dateSend = ( dateSend == null ) ? new Date() : dateSend;

    // compose sql

    String sqlInsert = "INSERT INTO send_buffer ";
    sqlInsert += "(message_id,message_type,message_count,message";
    sqlInsert += ",debit_amount,phone,provider,event_id";
    sqlInsert += ",channel_session_id,senderID,priority,suspended";
    sqlInsert += ",simulation,retry,params,date_tm,date_send) ";

    String sqlValues = "VALUES ";
    sqlValues += "('" + StringEscapeUtils.escapeSql( messageId ) + "',"
        + messageType + "," + messageCount + ",'"
        + StringEscapeUtils.escapeSql( messageContent ) + "'," + debitAmount
        + ",'" + StringEscapeUtils.escapeSql( phone ) + "','"
        + StringEscapeUtils.escapeSql( provider ) + "'," + eventId + ","
        + channelSessionId + ",'" + StringEscapeUtils.escapeSql( senderId )
        + "'," + priority + "," + suspended + "," + simulation + "," + retry
        + ",'" + params + "',NOW(),'"
        + DateTimeFormat.convertToString( dateSend ) + "')";

    String sql = sqlInsert + sqlValues;

    if ( routerApp.isDebug() ) {
      DLog.debug( lctx , "Perform " + StringEscapeUtils.escapeJava( sql ) );
    }

    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public int insertBeans( List listSendBufferBeans ) {
    int totalRecords = 0;

    if ( listSendBufferBeans == null ) {
      return totalRecords;
    }

    // compose sql batch
    String sqlInsert = "INSERT INTO send_buffer ";
    sqlInsert += "( message_id , message_type , message_count , message ";
    sqlInsert += ", debit_amount , phone , provider , event_id , channel_session_id ";
    sqlInsert += ", senderID , priority , suspended , simulation , retry , params ";
    sqlInsert += ", date_tm , date_send ) ";
    String sqlValues = "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),?) ";
    String sql = sqlInsert + sqlValues;

    // prepare the holder
    int idxRec = 0 , maxRec = listSendBufferBeans.size();
    Object[][] params = new Object[maxRec][];

    // load parameters
    SendBufferBean sendBufferBean = null;
    Iterator iterSendBufferBeans = listSendBufferBeans.iterator();
    while ( iterSendBufferBeans.hasNext() ) {
      sendBufferBean = (SendBufferBean) iterSendBufferBeans.next();
      if ( sendBufferBean == null ) {
        continue;
      }

      // clean parameters
      String messageId = sendBufferBean.getMessageId();
      messageId = ( messageId == null ) ? "" : messageId;
      String messageContent = sendBufferBean.getMessageContent();
      messageContent = ( messageContent == null ) ? "" : messageContent;
      String phoneNumber = sendBufferBean.getPhoneNumber();
      phoneNumber = ( phoneNumber == null ) ? "" : phoneNumber;
      String provider = sendBufferBean.getProvider();
      provider = ( provider == null ) ? "" : provider;
      String senderId = sendBufferBean.getSenderId();
      senderId = ( senderId == null ) ? "" : senderId;
      String strParams = BufferCommon.convertMapParamsToStr( sendBufferBean
          .getMapParams() );
      strParams = ( strParams == null ) ? "" : strParams;

      // clean dateSend
      Date dateSend = sendBufferBean.getDateSend();
      dateSend = ( dateSend == null ) ? new Date() : dateSend;
      String dateSendStr = DateTimeFormat.convertToString( dateSend );

      // create param
      params[idxRec] = new Object[16];
      params[idxRec][0] = messageId;
      params[idxRec][1] = Integer.toString( sendBufferBean.getMessageType() );
      params[idxRec][2] = Integer.toString( sendBufferBean.getMessageCount() );
      params[idxRec][3] = messageContent;
      params[idxRec][4] = Double.toString( sendBufferBean.getDebitAmount() );
      params[idxRec][5] = phoneNumber;
      params[idxRec][6] = provider;
      params[idxRec][7] = Integer.toString( sendBufferBean.getEventId() );
      params[idxRec][8] = Integer.toString( sendBufferBean
          .getChannelSessionId() );
      params[idxRec][9] = senderId;
      params[idxRec][10] = Integer.toString( sendBufferBean.getPriority() );
      params[idxRec][11] = "0"; // suspended
      params[idxRec][12] = "0"; // simulation
      params[idxRec][13] = Integer.toString( sendBufferBean.getRetry() );
      params[idxRec][14] = strParams;
      params[idxRec][15] = dateSendStr;

      // for debug only
      if ( routerApp.isDebug() ) {
        DLog.debug( lctx , "Prepared batch send buffer record ( " + idxRec
            + " ) : " + Arrays.asList( params[idxRec] ) );
      }

      idxRec = idxRec + 1;
    }

    if ( idxRec < 1 ) {
      return totalRecords;
    }

    int[] arrResults = dbLib.executeBatchStatement( "transactiondb" , sql ,
        params );
    if ( arrResults == null ) {
      DLog.warning( lctx , "Failed to execute the batch insert "
          + "send buffer bean(s) into table" );
      return totalRecords;
    }

    StringBuffer sbLogIdxFailed = new StringBuffer();
    for ( idxRec = 0 ; idxRec < arrResults.length ; idxRec++ ) {
      if ( arrResults[idxRec] > 0 ) {
        totalRecords = totalRecords + 1;
      } else {
        sbLogIdxFailed.append( idxRec + "," );
      }
    }

    DLog.debug( lctx , "Inserted into send buffer table total = "
        + totalRecords + " record(s) , and list of index record failed : "
        + sbLogIdxFailed.toString() );
    return totalRecords;
  }

  public List selectBeans( String criteria , int limit ) {
    // build query
    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "";
    if ( !StringUtils.isBlank( criteria ) ) {
      sqlWhere = "WHERE " + criteria + " ";
    }
    String sqlOrder = "ORDER BY send_id ASC ";
    String sqlLimit = "LIMIT " + limit + " ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    // execute query and populate records
    List listBeans = populateRecords( sql );
    return listBeans;
  }

  public List selectActiveBeansByListProviderIds( int limitRecords ,
      List listProviderIds ) {
    List listBeans = new ArrayList();

    // validate providerIds
    if ( listProviderIds == null ) {
      return listBeans;
    }
    int sizeProviderIds = listProviderIds.size();
    if ( sizeProviderIds < 1 ) {
      return listBeans;
    }

    // compose max record read
    if ( limitRecords < 1 ) {
      return listBeans;
    }
    int limitRecordPerProvider = limitRecords / sizeProviderIds;
    if ( limitRecordPerProvider < 100 ) {
      limitRecordPerProvider = 100;
    }

    // compose query

    StringBuffer sbSql = null;

    String sqlSelectFrom = sqlSelectFrom();
    String sqlCondition = "WHERE ( suspended = 0 ) ";
    sqlCondition += "AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) ";
    String sqlOrder = "ORDER BY priority DESC , send_id ASC ";
    String sqlLimit = "LIMIT " + limitRecordPerProvider + " ";

    Iterator iterProviderIds = listProviderIds.iterator();
    while ( iterProviderIds.hasNext() ) {
      String providerId = (String) iterProviderIds.next();
      if ( StringUtils.isBlank( providerId ) ) {
        continue;
      }

      StringBuffer sbSqlProvider = new StringBuffer();
      sbSqlProvider.append( "( " );
      sbSqlProvider.append( sqlSelectFrom );
      sbSqlProvider.append( sqlCondition );
      sbSqlProvider.append( "AND ( provider = '" + providerId + "' ) " );
      sbSqlProvider.append( sqlOrder );
      sbSqlProvider.append( sqlLimit );
      sbSqlProvider.append( ") " );

      if ( sbSql == null ) {
        sbSql = new StringBuffer();
      } else {
        sbSql.append( "UNION ALL " );
      }
      sbSql.append( sbSqlProvider.toString() );

    }

    if ( sbSql == null ) {
      return listBeans;
    }

    listBeans = populateRecords( sbSql.toString() );
    return listBeans;
  }

  public List selectEventIds() {
    List listEventIds = new ArrayList();

    // build query

    String sqlSelect = "SELECT DISTINCT ( event_id ) ";
    String sqlFrom = "FROM send_buffer ";
    String sql = sqlSelect + sqlFrom;

    // execute query

    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listEventIds;
    }
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      int eventId = 0;
      try {
        eventId = Integer.parseInt( qi.getFirstValue() );
      } catch ( NumberFormatException e ) {
      }
      if ( eventId < 1 ) {
        continue;
      }
      listEventIds.add( new Integer( eventId ) );
    }

    return listEventIds;
  }

  public long totalRecords( String criteria ) {
    long totalRecords = 0;

    String sqlSelect = "SELECT COUNT(send_id) AS total ";
    String sqlFrom = "FROM send_buffer ";
    String sqlWhere = "";
    if ( !StringUtils.isBlank( criteria ) ) {
      sqlWhere = "WHERE " + criteria + " ";
    }
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( routerApp.isDebug() ) {
      DLog.debug( lctx , "Performed " + sql );
    }

    totalRecords = BufferQuery.getFirstValueLong( sql , 0 );
    return totalRecords;
  }

  public boolean update( SendBufferBean bean ) {
    boolean result = false;

    if ( bean == null ) {
      return result;
    }

    // prepare params

    int sendId = bean.getSendId();
    int simulation = bean.isSimulation() ? 1 : 0;
    Date dtmDateSend = bean.getDateSend();
    String strDateSend = DateTimeFormat.convertToString( dtmDateSend );
    int suspended = bean.isSuspended() ? 1 : 0;

    // build query

    String sqlUpdate = "UPDATE send_buffer ";
    String sqlSet = "SET phone = '"
        + StringEscapeUtils.escapeSql( bean.getPhoneNumber() )
        + "' , message = '"
        + StringEscapeUtils.escapeSql( bean.getMessageContent() )
        + "' , simulation = " + simulation + " , message_type = "
        + bean.getMessageType() + " , date_send = '" + strDateSend
        + "' , provider = '" + StringEscapeUtils.escapeSql( bean.getProvider() )
        + "' , event_id = " + bean.getEventId() + " , suspended = " + suspended
        + " ";
    String sqlWhere = "WHERE ( send_id = " + sendId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    // execute query
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public int deleteBeans( List listSendBufferBeans ) {
    int totalRecords = 0;

    if ( listSendBufferBeans == null ) {
      return totalRecords;
    }

    StringBuffer sbSendIds = null;

    Iterator iterSendBufferBeans = listSendBufferBeans.iterator();
    while ( iterSendBufferBeans.hasNext() ) {
      SendBufferBean sendBufferBean = (SendBufferBean) iterSendBufferBeans
          .next();
      if ( sendBufferBean == null ) {
        continue;
      }
      int sendId = sendBufferBean.getSendId();
      if ( sendId < 1 ) {
        continue;
      }
      if ( sbSendIds == null ) {
        sbSendIds = new StringBuffer();
      } else {
        sbSendIds.append( "," );
      }
      sbSendIds.append( Integer.toString( sendId ) );
    }

    if ( sbSendIds == null ) {
      return totalRecords;
    }

    // build query
    String sqlDelete = "DELETE FROM send_buffer ";
    String sqlWhere = "WHERE send_id IN ( " + sbSendIds.toString() + " ) ";
    String sql = sqlDelete + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    // execute query
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      totalRecords = irslt.intValue();
    }

    return totalRecords;
  }

  public boolean delete( long sendId ) {
    boolean result = false;

    if ( sendId < 1 ) {
      return result;
    }

    // build query
    String sqlDelete = "DELETE FROM send_buffer ";
    String sqlWhere = "WHERE send_id = " + sendId + " ";
    String sql = sqlDelete + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    // execute query
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean suspend( int eventId ) {
    boolean result = false;

    // build query

    String sqlUpdate = "UPDATE send_buffer ";
    String sqlSet = "SET suspended = 1 ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute query

    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean resume( int eventId ) {
    boolean result = false;

    // build query

    String sqlUpdate = "UPDATE send_buffer ";
    String sqlSet = "SET suspended = 0 ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute query

    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private List populateRecords( String sql ) {
    List listBeans = new ArrayList();

    if ( sql == null ) {
      return listBeans;
    }

    // debug
    if ( routerApp.isDebug() ) {
      DLog.debug( lctx , "Performed " + sql );
    }

    // execute query
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listBeans;
    }

    // populate records
    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      SendBufferBean sendBufferBean = populateRecord( qi );
      if ( sendBufferBean == null ) {
        continue;
      }
      listBeans.add( sendBufferBean );
    }

    return listBeans;
  }

  private String sqlSelectFrom() {
    String sqlSelect = "SELECT send_id , event_id , channel_session_id ";
    sqlSelect += ", provider , date_tm , phone , message_id , message_type ";
    sqlSelect += ", message_count , message , debit_amount , priority ";
    sqlSelect += ", retry , senderID , params ";
    String sqlFrom = "FROM send_buffer ";
    String sqlSelectFrom = sqlSelect + sqlFrom;
    return sqlSelectFrom;
  }

  private SendBufferBean populateRecord( QueryItem qi ) {
    SendBufferBean bean = null;

    if ( qi == null ) {
      return bean;
    }

    bean = new SendBufferBean();

    String stemp;

    stemp = (String) qi.get( 0 ); // send_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setSendId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setEventId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 2 ); // channel_session_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setChannelSessionId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 3 ); // provider
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setProvider( stemp );
    }

    stemp = (String) qi.get( 4 ); // date_tm
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSend( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 5 ); // phone
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setPhoneNumber( stemp );
    }

    stemp = (String) qi.get( 6 ); // message_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMessageId( stemp );
    }

    stemp = (String) qi.get( 7 ); // message_type
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setMessageType( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 8 ); // message_count
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setMessageCount( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 9 ); // message
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMessageContent( stemp );
    }

    stemp = (String) qi.get( 10 ); // debit_amount
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setDebitAmount( Double.parseDouble( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 11 ); // priority
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setPriority( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 12 ); // retry
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setRetry( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 13 ); // senderID
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSenderId( stemp );
    }

    stemp = (String) qi.get( 14 ); // params
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMapParams( BufferCommon.convertStrParamsToMap( stemp ) );
    }

    return bean;
  }

}
