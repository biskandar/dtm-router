package com.beepcast.router.common;

import java.util.Iterator;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class BufferQuery {

  static final DLogContext lctx = new SimpleContext( "BufferQuery" );

  private static DatabaseLibrary dbLib = DatabaseLibrary.getInstance();

  public static long getFirstValueLong( String sql , long defaultValue ) {
    long longValue = defaultValue;
    String strValue = getFirstValueStr( sql , null );
    if ( ( strValue != null ) && ( !strValue.equals( "" ) ) ) {
      try {
        longValue = Long.parseLong( strValue );
      } catch ( NumberFormatException e ) {
      }
    }
    return longValue;
  }

  public static String getFirstValueStr( String sql , String defaultValue ) {
    String strValue = defaultValue;
    if ( sql == null ) {
      return strValue;
    }
    // DLog.debug( lctx , "Performed " + sql );
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return strValue;
    }
    Iterator it = qr.iterator();
    if ( !it.hasNext() ) {
      return strValue;
    }
    QueryItem qi = (QueryItem) it.next();
    if ( qi == null ) {
      return strValue;
    }
    strValue = qi.getFirstValue();
    return strValue;
  }

}
