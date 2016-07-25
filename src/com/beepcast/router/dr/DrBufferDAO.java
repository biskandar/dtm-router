package com.beepcast.router.dr;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.router.common.BufferCommon;
import com.beepcast.router.common.BufferQuery;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DrBufferDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "DrBufferDAO" );

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

  public DrBufferDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( DrBufferBean bean ) {
    boolean result = false;

    if ( bean == null ) {
      return result;
    }

    // read params
    String providerId = bean.getProviderId();
    String messageId = bean.getMessageId();
    String externalStatus = bean.getExternalStatus();
    Date externalStatusDate = bean.getExternalStatusDate();
    String externalStatusDateStr = ( externalStatusDate == null ) ? "NULL"
        : "'" + DateTimeFormat.convertToString( externalStatusDate ) + "'";
    String internalStatus = bean.getInternalStatus();
    int priority = bean.getPriority();
    String description = bean.getDescription();
    String internalParams = BufferCommon.convertMapParamsToStr( bean
        .getInternalMapParams() );
    String externalParams = bean.getExternalParams();
    int retry = bean.getRetry();

    // clean params
    providerId = ( providerId == null ) ? "" : providerId;
    messageId = ( messageId == null ) ? "" : messageId;
    externalStatus = ( externalStatus == null ) ? "" : externalStatus;
    internalStatus = ( internalStatus == null ) ? "" : internalStatus;
    description = ( description == null ) ? "" : description;
    internalParams = ( internalParams == null ) ? "" : internalParams;
    externalParams = ( externalParams == null ) ? "" : externalParams;

    // compose sql
    String sqlInsert = "INSERT INTO dn_buffer ";
    sqlInsert += "(provider_id,message_id,external_status";
    sqlInsert += ",external_status_date,internal_status,priority";
    sqlInsert += ",description,internal_params,external_params";
    sqlInsert += ",retry,date_inserted) ";
    String sqlValues = "VALUES ('" + StringEscapeUtils.escapeSql( providerId )
        + "','" + StringEscapeUtils.escapeSql( messageId ) + "','"
        + StringEscapeUtils.escapeSql( externalStatus ) + "',"
        + externalStatusDateStr + ",'"
        + StringEscapeUtils.escapeSql( internalStatus ) + "'," + priority
        + ",'" + StringEscapeUtils.escapeSql( description ) + "','"
        + StringEscapeUtils.escapeSql( internalParams ) + "','"
        + StringEscapeUtils.escapeSql( externalParams ) + "'," + retry
        + ",NOW()) ";
    String sql = sqlInsert + sqlValues;

    // execute sql
    // DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public int delete( int dnId ) {
    int result = 0;

    if ( dnId < 1 ) {
      return result;
    }

    // compose sql
    String sqlDelete = "DELETE FROM dn_buffer ";
    String sqlWhere = "WHERE ( dn_id = " + dnId + " ) ";
    String sql = sqlDelete + sqlWhere;

    // execute sql
    // DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      result = irslt.intValue();
    }

    return result;
  }

  public long totalRecords( String criteria ) {
    long totalRecords = 0;

    String sqlSelect = "SELECT COUNT(dn_id) AS total ";
    String sqlFrom = "FROM dn_buffer ";
    String sqlWhere = "";
    if ( !StringUtils.isBlank( criteria ) ) {
      sqlWhere = "WHERE " + criteria + " ";
    }
    String sql = sqlSelect + sqlFrom + sqlWhere;

    totalRecords = BufferQuery.getFirstValueLong( sql , 0 );
    return totalRecords;
  }

}
