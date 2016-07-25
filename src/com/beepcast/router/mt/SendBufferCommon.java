package com.beepcast.router.mt;

import java.util.Iterator;
import java.util.List;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class SendBufferCommon {

  static final DLogContext lctx = new SimpleContext( "SendBufferCommon" );

  public static void debugLog( List listSendBufferBeans ) {
    if ( listSendBufferBeans == null ) {
      return;
    }
    SendBufferBean sendBufferBean = null;
    Iterator iterSendBufferBeans = listSendBufferBeans.iterator();
    while ( iterSendBufferBeans.hasNext() ) {
      sendBufferBean = (SendBufferBean) iterSendBufferBeans.next();
      DLog.debug( lctx , "Debug sendBufferBean : " + sendBufferBean );
    }
  }

}
