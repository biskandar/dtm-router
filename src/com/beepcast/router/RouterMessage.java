package com.beepcast.router;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.beepcast.dbmanager.util.DateTimeFormat;

public class RouterMessage {

  public static final String MSGDIR_MO = "MO";
  public static final String MSGDIR_MT = "MT";
  public static final String MSGDIR_DR = "DR";

  public static final String MSGTYPE_SMS = "SMS";
  public static final String MSGTYPE_MMS = "MMS";
  public static final String MSGTYPE_WAP = "WAP";
  public static final String MSGTYPE_QR = "QR";
  public static final String MSGTYPE_WEBHOOK = "WEBHOOK";

  public static final int CONTENTTYPE_SMSTEXT = 0;
  public static final int CONTENTTYPE_SMSBINARY = 1;
  public static final int CONTENTTYPE_SMSUNICODE = 2;
  public static final int CONTENTTYPE_MMS = 3;
  public static final int CONTENTTYPE_WAPPUSH = 4;
  public static final int CONTENTTYPE_QRPNG = 5;
  public static final int CONTENTTYPE_QRGIF = 6;
  public static final int CONTENTTYPE_QRJPG = 7;
  public static final int CONTENTTYPE_WEBHOOK = 8;

  private String recordId;
  private int clientId;
  private int eventId;
  private String eventCode;
  private int channelSessionId;
  private String providerId;
  private String messageDirection;
  private String messageType;
  private int contentType;
  private int transProcType;
  private String internalMessageId;
  private String externalMessageId;
  private String destinationAddress;
  private String originAddress;
  private String originAddrMask;
  private String destination;
  private String origin;
  private int messageCount;
  private String message;
  private double debitAmount;
  private String replyMessage;
  private String internalStatus;
  private String externalStatus;
  private int priority;
  private boolean suspended;
  private boolean simulation;
  private int retry;
  private String description;

  private String externalStrParams;
  private Map externalMapParams;

  private String internalStrParams;
  private Map internalMapParams;

  private Date deliverDateTime;
  private Date submitDateTime;
  private Date statusDateTime;

  private Date createDateTime;

  public RouterMessage() {

    externalMapParams = new HashMap();
    internalMapParams = new HashMap();

    deliverDateTime = new Date();
    submitDateTime = new Date();
    statusDateTime = new Date();

    createDateTime = new Date();
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId( String recordId ) {
    this.recordId = recordId;
  }

  public int getClientId() {
    return clientId;
  }

  public void setClientId( int clientId ) {
    this.clientId = clientId;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId( int eventId ) {
    this.eventId = eventId;
  }

  public String getEventCode() {
    return eventCode;
  }

  public void setEventCode( String eventCode ) {
    this.eventCode = eventCode;
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId( String providerId ) {
    this.providerId = providerId;
  }

  public String getMessageDirection() {
    return messageDirection;
  }

  public void setMessageDirection( String messageDirection ) {
    this.messageDirection = messageDirection;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType( String messageType ) {
    this.messageType = messageType;
  }

  public int getContentType() {
    return contentType;
  }

  public void setContentType( int contentType ) {
    this.contentType = contentType;
  }

  public int getTransProcType() {
    return transProcType;
  }

  public void setTransProcType( int transProcType ) {
    this.transProcType = transProcType;
  }

  public String getInternalMessageId() {
    return internalMessageId;
  }

  public void setInternalMessageId( String internalMessageId ) {
    this.internalMessageId = internalMessageId;
  }

  public String getExternalMessageId() {
    return externalMessageId;
  }

  public void setExternalMessageId( String externalMessageId ) {
    this.externalMessageId = externalMessageId;
  }

  public String getDestinationAddress() {
    return destinationAddress;
  }

  public void setDestinationAddress( String destinationAddress ) {
    this.destinationAddress = destinationAddress;
  }

  public String getOriginAddress() {
    return originAddress;
  }

  public void setOriginAddress( String originAddress ) {
    this.originAddress = originAddress;
  }

  public String getOriginAddrMask() {
    return originAddrMask;
  }

  public void setOriginAddrMask( String originAddrMask ) {
    this.originAddrMask = originAddrMask;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination( String destination ) {
    this.destination = destination;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin( String origin ) {
    this.origin = origin;
  }

  public int getMessageCount() {
    return messageCount;
  }

  public void setMessageCount( int messageCount ) {
    this.messageCount = messageCount;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public double getDebitAmount() {
    return debitAmount;
  }

  public void setDebitAmount( double debitAmount ) {
    this.debitAmount = debitAmount;
  }

  public String getReplyMessage() {
    return replyMessage;
  }

  public void setReplyMessage( String replyMessage ) {
    this.replyMessage = replyMessage;
  }

  public String getInternalStatus() {
    return internalStatus;
  }

  public void setInternalStatus( String internalStatus ) {
    this.internalStatus = internalStatus;
  }

  public String getExternalStatus() {
    return externalStatus;
  }

  public void setExternalStatus( String externalStatus ) {
    this.externalStatus = externalStatus;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority( int priority ) {
    this.priority = priority;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public void setSuspended( boolean suspended ) {
    this.suspended = suspended;
  }

  public boolean isSimulation() {
    return simulation;
  }

  public void setSimulation( boolean simulation ) {
    this.simulation = simulation;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry( int retry ) {
    this.retry = retry;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getExternalStrParams() {
    return externalStrParams;
  }

  public void setExternalStrParams( String externalStrParams ) {
    this.externalStrParams = externalStrParams;
  }

  public Map getExternalMapParams() {
    return externalMapParams;
  }

  public String getInternalStrParams() {
    return internalStrParams;
  }

  public void setInternalStrParams( String internalStrParams ) {
    this.internalStrParams = internalStrParams;
  }

  public Map getInternalMapParams() {
    return internalMapParams;
  }

  public Date getDeliverDateTime() {
    return deliverDateTime;
  }

  public void setDeliverDateTime( Date deliverDateTime ) {
    this.deliverDateTime = deliverDateTime;
  }

  public Date getSubmitDateTime() {
    return submitDateTime;
  }

  public void setSubmitDateTime( Date submitDateTime ) {
    this.submitDateTime = submitDateTime;
  }

  public Date getStatusDateTime() {
    return statusDateTime;
  }

  public void setStatusDateTime( Date statusDateTime ) {
    this.statusDateTime = statusDateTime;
  }

  public Date getCreateDateTime() {
    return createDateTime;
  }

  public void setCreateDateTime( Date createDateTime ) {
    this.createDateTime = createDateTime;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "RouterMessage ( " + "recordId = " + this.recordId + TAB
        + "eventId = " + this.eventId + TAB + "channelSessionId = "
        + this.channelSessionId + TAB + "providerId = " + this.providerId + TAB
        + "messageDirection = " + this.messageDirection + TAB
        + "messageType = " + this.messageType + TAB + "contentType = "
        + this.contentType + TAB + "internalMessageId = "
        + this.internalMessageId + TAB + "externalMessageId = "
        + this.externalMessageId + TAB + "destinationAddress = "
        + this.destinationAddress + TAB + "originAddress = "
        + this.originAddress + TAB + "originAddrMask = " + this.originAddrMask
        + TAB + "destination = " + this.destination + TAB + "origin = "
        + this.origin + TAB + "messageCount = " + this.messageCount + TAB
        + "debitAmount = " + this.debitAmount + TAB + "internalStatus = "
        + this.internalStatus + TAB + "externalStatus = " + this.externalStatus
        + TAB + "priority = " + this.priority + TAB + "suspended = "
        + this.suspended + TAB + "simulation = " + this.simulation + TAB
        + "retry = " + this.retry + TAB + "description = " + this.description
        + TAB + "deliverDateTime = "
        + DateTimeFormat.convertToString( this.deliverDateTime ) + TAB
        + "submitDateTime = "
        + DateTimeFormat.convertToString( this.submitDateTime ) + TAB
        + "statusDateTime = "
        + DateTimeFormat.convertToString( this.statusDateTime ) + TAB
        + "createDateTime = "
        + DateTimeFormat.convertToString( this.createDateTime ) + TAB + " )";
    return retValue;
  }

}
