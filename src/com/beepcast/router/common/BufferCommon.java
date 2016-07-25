package com.beepcast.router.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class BufferCommon {

  static final DLogContext lctx = new SimpleContext( "BufferCommon" );

  private static String DELIMITER_FIELD_FIELD = ";;";
  private static String DELIMITER_FIELD_VALUE = "==";

  public static String convertMapParamsToStr( Map mapParams ) {
    String str = "";

    if ( mapParams == null ) {
      return str;
    }

    StringBuffer sb = null;

    Set keySet = mapParams.keySet();
    Iterator keyIter = keySet.iterator();
    while ( keyIter.hasNext() ) {
      String key = (String) keyIter.next();
      if ( StringUtils.isBlank( key ) ) {
        continue;
      }
      Object objValue = mapParams.get( key );
      if ( objValue == null ) {
        continue;
      }
      if ( !( objValue instanceof String ) ) {
        continue;
      }
      String strValue = (String) objValue;
      if ( StringUtils.isBlank( strValue ) ) {
        continue;
      }
      if ( sb == null ) {
        sb = new StringBuffer();
      } else {
        sb.append( DELIMITER_FIELD_FIELD );
      }
      sb.append( key );
      sb.append( DELIMITER_FIELD_VALUE );
      sb.append( strValue );
    }

    if ( sb == null ) {
      return str;
    }

    str = sb.toString();

    return str;
  }

  public static Map convertStrParamsToMap( String strParams ) {
    Map map = new HashMap();

    if ( StringUtils.isBlank( strParams ) ) {
      return map;
    }

    String[] arr = StringUtils.split( strParams , DELIMITER_FIELD_FIELD );
    if ( arr == null ) {
      return map;
    }
    for ( int i = 0 ; i < arr.length ; i++ ) {
      String str = arr[i];
      if ( StringUtils.isBlank( str ) ) {
        continue;
      }
      String[] fv = StringUtils.split( str , DELIMITER_FIELD_VALUE );
      if ( ( fv == null ) || ( fv.length < 2 ) ) {
        continue;
      }
      String key = StringUtils.trimToNull( fv[0] );
      String value = StringUtils.trimToNull( fv[1] );
      if ( ( key == null ) || ( value == null ) ) {
        continue;
      }
      map.put( key , value );
    }

    return map;
  }

}
