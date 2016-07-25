package com.beepcast.router.mt;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SendBufferService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "SendBufferService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private SendBufferDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public SendBufferService() {
    dao = new SendBufferDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( SendBufferBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to insert send buffer bean "
          + ", found null bean" );
      return result;
    }
    result = dao.insert( bean );
    return result;
  }

  public int insertBeans( List listSendBufferBeans ) {
    int totalRecords = 0;
    if ( listSendBufferBeans == null ) {
      DLog.warning( lctx , "Failed to insert send buffer beans "
          + ", found null list" );
      return totalRecords;
    }
    totalRecords = dao.insertBeans( listSendBufferBeans );
    return totalRecords;
  }

  public List selectBeans( String criteria , int limit ) {
    return dao.selectBeans( criteria , limit );
  }

  public List selectActiveBeansByListProviderIds( int limitRecords ,
      List listProviderIds ) {
    return dao.selectActiveBeansByListProviderIds( limitRecords ,
        listProviderIds );
  }

  public List selectEventIds() {
    return dao.selectEventIds();
  }

  public long totalRecords() {
    return dao.totalRecords( null );
  }

  public long totalRecordsByEventId( int eventId ) {
    return dao.totalRecords( "( event_id = "
        .concat( Integer.toString( eventId ) ).concat( " ) " ) );
  }

  public long totalRecordsByChannelSessionId( int channelSessionId ) {
    return dao.totalRecords( "( channel_session_id = ".concat(
        Integer.toString( channelSessionId ) ).concat( " ) " ) );
  }

  public long totalRecordsByListProviderIdsDateSendNow(
      List listActiveProviderIds ) {
    long totalRecords = 0;
    if ( ( listActiveProviderIds == null )
        || ( listActiveProviderIds.size() < 1 ) ) {
      return totalRecords;
    }
    String sqlCriteriaProviderIds = "( provider IN ('"
        + StringUtils.join( listActiveProviderIds , "','" ) + "') ) ";
    String sqlCriteriaDateSendNow = "AND ( ( date_send IS NULL ) "
        + "OR ( date_send <= NOW() ) ) ";
    String sqlCriteria = sqlCriteriaProviderIds + sqlCriteriaDateSendNow;
    return dao.totalRecords( sqlCriteria );
  }

  public long totalRecords( String criteria ) {
    return dao.totalRecords( criteria );
  }

  public boolean update( SendBufferBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to update send buffer bean "
          + ", found null bean" );
      return result;
    }
    if ( bean.getSendId() < 1 ) {
      DLog.warning( lctx , "Failed to update send buffer bean "
          + ", found zero send id" );
    }
    result = dao.update( bean );
    return result;
  }

  public int deleteBeans( List listSendBufferBeans ) {
    int totalRecords = 0;
    if ( listSendBufferBeans == null ) {
      DLog.warning( lctx , "Failed to delete beans "
          + ", found null list beans" );
      return totalRecords;
    }
    if ( listSendBufferBeans.size() < 1 ) {
      DLog.warning( lctx , "Failed to delete beans "
          + ", found empty list beans" );
      return totalRecords;
    }
    totalRecords = dao.deleteBeans( listSendBufferBeans );
    if ( totalRecords < 1 ) {
      return totalRecords;
    }
    DLog.debug( lctx , "Successfully deleted beans , effected = "
        + totalRecords + " record(s)" );
    return totalRecords;
  }

  public boolean delete( SendBufferBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to delete send buffer bean "
          + ", found null bean" );
      return result;
    }
    int sendId = bean.getSendId();
    if ( sendId < 1 ) {
      DLog.warning( lctx , "Failed to delete send buffer bean "
          + ", found zero send id" );
      return result;
    }
    result = dao.delete( sendId );
    return result;
  }

  public boolean suspend( int eventId ) {
    boolean result = false;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to suspend send buffer beans "
          + ", found zero event id" );
      return result;
    }
    result = dao.suspend( eventId );
    return result;
  }

  public boolean resume( int eventId ) {
    boolean result = false;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to resume send buffer beans "
          + ", found zero event id" );
      return result;
    }
    result = dao.resume( eventId );
    return result;
  }

}
