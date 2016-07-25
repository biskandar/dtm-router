package com.beepcast.router.mt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.beepcast.dbmanager.util.DateTimeFormat;

public class SendBufferBean {

  private int sendId;
  private String messageId;
  private int messageType;
  private int messageCount;
  private String messageContent;
  private double debitAmount;
  private String phoneNumber;
  private String provider;
  private int eventId;
  private int channelSessionId;
  private String senderId;
  private int priority;
  private boolean suspended;
  private boolean simulation;
  private int retry;
  private Map mapParams;
  private Date dateSend;

  public SendBufferBean() {
    mapParams = new HashMap();
  }

  public int getSendId() {
    return sendId;
  }

  public void setSendId( int sendId ) {
    this.sendId = sendId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId( String messageId ) {
    this.messageId = messageId;
  }

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType( int messageType ) {
    this.messageType = messageType;
  }

  public int getMessageCount() {
    return messageCount;
  }

  public void setMessageCount( int messageCount ) {
    this.messageCount = messageCount;
  }

  public String getMessageContent() {
    return messageContent;
  }

  public void setMessageContent( String messageContent ) {
    this.messageContent = messageContent;
  }

  public double getDebitAmount() {
    return debitAmount;
  }

  public void setDebitAmount( double debitAmount ) {
    this.debitAmount = debitAmount;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber( String phoneNumber ) {
    this.phoneNumber = phoneNumber;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId( int eventId ) {
    this.eventId = eventId;
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId( String senderId ) {
    this.senderId = senderId;
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

  public Map getMapParams() {
    return this.mapParams;
  }

  public void setMapParams( Map mapParams ) {
    this.mapParams = mapParams;
  }

  public void addParam( String key , String value ) {
    mapParams.put( key , value );
  }

  public String getParam( String key ) {
    return (String) mapParams.get( key );
  }

  public Date getDateSend() {
    return dateSend;
  }

  public void setDateSend( Date dateSend ) {
    this.dateSend = dateSend;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "SendBufferBean ( " + "sendId = " + this.sendId + TAB
        + "messageId = " + this.messageId + TAB + "messageType = "
        + this.messageType + TAB + "messageCount = " + this.messageCount + TAB
        + "debitAmount = " + this.debitAmount + TAB + "phoneNumber = "
        + this.phoneNumber + TAB + "provider = " + this.provider + TAB
        + "eventId = " + this.eventId + TAB + "channelSessionId = "
        + this.channelSessionId + TAB + "senderId = " + this.senderId + TAB
        + "priority = " + this.priority + TAB + "suspended = " + this.suspended
        + TAB + "simulation = " + this.simulation + TAB + "retry = "
        + this.retry + TAB + "dateSend = "
        + DateTimeFormat.convertToString( this.dateSend ) + TAB + " )";
    return retValue;
  }

}
