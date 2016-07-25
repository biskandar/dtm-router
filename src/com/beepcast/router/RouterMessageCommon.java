package com.beepcast.router;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterMessageCommon {

  static final DLogContext lctx = new SimpleContext( "RouterMessageCommon" );

  private static final String INT_PARAM_HDR_BUFFER_RETRY = "bufferRetry";

  public static String headerLog( RouterMessage routerMessage ) {
    String headerLog = "";
    if ( routerMessage == null ) {
      return headerLog;
    }
    return headerLog( routerMessage.getInternalMessageId() );
  }

  public static String headerLog( String messageId ) {
    String headerLog = "";
    if ( ( messageId == null ) || ( messageId.equals( "" ) ) ) {
      return headerLog;
    }
    headerLog = "[RouterMessage-" + messageId + "] ";
    return headerLog;
  }

  public static void debugLog( List listRouterMessages ) {
    if ( listRouterMessages == null ) {
      return;
    }
    Iterator iterRouterMessages = listRouterMessages.iterator();
    while ( iterRouterMessages.hasNext() ) {
      RouterMessage routerMessage = (RouterMessage) iterRouterMessages.next();
      if ( routerMessage == null ) {
        continue;
      }
      DLog.debug( lctx , "Debug " + routerMessage );
    }
  }

  public static boolean verifyRouterMTMessage( RouterMessage routerMTMessage ) {
    boolean result = false;

    if ( routerMTMessage == null ) {
      return result;
    }

    String headerLog = headerLog( routerMTMessage );

    if ( StringUtils.isBlank( routerMTMessage.getMessage() ) ) {
      DLog.warning( lctx , headerLog + "Failed to verify mt message "
          + ", found blank message content" );
      return result;
    }

    String routerMessageType = routerMTMessage.getMessageType();
    if ( routerMessageType == null ) {
      DLog.warning( lctx , headerLog + "Failed to verify mt message "
          + ", found blank router message type" );
      return result;
    }

    if ( ( routerMessageType.equalsIgnoreCase( RouterMessage.MSGTYPE_SMS ) )
        || ( routerMessageType.equalsIgnoreCase( RouterMessage.MSGTYPE_WAP ) )
        || ( routerMessageType.equalsIgnoreCase( RouterMessage.MSGTYPE_MMS ) ) ) {
      if ( StringUtils.isBlank( routerMTMessage.getDestinationAddress() ) ) {
        DLog.warning( lctx , headerLog + "Failed to verify mt message "
            + ", found blank destination address" );
        return result;
      }
      if ( StringUtils.isBlank( routerMTMessage.getOriginAddrMask() ) ) {
        DLog.warning( lctx , headerLog + "Failed to verify mt message "
            + ", found blank original address mask" );
        return result;
      }
    } else if ( routerMessageType.equalsIgnoreCase( RouterMessage.MSGTYPE_QR ) ) {
      // no need to validate destination or origin address mask
    } else if ( routerMessageType
        .equalsIgnoreCase( RouterMessage.MSGTYPE_WEBHOOK ) ) {
      // no need to validate destination or origin address mask
    } else {
      // nothing to do yet
    }

    if ( StringUtils.isBlank( routerMTMessage.getProviderId() ) ) {
      DLog.warning( lctx , headerLog + "Failed to verify mt message "
          + ", found blank provider id" );
      return result;
    }

    if ( routerMTMessage.getEventId() < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to verify mt message "
          + ", found zero event id" );
      return result;
    }

    if ( routerMTMessage.getSubmitDateTime() == null ) {
      DLog.debug( lctx , headerLog + "Found null submit date time "
          + ", meant it will send straight away" );
    }

    result = true;
    return result;
  }

  public static int getBufferRetryFromInternalMapParams(
      RouterMessage routerMessage , int defaultBufferRetry ) {
    int bufferRetry = defaultBufferRetry;
    try {
      bufferRetry = Integer.parseInt( (String) routerMessage
          .getInternalMapParams().get( INT_PARAM_HDR_BUFFER_RETRY ) );
    } catch ( Exception e ) {
    }
    return bufferRetry;
  }

  public static boolean setBufferRetryFromInternalMapParams(
      RouterMessage routerMessage , int bufferRetry ) {
    boolean updated = false;
    try {
      routerMessage.getInternalMapParams().put( INT_PARAM_HDR_BUFFER_RETRY ,
          Integer.toString( bufferRetry ) );
      updated = true;
    } catch ( Exception e ) {
    }
    return updated;
  }

}
