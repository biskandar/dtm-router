package com.beepcast.router.dr;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DrBufferService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "DrBufferService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DrBufferDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public DrBufferService() {
    dao = new DrBufferDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( DrBufferBean bean ) {
    boolean inserted = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to insert , found null bean" );
      return inserted;
    }
    if ( StringUtils.isBlank( bean.getProviderId() ) ) {
      DLog.warning( lctx , "Failed to insert , found blank provider id" );
      return inserted;
    }
    if ( StringUtils.isBlank( bean.getMessageId() ) ) {
      DLog.warning( lctx , "Failed to insert , found blank message id" );
      return inserted;
    }
    inserted = dao.insert( bean );
    return inserted;
  }

  public int delete( int dnId ) {
    int totalRecords = -1;
    if ( dnId < 1 ) {
      DLog.warning( lctx , "Failed to delete records , found zero dnId" );
      return totalRecords;
    }
    totalRecords = dao.delete( dnId );
    return totalRecords;
  }

  public long totalRecords() {
    return dao.totalRecords( null );
  }

  public long totalRecords( String criteria ) {
    return dao.totalRecords( criteria );
  }

}
