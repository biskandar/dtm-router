package com.beepcast.router.mo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.dbmanager.util.DateTimeFormat;

public class MoBufferBean {

  public static final String HDRMAPEXTPARAM_SENDDATESTR = "SendDateStr";
  public static final String HDRMAPEXTPARAM_ORIMASKADDR = "OriMaskAddr";
  public static final String HDRMAPEXTPARAM_PREFRESDVAR = "PrefResdVar.";

  private int moId;
  private int trnprcType;
  private int clientId;
  private int eventId;
  private String eventCode;
  private String messageId;
  private String messageType;
  private String sourceNode;
  private String provider;
  private String phone;
  private String message;
  private String fixedMessage;
  private String senderId;
  private Map mapExternalParams;
  private Date sendDate;
  private boolean simulation;
  private Date dateInserted;

  public MoBufferBean() {
    mapExternalParams = new HashMap();
  }

  public int getMoId() {
    return moId;
  }

  public void setMoId( int moId ) {
    this.moId = moId;
  }

  public int getTrnprcType() {
    return trnprcType;
  }

  public void setTrnprcType( int trnprcType ) {
    this.trnprcType = trnprcType;
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

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId( String messageId ) {
    this.messageId = messageId;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType( String messageType ) {
    this.messageType = messageType;
  }

  public String getSourceNode() {
    return sourceNode;
  }

  public void setSourceNode( String sourceNode ) {
    this.sourceNode = sourceNode;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone( String phone ) {
    this.phone = phone;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getFixedMessage() {
    return fixedMessage;
  }

  public void setFixedMessage( String fixedMessage ) {
    this.fixedMessage = fixedMessage;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId( String senderId ) {
    this.senderId = senderId;
  }

  public Map getMapExternalParams() {
    return mapExternalParams;
  }

  public void setMapExternalParams( Map mapExternalParams ) {
    this.mapExternalParams = mapExternalParams;
  }

  public Date getSendDate() {
    return sendDate;
  }

  public void setSendDate( Date sendDate ) {
    this.sendDate = sendDate;
  }

  public boolean isSimulation() {
    return simulation;
  }

  public void setSimulation( boolean simulation ) {
    this.simulation = simulation;
  }

  public Date getDateInserted() {
    return dateInserted;
  }

  public void setDateInserted( Date dateInserted ) {
    this.dateInserted = dateInserted;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "MoBufferBean ( " + "moId = " + this.moId + TAB
        + "trnprcType = " + this.trnprcType + TAB + "clientId = "
        + this.clientId + TAB + "eventId = " + this.eventId + TAB
        + "eventCode = " + this.eventCode + TAB + "messageId = "
        + this.messageId + TAB + "messageType = " + this.messageType + TAB
        + "sourceNode = " + this.sourceNode + TAB + "provider = "
        + this.provider + TAB + "phone = " + this.phone + TAB + "message = "
        + StringEscapeUtils.escapeJava( this.message ) + TAB
        + "fixedMessage = " + StringEscapeUtils.escapeJava( this.fixedMessage )
        + TAB + "senderId = " + this.senderId + TAB + "mapExternalParams = "
        + this.mapExternalParams + TAB + "sendDate = "
        + DateTimeFormat.convertToString( this.sendDate ) + TAB
        + "simulation = " + this.simulation + TAB + "dateInserted = "
        + DateTimeFormat.convertToString( this.dateInserted ) + TAB + " )";
    return retValue;
  }

}
