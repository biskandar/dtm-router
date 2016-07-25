package com.beepcast.router;

import com.beepcast.api.provider.data.ProviderMessage;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.msghistory.MessageProfile;
import com.beepcast.msghistory.MsgHistoryApp;
import com.beepcast.msghistory.query.NQMessageProfiles_MessageType;
import com.db4o.ObjectSet;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterBackupRestoreEngine {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "RouterBackupRestoreEngine" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private RouterApp routerApp;
  private MsgHistoryApp msgHistoryApp;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterBackupRestoreEngine( RouterApp routerApp ) {
    this.routerApp = routerApp;
    msgHistoryApp = MsgHistoryApp.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void restoreFromMsgHistory() {

    DLog.debug( lctx , "Trying to restore message(s) from msghistory" );

    String messageType = null;
    ObjectSet objectSet = null;
    MessageProfile messageProfile = null;
    ProviderMessage providerMessage = null;
    BoundedLinkedQueue objectQueue = null;
    boolean restore;

    // Restore DR Message
    messageType = "SMS.DR";
    objectQueue = routerApp.getDrQueue();
    objectSet = msgHistoryApp
        .getNAQMessageProfiles( new NQMessageProfiles_MessageType( messageType ) );
    if ( objectSet != null ) {
      DLog.debug( lctx , "Found " + objectSet.size()
          + " DR Message(s) ready to restore" );
      while ( objectSet.hasNext() ) {
        messageProfile = (MessageProfile) objectSet.next();
        if ( messageProfile != null ) {

          // delete history
          msgHistoryApp.deleteMessageProfile( messageProfile );

          // transform message
          providerMessage = new ProviderMessage();
          providerMessage.setMessageType( messageType );
          providerMessage.setRecordId( messageProfile.getRecordId() );
          providerMessage.setProviderId( messageProfile.getProviderId() );
          providerMessage.setExternalMessageId( messageProfile
              .getExternalMessageId() );
          providerMessage
              .setExternalStatus( messageProfile.getExternalStatus() );
          providerMessage
              .setInternalStatus( messageProfile.getInternalStatus() );
          providerMessage.setDescription( messageProfile.getDescription() );
          providerMessage
              .setStatusDateTime( messageProfile.getStatusDateTime() );

          // queued message
          try {
            restore = objectQueue.offer( providerMessage , 100 );
            if ( restore ) {
              DLog.debug(
                  lctx ,
                  "Successfully restore DR Message = "
                      + providerMessage.getRecordId()
                      + ","
                      + providerMessage.getProviderId()
                      + ","
                      + providerMessage.getExternalMessageId()
                      + ","
                      + providerMessage.getExternalStatus()
                      + ","
                      + providerMessage.getDescription()
                      + ","
                      + DateTimeFormat.convertToString( providerMessage
                          .getStatusDateTime() ) );
            }
          } catch ( InterruptedException e ) {
            DLog.warning( lctx , "Failed to put the DR Message in the queue , "
                + e );
            break;
          }

        }

      } // loop all message(s)
    }

  }

  public void backupToMsgHistory() {

    DLog.debug( lctx , "Trying to backup message(s) to msghistory" );

    BoundedLinkedQueue objectQueue = null;
    int sizeQueue = 0;
    Object dataQueue = null;
    ProviderMessage providerMessage = null;
    String messageType = null;
    MessageProfile messageProfile = null;
    boolean backup;

    // Backup DR Message
    messageType = "SMS.DR";
    objectQueue = routerApp.getDrQueue();
    if ( objectQueue != null ) {
      sizeQueue = objectQueue.size();
      DLog.debug( lctx , "Found " + sizeQueue
          + " DR Message(s) ready to backup" );
      while ( sizeQueue > 0 ) {

        providerMessage = null;
        try {
          // get message from the queue
          dataQueue = objectQueue.poll( 100 );
          if ( ( dataQueue != null ) && ( dataQueue instanceof ProviderMessage ) ) {
            providerMessage = (ProviderMessage) dataQueue;
          }
        } catch ( InterruptedException e ) {
          DLog.warning( lctx , "Failed to take DR Message from the queue , "
              + e );
          break;
        }

        if ( providerMessage != null ) {

          // transform message
          messageProfile = new MessageProfile();
          messageProfile.setMessageType( messageType );
          messageProfile.setRecordId( providerMessage.getRecordId() );
          messageProfile.setProviderId( providerMessage.getProviderId() );
          messageProfile.setExternalMessageId( providerMessage
              .getExternalMessageId() );
          messageProfile
              .setExternalStatus( providerMessage.getExternalStatus() );
          messageProfile
              .setInternalStatus( providerMessage.getInternalStatus() );
          messageProfile.setDescription( providerMessage.getDescription() );
          messageProfile
              .setStatusDateTime( providerMessage.getStatusDateTime() );

          // backup message
          backup = msgHistoryApp.storeMessageProfile( messageProfile );
          if ( backup ) {
            DLog.debug(
                lctx ,
                "Successfully backup DR Message = "
                    + messageProfile.getRecordId()
                    + ","
                    + messageProfile.getProviderId()
                    + ","
                    + messageProfile.getExternalMessageId()
                    + ","
                    + messageProfile.getExternalStatus()
                    + ","
                    + messageProfile.getDescription()
                    + ","
                    + DateTimeFormat.convertToString( messageProfile
                        .getStatusDateTime() ) );
          }

        }

        sizeQueue = sizeQueue - 1;
      } // loop all message(s)
    }

  }

}
