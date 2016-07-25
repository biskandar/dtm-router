package com.beepcast.router;

import java.util.Date;
import java.util.Map;

public interface RouterApi {

  public long totalSizeMoBuffer();

  public long totalSizeMtBuffer();

  public long totalSizeDrBuffer();

  public boolean refreshListProviderIds();

  public boolean procMoMessage( int transProcType , int clientId , int eventId ,
      String eventCode , String messageId , String messageType , String origin ,
      String providerId , String originAddress , String messageContent ,
      String messageReply , String destinationAddress , Map externalMapParams ,
      Date deliverDateTime );

  public boolean sendMtMessage( String messageId , int messageType ,
      int messageCount , String messageContent , double debitAmount ,
      String phoneNumber , String providerId , int eventId ,
      int channelSessionId , String senderId , int priority , int retry ,
      Date dateSend , Map params );

}
