package com.beepcast.router.dr;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DrBuffersService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "DrBuffersService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DrBuffersDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public DrBuffersService() {
    dao = new DrBuffersDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int insert( List listDrBufferBeans ) {
    int totalInserted = 0;
    if ( listDrBufferBeans == null ) {
      DLog.warning( lctx , "Failed to insert , found null list beans" );
      return totalInserted;
    }
    if ( listDrBufferBeans.size() < 1 ) {
      DLog.warning( lctx , "Failed to insert , found blank list beans" );
      return totalInserted;
    }
    totalInserted = dao.insert( listDrBufferBeans );
    return totalInserted;
  }

  public List select( int limit ) {
    List listDrBufferBeans = null;
    if ( limit < 1 ) {
      DLog.warning( lctx , "Failed to select , found zero limit" );
      return listDrBufferBeans;
    }
    listDrBufferBeans = dao.select( limit );
    return listDrBufferBeans;
  }

  public int delete( String strDnIds ) {
    int totalRecords = -1;
    if ( StringUtils.isBlank( strDnIds ) ) {
      DLog.warning( lctx , "Failed to delete records , found blank strDnIds" );
      return totalRecords;
    }
    totalRecords = dao.delete( strDnIds );
    return totalRecords;
  }

}
