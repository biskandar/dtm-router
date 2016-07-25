package com.beepcast.router.dr;

import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.router.common.BufferCommon;

public class DrBufferQuery {

  public static String sqlSelectFrom() {
    String sqlSelect = "SELECT dn_id , provider_id , message_id ";
    sqlSelect += ", external_status , external_status_date ";
    sqlSelect += ", internal_status , priority , description ";
    sqlSelect += ", internal_params , external_params , retry ";
    String sqlFrom = "FROM dn_buffer ";
    String sqlSelectFrom = sqlSelect + sqlFrom;
    return sqlSelectFrom;
  }

  public static DrBufferBean populateRecord( QueryItem qi ) {
    DrBufferBean bean = null;

    if ( qi == null ) {
      return bean;
    }

    String stemp = null;

    bean = new DrBufferBean();

    stemp = (String) qi.get( 0 ); // dn_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setDnId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // provider_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setProviderId( stemp );
    }

    stemp = (String) qi.get( 2 ); // message_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMessageId( stemp );
    }

    stemp = (String) qi.get( 3 ); // external_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setExternalStatus( stemp );
    }

    stemp = (String) qi.get( 4 ); // external_status_date
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setExternalStatusDate( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 5 ); // internal_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setInternalStatus( stemp );
    }

    stemp = (String) qi.get( 6 ); // priority
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setPriority( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 7 ); // description
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDescription( stemp );
    }

    stemp = (String) qi.get( 8 ); // internal_params
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.getInternalMapParams().putAll(
          BufferCommon.convertStrParamsToMap( stemp ) );
    }

    stemp = (String) qi.get( 9 ); // external_params
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setExternalParams( stemp );
    }

    stemp = (String) qi.get( 10 ); // retry
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setRetry( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    return bean;
  }

}
