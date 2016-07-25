package com.beepcast.router.dr;

import java.util.Date;
import java.util.Map;

public class DrBufferBeanFactory {

  public static DrBufferBean createDrBufferBean( String providerId ,
      String messageId , String externalStatus , Date externalStatusDate ,
      String internalStatus , int priority , String description ,
      Map internalMapParams , String externalParams , int retry ) {
    DrBufferBean bean = new DrBufferBean();
    bean.setProviderId( providerId );
    bean.setMessageId( messageId );
    bean.setExternalStatus( externalStatus );
    bean.setExternalStatusDate( externalStatusDate );
    bean.setInternalStatus( internalStatus );
    bean.setPriority( priority );
    bean.setDescription( description );
    if ( internalMapParams != null ) {
      bean.getInternalMapParams().putAll( internalMapParams );
    }
    bean.setExternalParams( externalParams );
    bean.setRetry( retry );
    return bean;
  }

}
