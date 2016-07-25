package com.beepcast.router.mo;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class MoBufferService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "MoBufferService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private MoBufferDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public MoBufferService() {
    dao = new MoBufferDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( MoBufferBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to insert mo buffer bean "
          + ", found null bean" );
      return result;
    }
    result = dao.insert( bean );
    return result;
  }

  public List select( String criteria , int limit ) {
    return dao.select( criteria , limit );
  }

  public int deleteRecords( String strMoIds ) {
    int totalRecords = -1;
    if ( StringUtils.isBlank( strMoIds ) ) {
      DLog.warning( lctx , "Failed to delete records , found blank strMoIds" );
      return totalRecords;
    }
    totalRecords = dao.deleteRecords( strMoIds );
    return totalRecords;
  }

  public long totalRecords() {
    return dao.totalRecords( null );
  }

  public long totalRecords( String criteria ) {
    return dao.totalRecords( criteria );
  }

}
