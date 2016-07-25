package com.beepcast.router.dr;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.router.common.BufferCommon;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DrBuffersDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "DrBuffersDAO" );

  static final Object lockInsertBatch = new Object();

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

  public DrBuffersDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int insert( List listDrBufferBeans ) {
    int totalInserted = 0;
    synchronized ( lockInsertBatch ) {

      if ( listDrBufferBeans == null ) {
        return totalInserted;
      }

      // compose sql
      String sqlInsert = "INSERT INTO dn_buffer ";
      sqlInsert += "(provider_id,message_id,external_status";
      sqlInsert += ",external_status_date,internal_status,priority";
      sqlInsert += ",description,internal_params,external_params";
      sqlInsert += ",retry,date_inserted) ";
      String sqlValues = "VALUES ( ? , ? , ? , ? , ? , ? , ? ";
      sqlValues += ", ? , ? , ? , NOW() ) ";
      String sql = sqlInsert + sqlValues;

      DrBufferBean drBufferBean = null;
      List listParams = new ArrayList();
      Iterator iterDrBufferBeans = listDrBufferBeans.iterator();
      while ( iterDrBufferBeans.hasNext() ) {
        drBufferBean = (DrBufferBean) iterDrBufferBeans.next();
        if ( drBufferBean == null ) {
          continue;
        }

        // read params

        String providerId = drBufferBean.getProviderId();
        if ( StringUtils.isBlank( providerId ) ) {
          continue;
        }
        String messageId = drBufferBean.getMessageId();
        if ( StringUtils.isBlank( messageId ) ) {
          continue;
        }
        String extStatus = drBufferBean.getExternalStatus();
        if ( StringUtils.isBlank( extStatus ) ) {
          continue;
        }
        Date extStatusDate = drBufferBean.getExternalStatusDate();
        String intStatus = drBufferBean.getInternalStatus();
        int priority = drBufferBean.getPriority();
        String description = drBufferBean.getDescription();
        String intParams = BufferCommon.convertMapParamsToStr( drBufferBean
            .getInternalMapParams() );
        String extParams = drBufferBean.getExternalParams();
        int retry = drBufferBean.getRetry();

        // clean params

        providerId = ( providerId == null ) ? "" : providerId.trim();
        messageId = ( messageId == null ) ? "" : messageId.trim();
        extStatus = ( extStatus == null ) ? "" : extStatus.trim();
        extStatusDate = ( extStatusDate == null ) ? new Date() : extStatusDate;
        intStatus = ( intStatus == null ) ? "" : intStatus.trim();
        description = ( description == null ) ? "" : description.trim();
        intParams = ( intParams == null ) ? "" : intParams.trim();
        extParams = ( extParams == null ) ? "" : extParams.trim();

        // compose as a param

        Object[] param = new Object[10];
        param[0] = providerId;
        param[1] = messageId;
        param[2] = extStatus;
        param[3] = DateTimeFormat.convertToString( extStatusDate );
        param[4] = intStatus;
        param[5] = new Integer( priority );
        param[6] = description;
        param[7] = intParams;
        param[8] = extParams;
        param[9] = new Integer( retry );

        // add new param into the list
        listParams.add( param );

      } // while ( iterDrBufferBeans.hasNext() )

      int sizeListParams = listParams.size();
      if ( sizeListParams < 1 ) {
        // found empty list params , stop process here
        return totalInserted;
      }

      // convert list to array 2 dimensions of params
      Object[][] params = new Object[sizeListParams][];
      for ( int i = 0 ; i < sizeListParams ; i++ ) {
        Object[] param = (Object[]) listParams.get( i );
        if ( param == null ) {
          continue;
        }
        params[i] = new Object[param.length];
        for ( int j = 0 ; j < param.length ; j++ ) {
          params[i][j] = param[j];
        }
      }

      if ( params == null ) {
        // found null params , stop process here
        return totalInserted;
      }

      // execute sql batch
      int[] rslt = dbLib.executeBatchStatement( "transactiondb" , sql , params );
      if ( rslt != null ) {
        for ( int i = 0 ; i < rslt.length ; i++ ) {
          if ( rslt[i] > 0 ) {
            totalInserted = totalInserted + 1;
          }
        }
      }

      DLog.debug( lctx , "Successfully executed sql batch insert "
          + "dr status total = " + totalInserted + " record(s)" );

    } // synchronized ( lockBatch )
    return totalInserted;
  }

  public List select( int limit ) {
    List listDrBufferBeans = new ArrayList();

    // compose sql
    String sqlSelectFrom = DrBufferQuery.sqlSelectFrom();
    String sqlOrder = "ORDER BY priority DESC , dn_id ASC ";
    String sqlLimit = "LIMIT " + limit + " ";
    String sql = sqlSelectFrom + sqlOrder + sqlLimit;

    // execute sql
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listDrBufferBeans;
    }

    // populate records
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      DrBufferBean drBufferBean = DrBufferQuery.populateRecord( qi );
      if ( drBufferBean == null ) {
        continue;
      }
      listDrBufferBeans.add( drBufferBean );
    }

    return listDrBufferBeans;
  }

  public int delete( String strDnIds ) {
    int result = 0;

    if ( strDnIds == null ) {
      return result;
    }

    // compose sql
    String sqlDelete = "DELETE FROM dn_buffer ";
    String sqlWhere = "WHERE dn_id IN ( " + strDnIds + " ) ";
    String sql = sqlDelete + sqlWhere;

    // execute sql
    // DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      result = irslt.intValue();
    }

    return result;
  }

}
