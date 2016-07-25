package com.beepcast.router.dr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

public class DrBufferBean {

  private int dnId;
  private String providerId;
  private String messageId;
  private String externalStatus;
  private Date externalStatusDate;
  private String internalStatus;
  private int priority;
  private String description;
  private Map internalMapParams;
  private String externalParams;
  private int retry;

  public DrBufferBean() {
    internalMapParams = new HashMap();
  }

  public int getDnId() {
    return dnId;
  }

  public void setDnId( int dnId ) {
    this.dnId = dnId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId( String providerId ) {
    this.providerId = providerId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId( String messageId ) {
    this.messageId = messageId;
  }

  public String getExternalStatus() {
    return externalStatus;
  }

  public void setExternalStatus( String externalStatus ) {
    this.externalStatus = externalStatus;
  }

  public Date getExternalStatusDate() {
    return externalStatusDate;
  }

  public void setExternalStatusDate( Date externalStatusDate ) {
    this.externalStatusDate = externalStatusDate;
  }

  public String getInternalStatus() {
    return internalStatus;
  }

  public void setInternalStatus( String internalStatus ) {
    this.internalStatus = internalStatus;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority( int priority ) {
    this.priority = priority;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public Map getInternalMapParams() {
    return internalMapParams;
  }

  public String getExternalParams() {
    return externalParams;
  }

  public void setExternalParams( String externalParams ) {
    this.externalParams = externalParams;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry( int retry ) {
    this.retry = retry;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "DrBufferBean ( " + "dnId = " + this.dnId + TAB
        + "providerId = " + this.providerId + TAB + "messageId = "
        + this.messageId + TAB + "externalStatus = " + this.externalStatus
        + TAB + "externalStatusDate = " + this.externalStatusDate + TAB
        + "internalStatus = " + this.internalStatus + TAB + "priority = "
        + this.priority + TAB + "description = " + this.description + TAB
        + "internalMapParams = " + this.internalMapParams + TAB
        + "externalParams = "
        + StringEscapeUtils.escapeJava( this.externalParams ) + TAB
        + "retry = " + this.retry + TAB + " )";
    return retValue;
  }

}
