# dtm-router
Directtomobile Router Module

v1.0.44

- Support for router content type "Webhook"

- Centralized jar libraries

- Add new library :
  . subscriber2beepcast-v1.2.41.jar
  . model2beepcast-v1.0.83.jar
  . client2beepcast-v2.3.10.jar
  . provider2beepcast-v2.2.32.jar
  . transaction2beepcast-v1.1.39.jar
  . channel2beepcast-v1.0.69.jar

v1.0.43

- Not to forward nya mt message that cames from MODEM , keep store into send buffer

    DEBUG   {RouterMOWorkerThread-0}RouterMTWorker [RouterMessage-MDM20234000] Send Mt Message : messageId = MDM20234000 , gatewayMessageType = 0 , messageCount = 1 , messageContent = Test Incoming Basic Modem Event 01 , debitAmount = 1.0 , phoneNumber = +6598394294 , providerId = SINGTELMDM01 , eventId = 179 , channelSessionId = 0 , senderId = 90102337 , priority = 5 , retry = 0 , dateSend = null , mapParams = {oriProvider=STARHUBMDM01}
    DEBUG   {RouterMOWorkerThread-0}RouterMessageFactory [RouterMessage-MDM20234000] Resolved message / content type : routerMessageType = SMS , routerContentType = 0 , gatewayMessageType = 0
    DEBUG   {RouterMOWorkerThread-0}RouterMessageCommon [RouterMessage-MDM20234000] Found null submit date time , meant it will send straight away
    DEBUG   {RouterMOWorkerThread-0}LoadProfiles [LoadProfile-ROUTER_SMSMT_IN] Stored profile object in the map
    DEBUG   {RouterMOWorkerThread-0}RouterMTWorker [RouterMessage-MDM20234000] Found mt message provider id SINGTELMDM01 is  in the valid list
    DEBUG   {RouterMOWorkerThread-0}RouterMTWorker [RouterMessage-MDM20234000] Stored the mt message into input output queue , takes 1 ms

    DEBUG   {RouterMTManagementWorker}RouterMTWorker Read total 1 send buffer bean(s) ready to insert into send buffer table : MDM20234000,
    DEBUG   {RouterMTManagementWorker}SendBufferDAO Prepared batch send buffer record ( 0 ) : [MDM20234000, 0, 1, Test Incoming Basic Modem Event 01, 1.0, +6598394294, SINGTELMDM01, 179, 0, 90102337, 5, 0, 0, 0, oriProvider==STARHUBMDM01;;gatewayMessageType==0, 2016-04-15 10:54:24]
    DEBUG   {RouterMTManagementWorker}DatabaseEngineExecuteBatchStatement [Db-transactiondb] [Retry-0] Populated batches , total effected : 1 -> 1 record(s)
    DEBUG   {RouterMTManagementWorker}DatabaseEngineExecuteBatchStatement [Db-transactiondb] [Retry-0] Executed batches , total result : 1 record(s)
    DEBUG   {RouterMTManagementWorker}DatabaseEngineExecuteBatchStatement [Db-transactiondb] [Retry-0] Performed to execute batch records , with : updated = 1 record(s) , emptied = 0 record(s) , failed = 0 record(s)
    DEBUG   {RouterMTManagementWorker}SendBufferDAO Inserted into send buffer table total = 1 record(s) , and list of index record failed : 

    DEBUG   {RouterMTDbOuWorker}SendBufferDAO Performed ( 
      SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params 
        FROM send_buffer 
        WHERE ( suspended = 0 ) 
          AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) 
          AND ( provider = 'SINGTELMDM01' ) 
        ORDER BY priority DESC , send_id ASC LIMIT 100 ) 
      UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'QR' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'XX1' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'CLIENT_API' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'EE' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'EE4' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'EE3' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'EE2' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) UNION ALL ( SELECT send_id , event_id , channel_session_id , provider , date_tm , phone , message_id , message_type , message_count , message , debit_amount , priority , retry , senderID , params FROM send_buffer WHERE ( suspended = 0 ) AND ( ( date_send IS NULL ) OR ( date_send < NOW() ) ) AND ( provider = 'EE1' ) ORDER BY priority DESC , send_id ASC LIMIT 100 ) 
    DEBUG   {RouterMTDbOuWorker}SendBufferDAO Perform DELETE FROM send_buffer WHERE send_id IN ( 10 ) 
    DEBUG   {RouterMTDbOuWorker}SendBufferService Successfully deleted beans , effected = 1 record(s)
    
- Allow to process mo message with empty message when event id is exist

- Support for new mobufferbean header message params for reserved variables

- Add new library :
  . transaction2beepcast-v1.1.38.jar
  . provider2beepcast-v2.2.31.jar
  . client2beepcast-v2.3.08.jar
  . beepcast_loadmanagement-v1.2.05.jar
  . beepcast_session-v1.0.03.jar
  . beepcast_database-v1.2.02.jar
  . beepcast_properties-v2.0.01.jar

v1.0.42 

- Found bug to handle the database exception during database server restart

    Exception in thread "RouterMTDbOuWorker" 
    java.lang.NullPointerException
      at com.beepcast.database.DatabaseEngine.simpleQuery(DatabaseEngine.java:197)
      at com.beepcast.database.DatabaseLibrary.simpleQuery(DatabaseLibrary.java:105)
      at com.beepcast.router.mt.SendBufferDAO.populateRecords(SendBufferDAO.java:541)
      at com.beepcast.router.mt.SendBufferDAO.selectActiveBeansByListProviderIds(SendBufferDAO.java:310)
      at com.beepcast.router.mt.SendBufferService.selectActiveBeansByListProviderIds(SendBufferService.java:73)
      at com.beepcast.router.RouterMTWorker.retrieveRouterMTMessagesFromTable(RouterMTWorker.java:618)
      at com.beepcast.router.RouterMTWorker.access$8(RouterMTWorker.java:605)
      at com.beepcast.router.RouterMTWorker$RouterMTDbOuWorker.run(RouterMTWorker.java:822)
      
    Exception in thread "RouterDRDBWorkerThread" 
    java.lang.NullPointerException
      at com.beepcast.database.DatabaseEngine.simpleQuery(DatabaseEngine.java:197)
      at com.beepcast.database.DatabaseLibrary.simpleQuery(DatabaseLibrary.java:105)
      at com.beepcast.router.dr.DrBuffersDAO.select(DrBuffersDAO.java:186)
      at com.beepcast.router.dr.DrBuffersService.select(DrBuffersService.java:65)
      at com.beepcast.router.RouterDRWorker.moveDrRecordsFromDnBufferToDrQueue(RouterDRWorker.java:262)
      at com.beepcast.router.RouterDRWorker.access$2(RouterDRWorker.java:255)
      at com.beepcast.router.RouterDRWorker$RouterDRDBWorkerThread.run(RouterDRWorker.java:453)

- Add new library :
  . subscriber2beepcast-v1.2.40.jar
  . beepcast_database-v1.2.01.jar

  . transaction2beepcast-v1.1.37.jar
  . mysql-connector-java-5.1.37-bin.jar
  . channel2beepcast-v1.0.68.jar
  . provider2beepcast-v2.2.29.jar  
  
v1.0.41

- Found delay during update the invalid list 

    04.12.2015 14.41.36:075 4024 DEBUG   {RouterDRWorkerThread-5}End2endBroker [ProviderMessage-INT11331295] 
      Updating mobile user : clientId = 1 , phoneNumber = +6511000001 , mobileCcnc = 51025F
    04.12.2015 14.41.36:785 4025 WARNING {RouterDRWorkerThread-0}DatabaseEngine [profiledb] 
      Found delayed for query UPDATE client_subscriber SET global_invalid = 0 , date_global_invalid = NOW() 
        WHERE ( client_id = 1 ) AND ( encrypt_phone = AES_ENCRYPT('+6511000001','jkiZmu0s575xFbgGFkdQ') )  , in 7562 ms.
    04.12.2015 14.41.36:785 4026 DEBUG   {RouterDRWorkerThread-0}ClientSubscriberService [Client-1] [MSISDN-+6511000001] 
      Updated client subscriber global invalid flag set to false , effected = 100000 record(s)
    04.12.2015 14.41.36:785 4027 WARNING {RouterDRWorkerThread-0}ClientSubscriberService [Client-1] [MSISDN-+6511000001] 
      Failed to perform update valid number , found can not delete phone number from the global invalid list table
    04.12.2015 14.41.36:786 4028 DEBUG   {RouterDRWorkerThread-0}RouterToClient [RouterMessage-INT11331266] 
      Stored dr message into client msg api , result = NOT_SUPPORT

  The delay is found when there is a lot of transaction with same number , the innodb lock per record
      
- Found bug the router dr db mover doesn't run while there is records need to process

    DEBUG   {RouterManagementThread}RouterDRWorker Refreshed : dbWorkerBusy = false , dbWorkerRun = true 
    , totalWorkers = 20 : totalProcessingDrMessages = 1705 , totalProcessingMtMessages = 0 , isBroadcastRunning = false

    [beepcast@aw8 beepadmin-prod]$ grep "RouterDRDBWorker" ./log/beepadmin-20151116-*.log
    ./log/beepadmin-20151116-2.log:16.11.2015 09.35.26:972 477306 ERROR   
      {RouterDRDBWorkerThread}DatabaseEngine [profiledb] Database query failed 
        , com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
    
- 
    
v1.0.40

- Found bugs failed to do the batch 

    DEBUG   {RouterMTManagementWorker}SendBufferBeanTransformUtils [RouterMessage-INT9123713] 
      Transforming router mt message to send buffer bean : messageType = 11 , messageCount = 1 
      , debitAmount = 0.0 , providerId = QR , eventId = 189 , channelSessionId = 192 
      , priority = 1 , retry = 0 , dateSend = null 
      , mapParams = {qrImageFileSize=128, gatewayMessageType=11, gatewayXipmeId=CLG13402182
      , qrImageFileName=CO5_00068837.PNG}
    DEBUG   {RouterMTManagementWorker}RouterMTWorker Read total 26 send buffer bean(s) ready to 
      insert into send buffer table : INT9123713,INT9123718,INT9123717,INT9123719,INT9123722
      ,INT9123723,INT9123727,INT9123726,INT9123728,INT9123729,INT9123732,INT9123734,INT9123733
    WARNING {RouterMTManagementWorker}DatabaseEngine [transactiondb] Failed to add batch parameter 
      , java.sql.SQLException: No value specified for parameter 10
    WARNING {RouterMTManagementWorker}DatabaseEngine [transactiondb] Failed to add batch parameter 
      , java.sql.SQLException: No value specified for parameter 10

- Found bug for qr images ( not require any original address ) :

    WARNING {RouterMOWorkerThread-0}RouterMessageCommon [RouterMessage-MDM20173000] 
      Failed to verify mt message , found blank original address mask
    WARNING {RouterMOWorkerThread-0}RouterMTWorker [RouterMessage-MDM20173000] 
      Failed to send mt message , found invalid parameters inside
    WARNING {RouterMOWorkerThread-0}Transaction [MDM20173000] Failed to insert an outgoing message 
      into send buffer , found failed send mt message thru router app
    WARNING {RouterMOWorkerThread-0}Transaction [MDM20173000] [OutMsg-0] Failed to finalized , 
      found failed to insert outgoing message into send buffer

- Use database server mysql 5.6 port 3307

- There is change in the provider message location in the provider module ,
  require to refactor , because some of the function require that one .

- Use JDK 1.7

- Add new library :

  . stax-api-1.0.1.jar
  . stax-1.2.0.jar
  . jettison-1.2.jar
  . xpp3_min-1.1.4c.jar
  . jdom-1.1.3.jar
  . dom4j-1.6.1.jar
  . xom-1.1.jar
  . cglib-nodep-2.2.jar
  . xmlpull-1.1.3.1.jar
  . xstream-1.4.8.jar

  . commons-logging-1.2.jar
  . commons-pool2-2.4.2.jar
  . commons-dbcp2-2.1.1.jar
  . mysql-connector-java-5.1.35-bin.jar
  . beepcast_database-v1.2.00.jar
  
  . beepcast_clientapi-v1.0.04.jar
  . xipme_api-v1.0.29.jar
  . model2beepcast-v1.0.82.jar
  . provider2beepcast-v2.2.28.jar
  . transaction2beepcast-v1.1.34.jar


v1.0.39

- Seems like the router dr worker effect busy / idle if there is on demand broadcast event ,
  this should be not effect because only for broadcast event push will push into idle dr worker

- Add new library :
  . lookup2beepcast-v1.0.00.jar
 
v1.0.38

- Move the refresh list provider ids in the router management , and it will
  call every time ( not based on idle situation )

- Change api to refresh the lists , add parameter additionActiveProviderIds

- Migrate all the existing projects to use IDE MyEclipse Pro 2014
  . Build as java application ( not as web project )

- Add new library :
  . 

v1.0.37

- Trap delivery report is based on the status date time

    12.06.2015 15.50.19:413 2057 DEBUG   {RouterDRWorkerThread-0}RouterDRWorkerThread [RouterMessage-MDM20130000] 
      Trapped a delivered status : NX1, MDM20130000 with total latency in 480 minute(s)

- Resolve new provider with transaction.destinationProviderSimulation function

- Add new library :
  . channel2beepcast-v1.0.65.jar
  . transaction2beepcast-v1.1.31.jar
  . provider2beepcast-v2.2.25.jar
  . client_request2beepcast-v1.0.05.jar
  . model2beepcast-v1.0.80.jar
  . subscriber2beepcast-v1.2.36.jar
  . dwh_model-v1.0.33.jar
  . beepcast_keyword-v1.0.07.jar
  . beepcast_dbmanager-v1.1.36.jar
  . beepcast_onm-v1.2.09.jar

v1.0.36

- Found java exception 

    java.lang.NullPointerException
      at com.beepcast.router.dr.ProcessRetryMessage.execute(ProcessRetryMessage.java:121)
      at com.beepcast.api.provider.MTRespWorker.processStatusMessages(MTRespWorker.java:438)
      at com.beepcast.api.provider.MTRespWorker.extractMTRespMessage(MTRespWorker.java:198)
      at com.beepcast.api.provider.MTRespWorker.clean(MTRespWorker.java:142)
      at com.beepcast.api.provider.ProviderManagement$ManagementThread.run(ProviderManagement.java:337)

    {ProviderManagementThread}ProcessRetryMessage [RouterMessage-MDM20072000] Retrying to send the message back : retry value = 0 , submit date time = Thu Jan 29 17:54:01 SGT 2015
    {ProviderManagementThread}RouterApp [RouterMessage-MDM20072000] Verified retry value : currentRetryValue = 0 is less than maximumRetyValue = 1
    {ProviderManagementThread}RouterApp [RouterMessage-MDM20072000] Read retry duration from  configuration : duration = 60000 ms
    {ProviderManagementThread}ProcessRetryMessage [RouterMessage-MDM20072000] Failed to re-send mt message , java.lang.NullPointerException
    {ProviderManagementThread}MTRespWorker [ProviderMessage-MDM20072000] Found retry flag inside provider mt message , will do re-send message : result = false

- Make sure all the refund gateway log debit amount is reflected in the records

- DrWorker management only works for current day send

- Add feature to shutdown , retry , and ignored delivery report message

- Add new library :
  . provider2beepcast-v2.2.24.jar
  . transaction2beepcast-v1.1.28.jar
  . model2beepcast-v1.0.76.jar
  . beepcast_dbmanager-v1.1.35.jar

v1.0.35

- There is a change inside ./conf/onm.xml
        
        <module type="queue" name="queueRouter.mo" />
        <module type="queue" name="queueRouter.mt.in" />
        <module type="queue" name="queueRouter.mt.ou" />
        <module type="queue" name="queueRouter.dr" />

- Found bug , keep the priority value consistent ( found the read send_buffer dao bypass priority field )

    ./log/beepadmin-20141204-32.log:04.12.2014 13.55.59:927 17804724 DEBUG   
      {ChannelProcessorThread-13}SendBufferBeanTransformUtils [RouterMessage-INT2186368] 
        Transforming router mt message to send buffer bean : messageType = 0 , messageCount = 1 , debitAmount = 1.0 , providerId = EE4 , eventId = 96 , channelSessionId = 122 
        , priority = 1 , retry = 0 , dateSend = null , mapParams = {gatewayMessageType=0}
    ./log/beepadmin-20141204-32.log:04.12.2014 13.55.59:928 17804787 DEBUG   
      {ChannelProcessorThread-13}RouterMTWorker Read total 288 send buffer bean(s) ready to insert into send buffer table : (cut)
    ./log/beepadmin-20141204-34.log:04.12.2014 13.59.16:506 19263303 DEBUG   
      {RouterMTDbOuWorker}SendBufferBeanTransformUtils [RouterMessage-INT2186368] 
        Transforming send buffer bean to router mt message : gatewayMessageType = 0 , messageCount = 1 , debitAmount = 1.0 , providerId = EE4 , eventId = 96 , channelSessionId = 122 
        , priority = 0 , retry = 0 , dateSend = Thu Dec 04 13:55:59 SGT 2014 , internalMapParams = {gatewayMessageType=0}
    ./log/beepadmin-20141204-34.log:04.12.2014 13.59.19:146 19283543 DEBUG   
      {RouterMTDbOuWorker}ProviderApp [ProviderMessage-INT2186368] Send message: dstAddr = +6500087431, oriAddr = null, oriAddrMask = +6591093429, debitAmount = 1.0, cntType = 0, msgCount = 1, msgContent = Test Outgoing Basic Event 34 Code

- There is change inside ./conf/oproperties.xml

      <property field="Router.Management.sleepMillis" value="15000"
        description="Sleep time for router management worker" />
      <property field="Router.DRDBWorker.minSleepMillis" value="1000"
        description="Minimum sleep time for the dr db worker" />
      <property field="Router.DRDBWorker.maxSleepMillis" value="2000"
        description="Maximum sleep time for the dr db worker" />
      <property field="Router.DRDBWorker.limitRecords" value="50"
        description="The total records load from table into queue" />
      <property field="Router.DRDBWorker.workerBusyMtSizeThreshold" value="50"
        description="Need to busy dr db worker when there is many mt messages" />
      <property field="Router.DRDBWorker.workerBusyForRunningBroadcast" value="true"
        description="Need to busy dr db worker when there is broadcast running" />
      <property field="Router.DRDBWorker.workerSuspendMtSizeThreshold" value="50"
        description="Need to suspend dr db worker when there is many mt messages" />
      <property field="Router.DRDBWorker.workerSuspendForRunningBroadcast" value="true"
        description="Need to suspend dr db worker when there is broadcast running" />
      <property field="Router.DRWorker.workerSizeMin" value="1"
        description="Set how many minimum total dr worker run concurrently" />
      <property field="Router.DRWorker.workerSizeMax" value="5"
        description="Set how many maximum total dr worker run concurrently" />
      <property field="Router.DRWorker.workerSizeRefreshDrSizeThreshold" value="500"
        description="Set how many total dr worker based on dr buffer size" />
      <property field="Router.DRWorker.workerSizeRefreshMtSizeThreshold" value="50"
        description="Set how many total dr worker based on mt buffer size" />
      <property field="Router.DRWorker.workerSleepMillis" value="500"
        description="Set sleep for each dr worker before execute the next record" />
      <property field="Router.DRWorker.queueTimeoutMillis" value="5000"
        description="Set queue timeout for each dr worker" />
      <property field="Router.DRWorker.queueBatchThreshold" value="10"
        description="Set queue batch threshold inside dr worker" />

- Put intelligent in the dr worker to work harder when there is no
  broadcast , and it will slow down when there is a big broadcast .

- Add new library :
  . transaction2beepcast-v1.1.26.jar
  . provider2beepcast-v2.2.22.jar
  . client_request2beepcast-v1.0.04.jar
  . client2beepcast-v2.3.06.jar
  . model2beepcast-v1.0.74.jar
  . subscriber2beepcast-v1.2.35.jar
  . dwh_model-v1.0.31.jar
  . beepcast_online_properties-v1.0.05.jar
  . beepcast_dbmanager-v1.1.34.jar
  . beepcast_encrypt-v1.0.04.jar
  . channel2beepcast-v1.0.62.jar

v1.0.34

- Found bug java exception when there is no available providers

    java.lang.NullPointerException
      at com.beepcast.database.DatabaseEngine.simpleQuery(DatabaseEngine.java:197)
      at com.beepcast.database.DatabaseLibrary.simpleQuery(DatabaseLibrary.java:105)
      at com.beepcast.router.mt.SendBufferDAO.selectActiveBeansByListProviderIds(SendBufferDAO.java:320)
      at com.beepcast.router.mt.SendBufferService.selectActiveBeansByListProviderIds(SendBufferService.java:75)
      at com.beepcast.router.RouterMTWorker.retriveRouterMTMessagesFromTable(RouterMTWorker.java:613)
      at com.beepcast.router.RouterMTWorker.access$8(RouterMTWorker.java:600)
      at com.beepcast.router.RouterMTWorker$RouterMTDbOuWorker.run(RouterMTWorker.java:815)

- Add new library :
  . 

v1.0.33

- Added function to get totalRecords by eventId and by channelSessionId

- Add new library :
  . client2beepcast-v2.3.05.jar
  . model2beepcast-v1.0.70.jar
  . subscriber2beepcast-v1.2.33.jar
  . beepcast_session-v1.0.02.jar
  . beepcast_onm-v1.2.08.jar
  . 

v1.0.32

- Found bug when found no provider available ...

    java.lang.NullPointerException
      at com.beepcast.database.DatabaseEngine.simpleQuery(DatabaseEngine.java:197)
      at com.beepcast.database.DatabaseLibrary.simpleQuery(DatabaseLibrary.java:105)
      at com.beepcast.router.mt.SendBufferDAO.selectActiveBeansByListProviderIds(SendBufferDAO.java:320)
      at com.beepcast.router.mt.SendBufferService.selectActiveBeansByListProviderIds(SendBufferService.java:75)
      at com.beepcast.router.RouterMTWorker.retriveRouterMTMessagesFromTable(RouterMTWorker.java:613)
      at com.beepcast.router.RouterMTWorker.access$8(RouterMTWorker.java:600)
      at com.beepcast.router.RouterMTWorker$RouterMTDbOuWorker.run(RouterMTWorker.java:815)

- Add new library :
  . transaction2beepcast-v1.1.21.jar
  . model2beepcast-v1.0.69.jar
  . subscriber2beepcast-v1.2.31.jar
  . beepcast_loadmanagement-v1.2.04.jar
  . beepcast_encrypt-v1.0.03.jar

v1.0.31

- ...

- Add new library :
  . transaction2beepcast-v1.1.20.jar
  . provider2beepcast-v2.2.17.jar
  . subscriber2beepcast-v1.2.27.jar
  . client_request2beepcast-v1.0.03.jar
  . client2beepcast-v2.3.04.jar
  . billing2beepcast-v1.1.07.jar
  . model2beepcast-v1.0.68.jar
  . dwh_model-v1.0.29.jar
  . beepcast_onm-v1.2.07.jar

v1.0.30

- The retry mechanism must be updated from "delivery report status retryable" and/or "timeout connection to provider"

- Execute sql below :

    ALTER TABLE dn_buffer` 
      ADD COLUMN `internal_params` TEXT AFTER `description` ;

- There is a change in file ./conf/oproperties.xml

      <property field="Router.RetryMTBuffer" value="1"
        description="Total retries for unstored mt to the provider channel queue" />

- Found bug to save the unstored queue mt message into the send buffer table , please find log below :

    ../../log/beepadmin-20131111-52.log:11.11.2013 17.51.29:876 15939414 DEBUG   {ChannelProcessorThread-13}RouterMTWorker [RouterMessage-INT18849772] Send Mt Message : messageId = INT18849772 , gatewayMessageType = 0 , messageCount = 2 , messageContent = Test SG 100000 Numbers B CO4_00037411 CO5_00037411  http:\/\/xip.me\/Q0bnf 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 End , debitAmount = 2.0 , phoneNumber = +6500037411 , providerId = BC1 , eventId = 2559 , channelSessionId = 746 , senderId = +6591093429 , priority = 1 , retry = 0 , dateSend = null , mapParams = {}
    ../../log/beepadmin-20131111-52.log:11.11.2013 17.51.29:876 15939415 DEBUG   {ChannelProcessorThread-13}RouterMessageCommon [RouterMessage-INT18849772] Found null submit date time , meant it will send straight away
    ../../log/beepadmin-20131111-52.log:11.11.2013 17.51.29:876 15939416 DEBUG   {ChannelProcessorThread-13}RouterMTWorker [RouterMessage-INT18849772] Found process queue is full
    ../../log/beepadmin-20131111-52.log:11.11.2013 17.51.29:876 15939417 DEBUG   {ChannelProcessorThread-13}RouterMTWorker [RouterMessage-INT18849772] Stored the mt message into input batch queue
    ../../log/beepadmin-20131111-52.log:11.11.2013 17.51.30:378 15941699 DEBUG   {ChannelProcessorThread-13}Transaction [INT18849772] Successfully inserted an outgoing message into send buffer
    ../../log/beepadmin-20131111-52.log:11.11.2013 17.51.30:378 15941700 DEBUG   {ChannelProcessorThread-13}Transaction [INT18849772] [1] Successfully processed as normal message
    ../../log/beepadmin-20131111-52.log:11.11.2013 17.51.55:002 16051148 DEBUG   {ChannelProcessorThread-6}RouterMTWorker Read total 288 send buffer bean(s) ready to insert into send buffer table : INT18849677,INT18849678,INT18849680,INT18849679,INT18849681,INT18849682,INT18849683,INT18849684,INT18849685,INT18849686,INT18849687,INT18849688,INT18849689,INT18849690,INT18849698,INT18849699,INT18849702,INT18849703,INT18849705,INT18849704,INT18849715,INT18849763,INT18849764,INT18849765,INT18849766,INT18849767,INT18849769,INT18849770,INT18849768,INT18849771,INT18849774,INT18849772,INT18849773,INT18849775,INT18849776,INT18849777,INT18849778,INT18849779,INT18849780,INT18849781,INT18849783,INT18849784,INT18849785,INT18849786,INT18849795,INT18849796,INT18849799,INT18849800,INT18849826,INT18849827,INT18849822,INT18849829,INT18849830,INT18849832,INT18849833,INT18849835,INT18849836,INT18849837,INT18849838,INT18849840,INT18849841,INT18849842,INT18849843,INT18849844,INT18849845,INT18849848,INT18849849,INT18849850,INT18849851,INT18849852,INT18849853,INT18849854,INT18849855,INT18849856,INT18849857,INT18849858,INT18849861,INT18849863,INT18849866,INT18849867,INT18849870,INT18849871,INT18849872,INT18849882,INT18849883,INT18849885,INT18849886,INT18849889,INT18849890,INT18849891,INT18849892,INT18849893,INT18849894,INT18849895,INT18849896,INT18849897,INT18849898,INT18849899,INT18849900,INT18849901,INT18849902,INT18849908,INT18849907,INT18849909,INT18849910,INT18849912,INT18849942,INT18849943,INT18849944,INT18849946,INT18849947,INT18849948,INT18849958,INT18849960,INT18849976,INT18849978,INT18849985,INT18849986,INT18849988,INT18849989,INT18849990,INT18849991,INT18849993,INT18849994,INT18850004,INT18850005,INT18850006,INT18850008,INT18850017,INT18850018,INT18850020,INT18850021,INT18850022,INT18850023,INT18850032,INT18850034,INT18850035,INT18850036,INT18850037,INT18850047,INT18850048,INT18850050,INT18850051,INT18850052,INT18850053,INT18850064,INT18850070,INT18850075,INT18850077,INT18850078,INT18850085,INT18850086,INT18850087,INT18850088,INT18850089,INT18850091,INT18850092,INT18850099,INT18850104,INT18850105,INT18850106,INT18850107,INT18850114,INT18850115,INT18850116,INT18850117,INT18850119,INT18850120,INT18850121,INT18850122,INT18850131,INT18850132,INT18850134,INT18850135,INT18850136,INT18850137,INT18850148,INT18850149,INT18850150,INT18850151,INT18850152,INT18850155,INT18850156,INT18850158,INT18850159,INT18850160,INT18850161,INT18850162,INT18850163,INT18850164,INT18850173,INT18850175,INT18850176,INT18850177,INT18850178,INT18850188,INT18850190,INT18850191,INT18850192,INT18850193,INT18850199,INT18850200,INT18850201,INT18850203,INT18850205,INT18850206,INT18850207,INT18850208,INT18850221,INT18850219,INT18850220,INT18850222,INT18850233,INT18850234,INT18850235,INT18850236,INT18850237,INT18850246,INT18850247,INT18850248,INT18850249,INT18850250,INT18850251,INT18850252,INT18850253,INT18850274,INT18850282,INT18850283,INT18850284,INT18850285,INT18850286,INT18850287,INT18850288,INT18850289,INT18850290,INT18850291,INT18850292,INT18850293,INT18850294,INT18850295,INT18850296,INT18850297,INT18850298,INT18850299,INT18850300,INT18850301,INT18850302,INT18850303,INT18850304,INT18850309,INT18850311,INT18850310,INT18850312,INT18850314,INT18850315,INT18850316,INT18850317,INT18850327,INT18850329,INT18850330,INT18850339,INT18850341,INT18850342,INT18850343,INT18850344,INT18850345,INT18850346,INT18850347,INT18850348,INT18850349,INT18850350,INT18850351,INT18850352,INT18850353,INT18850354,INT18850355,INT18850356,INT18850357,INT18850358,INT18850359,INT18850360,INT18850361,INT18850362,INT18850363,INT18850364,INT18850365,INT18850366,INT18850367,
    ../../log/beepadmin-20131111-57.log:11.11.2013 18.00.19:001 17905350 DEBUG   {RouterMTDbOuWorker}ProviderApp [ProviderMessage-INT18849772] Send message : dstAddr = +6500037411 , oriAddr = null , oriAddrMask = +6591093429 , debitAmount = 2.0 , cntType = 0 , msgCount = 2 , msgContent = Test SG 100000 Numbers B CO4_00037411 CO5_00037411  http:\/\/xip.me\/Q0bnf 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 End
    ../../log/beepadmin-20131111-57.log:11.11.2013 18.00.19:001 17905351 DEBUG   {RouterMTDbOuWorker}ProviderUtil [ProviderMessage-INT18849772] Resolved master provider : id = 36 , providerId = BC , direction = OU , type = EXTERNAL , description = Beepcast Mach Simulator
    ../../log/beepadmin-20131111-57.log:11.11.2013 18.00.22:991 17950770 WARNING {RouterMTDbOuWorker}MTSendWorker [Provider-BC1] [ProviderMessage-INT18849772] Failed to store provider message into provider agent channel , with : BC , +6500037411 , SMS.MT , SMSTEXT , Test SG 100000 Numbers B CO4_0 , takes = 3990 ms
    ../../log/beepadmin-20131111-57.log:11.11.2013 18.00.22:993 17950777 WARNING {RouterMTDbOuWorker}ProviderService [RouterMessage-INT18849772] Failed to process mt message , found failed to send message thru provider app
    ../../log/beepadmin-20131111-57.log:11.11.2013 18.00.22:993 17950778 WARNING {RouterMTDbOuWorker}RouterMTWorker [RouterMessage-INT18849772] Failed to submit a mt message to provider app

- Add new library :
  . transaction2beepcast-v1.1.13.jar
  . model2beepcast-v1.0.64.jar
  . dwh_model-v1.0.27.jar

v1.0.29

- Bug in the inserting to send buffer ?

    beepadmin-20131018-8.log	314747	18.10.2013 11.30.11:815 3229903 DEBUG   {ChannelProcessorThread-13}RouterMTWorker [RouterMessage-INT14356375] Send Mt Message : messageId = INT14356375 , gatewayMessageType = 0 , messageCount = 1 , messageContent = Test Outgoing Basic Event 01 Code , debitAmount = 1.0 , phoneNumber = +6500001371 , providerId = EE4 , eventId = 6 , channelSessionId = 3 , senderId = +6591093429 , priority = 1 , retry = 0 , dateSend = null , mapParams = {}
    beepadmin-20131018-8.log	314748	18.10.2013 11.30.11:815 3229904 DEBUG   {ChannelProcessorThread-13}RouterMessageCommon [RouterMessage-INT14356375] Found null submit date time , meant it will send straight away
    beepadmin-20131018-8.log	314749	18.10.2013 11.30.11:815 3229905 DEBUG   {ChannelProcessorThread-13}RouterMTWorker [RouterMessage-INT14356375] Found process queue is full
    beepadmin-20131018-8.log	314750	18.10.2013 11.30.11:815 3229906 DEBUG   {ChannelProcessorThread-13}RouterMTWorker [RouterMessage-INT14356375] Stored the mt message into input batch queue
    beepadmin-20131018-8.log	314751	18.10.2013 11.30.11:816 3229907 DEBUG   {ChannelProcessorThread-13}Transaction [INT14356375] Successfully inserted an outgoing message into send buffer
    beepadmin-20131018-8.log	314752	18.10.2013 11.30.11:816 3229908 DEBUG   {ChannelProcessorThread-13}Transaction [INT14356375] [1] Successfully processed as normal message

    18.10.2013 14.05.57:656 2214 DEBUG   {RouterMTManagementWorker}RouterMTWorker Found input batch queue is idle , clean total 1 message(s) inside .
    18.10.2013 14.05.57:660 2215 DEBUG   {RouterMTManagementWorker}RouterMTWorker Read total 1 send buffer bean(s) ready to insert into send buffer table : MDM14456000,
    18.10.2013 14.05.57:664 2216 DEBUG   {RouterMTManagementWorker}SendBufferDAO Prepared batch send buffer record ( 0 ) : [MDM14456000, 0, 1, Test Incoming Basic Event 01 http://xipme.com:8883/L1s9 Â£Â¥Ã¨Ã©Ã¹Ã¬Ã²Ã¤Ã¶Ã±Ã¼Ã  Test, 1.0, +6598394294, EE4, 1, 0, +6591093429, 5, 0, 0, 0, , null]
    18.10.2013 14.05.57:679 2217 INFO    {RouterMTManagementWorker}DatabaseEngine [transactiondb] Loaded batch record(s) total = 1
    18.10.2013 14.05.57:690 2218 ERROR   {RouterMTManagementWorker}DatabaseEngine [transactiondb] Failed to execute the batch , java.sql.BatchUpdateException: No value specified for parameter 16
    18.10.2013 14.05.57:690 2219 INFO    {RouterMTManagementWorker}DatabaseEngine [transactiondb] Executed batch record(s) total = 0
    18.10.2013 14.05.57:691 2220 WARNING {RouterMTManagementWorker}SendBufferDAO Failed to execute the batch insert send buffer bean(s) into table
    
    
- Add new library :
  . provider2beepcast-v2.2.14.jar
  . client2beepcast-v2.3.02.jar
  . 

v1.0.28

- Bug to update provider delivery report status into gateway log table

  {RouterDRDBWorkerThread}RouterDRWorker [RouterMessage-EE4-MDM14231002] Queuing provider dr message : recordId = 459337 , providerId = EE4 , externalStatus = 104 , internalStatus = FAILED , statusDateTime = 2013-09-25 09:34:12 , externalParams = ZG5yPSUyQjY1OTA1MTc3MTUmZHRhZz1NRE0xNDIzMTAwMiZzdGF0dXM9MiZyZWFzb249MTA0JnRk\r\naWY9MTIwJmRuZXRpZD02MkYyMjAmbW5jPTI1RiZtY2M9NTEw
  {RouterDRDBWorkerThread}DrBufferDAO Perform DELETE FROM dn_buffer WHERE dn_id IN (459337) 
  {RouterDRWorkerThread-3}LoadProfiles [LoadProfile-ROUTER_SMSDR_SYS] Stored profile object in the map
  {RouterDRDBWorkerThread}RouterDRWorker Successfully moved 1 dr record(s) from dn_buffer table into dr queue ( take 0 ms ) , listRecordIds : 459337
  {RouterDRWorkerThread-3}RouterDRWorkerThread [RouterMessage-EE4-MDM14231002] Failed to enrich provider message , failed to match dr message inside the gateway log table
  {RouterDRWorkerThread-3}RouterDRWorkerThread [RouterMessage-EE4-MDM14231002] Failed to extract dr message , found not matched with any gateway log record
  {RouterDRWorkerThread-3}DrBufferDAO Perform INSERT INTO dn_buffer (provider_id,message_id,external_status,external_status_date,internal_status,priority,description,external_params,retry,date_inserted) VALUES ('EE4','MDM14231002','104','2013-09-25 09:34:12','FAILED',0,'','ZG5yPSUyQjY1OTA1MTc3MTUmZHRhZz1NRE0xNDIzMTAwMiZzdGF0dXM9MiZyZWFzb249MTA0JnRkaWY9MTIwJmRuZXRpZD02MkYyMjAmbW5jPTI1RiZtY2M9NTEw',1,NOW()) 
  {RouterDRWorkerThread-3}RouterDRWorkerThread [RouterMessage-EE4-MDM14231002] Found current dr message's retry = 0 , trying to store into dn buffer : result = true

- Clean up the gateway and mobile user table ( phone and message )

  gatewayLogDAO
  mobileUserDAO

- Bug to forward MoBufferBean.SendDateStr (RouterApp) to TransactionMessage.SendDateStr (TransactionApp)

  {RouterMOWorkerThread-0}RouterMOProcessor [RouterMessage-API14033000-0-0] Processing mo message : transProcType = 0 , clientId = 1 , eventId = 1 , eventCode = ADI , provider = CLIENT_API , originNumber = +85290517715 , destinationNumber = null , message = ADI , replyMessage = Test from benny iskandar , externalMapParams = {SendDateStr=2014-05-05 21:35:15} , deliverDateTime = 2013-09-23 14:49:37
  {RouterMOWorkerThread-0}RouterMOProcessor [RouterMessage-API14033000-0-0] Define pre-set transaction input message params : clientId = 1
  {RouterMOWorkerThread-0}RouterMOProcessor [RouterMessage-API14033000-0-0] Define pre-set transaction input message params : eventId = 1
  {RouterMOWorkerThread-0}RouterMOProcessor [RouterMessage-API14033000-0-0] Prepared transaction input message : tranProcType = 0 , priority = 5 , originalProvider = CLIENT_API , originalAddress = +85290517715 , destinationAddress = null , messageContent = ADI , replyMessageContent = Test from benny iskandar
  {RouterMOWorkerThread-0}RouterMOProcessor [RouterMessage-API14033000-0-0] Created transaction process object with process type : standard

  {RouterMOWorkerThread-1}RouterMTWorker [RouterMessage-API14036001-0-0] Send Mt Message : messageId = API14036001-0-0 , gatewayMessageType = 0 , messageCount = 1 , messageContent = Test from benny iskandar , debitAmount = 1.0 , phoneNumber = +85290517715 , providerId = EE4 , eventId = 1 , channelSessionId = 0 , senderId = Hello , priority = 5 , retry = 0 , dateSend = 2013-09-23 17:00:00 , mapParams = {oriProvider=CLIENT_API}
  {RouterMOWorkerThread-1}RouterMTWorker [RouterMessage-API14036001-0-0] Stored the mt message into input output queue
  {RouterMOWorkerThread-1}Transaction [API14036001-0-0] Successfully inserted an outgoing message into send buffer
  {RouterMOWorkerThread-1}Transaction [API14036001-0-0] [1] Successfully processed as normal message
  {RouterMOWorkerThread-1}RouterMOProcessor [RouterMessage-API14036001-0-0] Finish transaction process , resultCode = PROCESS_SUCCEED , take = 22 ms
  {RouterMTInOuWorker}ProviderApp [ProviderMessage-API14036001-0-0] Send message : dstAddr = +85290517715 , oriAddr = null , oriAddrMask = Hello , debitAmount = 1.0 , cntType = 0 , msgCount = 1 , msgContent = Test from benny iskandar
  {RouterMTInOuWorker}ProviderUtil [ProviderMessage-API14036001-0-0] Resolved provider : id = 29 , providerId = EE4 , direction = OU , type = EXTERNAL , description = Mach Provider
  {RouterMTInOuWorker}MTSendWorker [Provider-EE4] [ProviderMessage-API14036001-0-0] Successfully stored provider message into provider agent channel , with : EE4 , +85290517715 , SMS.MT , SMSTEXT , Test from benny iskandar , takes = 0 ms
  {End2endAgentThread-EE4.1}End2endAgent [ProviderMessage-API14036001-0-0] Got a message from internal channel
  {End2endAgentThread-EE4.1}End2endAgent [Provider-EE4] [ProviderMessage-API14036001-0-0] Composed httpParameters id=36140&pw=u6L373uq&dnr=%2B85290517715&snr=Hello&drep=1&dtag=API14036001-0-0&split=5&msg=Test+from+benny+iskandar
  {End2endAgentThread-EE4.1}End2endAgent [Provider-EE4] [ProviderMessage-API14036001-0-0] Created the request to remoteUrl http://corvus:9702/ee_two
  {End2endAgentThread-EE4.1}End2endAgent [Provider-EE4] [ProviderMessage-API14036001-0-0] Processed the response ( 12 ms ) = +OK01
  {End2endAgentThread-EE4.1}End2endAgent [Provider-EE4] [ProviderMessage-API14036001-0-0] Extracted external status = +OK01
  {End2endAgentThread-EE4.1}BaseAgent [ProviderMessage-API14036001-0-0] Found map status response +OK01 -> SUBMITTED
  
- Add new library :
  . transaction2beepcast-v1.1.12.jar
  . provider2beepcast-v2.2.12.jar
  . client_request2beepcast-v1.0.02.jar
  . client2beepcast-v2.3.01.jar
  . model2beepcast-v1.0.62.jar
  . subscriber2beepcast-v1.2.25.jar
  . beepcast_keyword-v1.0.04.jar
  . beepcast_online_properties-v1.0.03.jar
  . beepcast_dbmanager-v1.1.31.jar
  . beepcast_onm-v1.2.04.jar

v1.0.27

- Add process delivery report per provider broker

- Add new library :
  . provider2beepcast-v2.2.09.jar
  . transaction2beepcast-v1.1.05.jar
  . model2beepcast-v1.0.60.jar
  . dwh_model-v1.0.25.jar

v1.0.26

- Put retry for the delivery report that found no match with gateway log, 
  it might be the record is not inserted yet

- Execute sql below 

    ALTER TABLE `dn_buffer` 
      ADD COLUMN `retry` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `external_params` ;  
  
- There is a change inside file ./conf/oproperties.xml

      <property field="Router.CachedMOMessage" value="true"
        description="First mo message store into queue or table" />
      <property field="Router.RetryDRSynch" value="1"
        description="Total retries for unmatch dr inside gateway log table" />

- Execute sql below 

    ALTER TABLE `mo_buffer` 
      ADD COLUMN `params` TEXT AFTER `senderID` ;

    ALTER TABLE `mo_buffer` 
      CHANGE COLUMN `params` `external_params` TEXT CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL ;
      
- Create backend for MO Buffer , with additional params

- Put rules inside MO Worker for client api transaction need to support the masking sender address

- Add new library :
  . transaction2beepcast-v1.1.04.jar
  . provider2beepcast-v2.2.07.jar
  . model2beepcast-v1.0.59.jar
  . subscriber2beepcast-v1.2.20.jar
  . dwh_model-v1.0.22.jar
  . client2beepcast-v2.2.14.jar
  . beepcast_dbmanager-v1.1.27.jar
  . beepcast_onm-v1.2.01.jar
  . beepcast_online_properties-v1.0.02.jar

v1.0.25

- Adapted the api of transaction process

- Add new library :
  . transaction2beepcast-v1.1.00.jar
  . model2beepcast-v1.0.55.jar
  . beepcast_keyword-v1.0.03.jar
  . 

v1.0.24

- Found crash during mo message will effect all the remaining process as well :

    07.09.2012 14.10.12:263 6128961 DEBUG   {HttpProcessor[8080][8]}TransAction Process the new request : node = ANDROMEDA , phone = +6598394294 , message = adi 14.10 , senderID = +6591093429 , messageType = 0 , providerName = STARHUBMDM01 , command = process_sms , clientId =  , listId =  , subscribed =  , browserAddress =  , browserAgent =
    07.09.2012 14.10.12:263 6128962 DEBUG   {HttpProcessor[8080][8]}TransAction Execute command process sms
    07.09.2012 14.10.12:263 6128963 DEBUG   {HttpProcessor[8080][8]}TransActionProcessSms [MDM15933486] Processed mo message ( takes 0 ms ) : transProcType = 0 , messageType = SMS.MO , deliverDateTime = 2012-09-07 14:10:12
    07.09.2012 14.10.12:263 6128964 DEBUG   {HttpProcessor[8080][8]}TransAction Create the response OK

- Add new library :
  . transaction2beepcast-v1.0.77.jar
  . model2beepcast-v1.0.54.jar
  . billing2beepcast-v1.1.05.jar
  . client2beepcast-v2.2.13.jar
  . provider2beepcast-v2.2.04.jar
  . subscriber2beepcast-v1.2.19.jar
  . dwh_model-v1.0.20.jar
  . beepcast_database-v1.1.05.jar

v1.0.23

- Add external api to store mo message

- Add new library :
  . transaction2beepcast-v1.0.71.jar
  . provider2beepcast-v2.2.03.jar
  . client2beepcast-v2.2.12.jar
  . model2beepcast-v1.0.52.jar
  . subscriber2beepcast-v1.2.18.jar
  . throttle-v1.0.01.jar
  . 

v1.0.21

- Log failed message id inserted into dn_buffer or updated from gateway_log table

- Add new library :
  . transaction2beepcast-v1.0.62.jar
  . provider2beepcast-v2.2.00.jar
  . client2beepcast-v2.2.11.jar
  . billing2beepcast-v1.1.05.jar
  . model2beepcast-v1.0.51.jar
  . subscriber2beepcast-v1.2.10.jar
  . dwh_model-v1.0.14.jar
  . beepcast_online_properties-v1.0.01.jar
  . beepcast_dbmanager-v1.1.26.jar
  . beepcast_onm-v1.1.08.jar
  . 

v1.0.20

- Change headerLog inside RouterDRWorkerThread to use internalMessageId after enrich the message,
  so the processed is trackable

- Calculate latency for tyntec provider based on delivery status submitDate and statusDate 

- Add new library :
  . 

v1.0.19

- Process Mobile Country Code Network Code insert into mobile user table

- Add new library :
  . transaction2beepcast-v1.0.55.jar
  . provider2beepcast-v2.1.24.jar
  . billing2beepcast-v1.1.04.jar
  . model2beepcast-v1.0.45.jar
  . subscriber2beepcast-v1.2.05.jar
  . dwh_model-v1.0.12.jar
  . beepcast_dbmanager-v1.1.25.jar

v1.0.18

- Build with proper conf factory

- Linked to onmApp

- Add new library :
  . beepcast_loadmanagement-v1.2.02.jar
  . beepcast_onm-v1.1.06.jar
  . transaction2beepcast-v1.0.51.jar
  . model2beepcast-v1.0.42.jar
  . subscriber2beepcast-v1.2.03.jar
  . beepcast_session-v1.0.01.jar

v1.0.18

- Integrated with new running number library 

- Add new library :
  . beepcast_idgen-v1.0.00.jar
  . client2beepcast-v2.2.09.jar

v1.0.17

- Add feature to filter during process the mo_buffer records , 
  only select the records with valid send date

- Add new library :
  . client2beepcast-v2.2.08.jar
  . beepcast_dbmanager-v1.1.22.jar
  . provider2beepcast-v2.1.23.jar
  . transaction2beepcast-v1.0.48.jar

v1.0.16

- Restructure mo buffer table to add new field to store TransactionProcessType value

    ALTER TABLE `mo_buffer` 
      ADD COLUMN `trnprc_type` VARCHAR(45) AFTER `mo_id` ;

- Add new library :
  . transaction2beepcast-v1.0.47.jar
  . provider2beepcast-v2.1.22.jar
  . client2beepcast-v2.2.04.jar
  . model2beepcast-v1.0.40.jar
  . subscriber2beepcast-v1.2.02.jar
  . beepcast_dbmanager-v1.1.20.jar
  . beepcast_encrypt-v1.0.00.jar
  . db4o-6.4.54.11278-java1.2.jar
  . beepcast_session-v1.0.00.jar
  . beepcast_keyword-v1.0.02.jar

v1.0.15

- Filter for outgoing direction provider only to support mt worker .

- Add online property for suspend mt , mo , and dr transactions inside .

      <property field="Router.SuspendMOWorker" value="false"
        description="To suspend all the mo messages process" />
      <property field="Router.SuspendMTWorker" value="false"
        description="To suspend all the mt messages process" />
      <property field="Router.SuspendDRWorker" value="false"
        description="To suspend all the dr messages process" />

- Add new library :
  . beepcast_dbmanager-v1.1.19.jar
  . model2beepcast-v1.0.37.jar
  . provider2beepcast-v2.1.21.jar

v1.0.14

- Use the new structure for sms process execution .

- Add new library :
  . transaction2beepcast-v1.0.43.jar

v1.0.13

- Add filter before submit mt message , to verify the existing list active providers .
  When found unregistered provider id , will still store into send buffer table .
  Assume unregistered provider id as modem id .

- Provide router api to send mt messages with batch mechanism .

- Add configuration on the fly on router throughput .

- Add configuration on the fly on the max buffer 
      
      <property field="Router.DeliveryStatusMaxLoad" value="20"
        description="The max load process of delivery status router in msg/sec" />
      <property field="Router.SubmitMessageMaxLoad" value="25"
        description="The max load process of submit message router in msg/sec" />
      <property field="Router.DeliveryMessageMaxLoad" value="20"
        description="The max load process of delivery message router in msg/sec" />
      
      <property field="Router.DeliveryStatusMaxBuffer" value="1000"
        description="The max delivery status records inside buffer table" />
      <property field="Router.SubmitMessageMaxBuffer" value="1000"
        description="The max submit message records inside buffer table" />
      <property field="Router.DeliveryMessageMaxBuffer" value="1000"
        description="The max delivery message records inside buffer table" />

- Change the design router mt process :
  . use an api to send mt message .
  . thru the api will store the message into the cache or table first .
  . will process the mt message from table or cache thru provider app .
  . use the batch mechanism for performance wise .

- Add new library :
  . transaction2beepcast-v1.0.38.jar
  . client_request2beepcast-v1.0.01.jar
  . client2beepcast-v2.2.02.jar
  . model2beepcast-v1.0.36.jar
  . subscriber2beepcast-v1.1.04.jar
  . beepcast_dbmanager-v1.1.17.jar

v1.0.12

- Change structure inside send_buffer table :

    ALTER TABLE `send_buffer` ADD COLUMN `params` TEXT AFTER `retry` ;

- Add Router Api to send mt message 

  . move the send buffer module from transaction to router 
  . provider api send mt message thru router api

- Support Integrated Event Bean , Service and DAO

- add new library :
  . model2beepcast-v1.0.31.jar  
  . provider2beepcast-v2.1.20.jar
  . client2beepcast-v2.2.01.jar
  . subscriber2beepcast-v1.1.02.jar
  . dwh_model-v1.0.09.jar
  . beepcast_dbmanager-v1.1.15.jar
  . transaction2beepcast-v1.0.34.jar

v1.0.11

- fixed bugs : this is only found in windows environment , there is a access read write issue .

  java.lang.NullPointerException
  	at com.beepcast.database.DatabaseLibrary.simpleQuery(DatabaseLibrary.java:339)
  	at com.beepcast.router.RouterMTWorker.queryMtMessages(RouterMTWorker.java:446)
  	at com.beepcast.router.RouterMTWorker.processMtMessages(RouterMTWorker.java:316)
  	at com.beepcast.router.RouterMTWorker.access$6(RouterMTWorker.java:297)
  	at com.beepcast.router.RouterMTWorker$RouterMTDBWorkerThread.run(RouterMTWorker.java:654)

	beepadmin.log	1121	20.05.2010 15.49.59:578 1105 ERROR   
	{RouterMTDBWorkerThread}DatabaseLibrary [transactiondb] 
	Database query failed , java.sql.SQLException: Can't create/write to file 'C:\WINDOWS\TEMP\#sql_f0_0.MYD' (Errcode: 13)

- add new library :
  . transaction2beepcast-v1.0.26.jar
  . client_request2beepcast-v1.0.00.jar
  . model2beepcast-v1.0.28.jar
  . subscriber2beepcast-v1.1.01.jar
  . beepcast_dbmanager-v1.1.11.jar  	
  	
v1.0.10

- change the way to store client message thru client app , by using client api .

- add new library :
  . client2beepcast-v2.1.08.jar
  . transaction2beepcast-v1.0.25.jar

v1.0.09

- restructure router message , 
  . add date inserted

- execute sql below :

  ALTER TABLE `dn_buffer` 
    ADD COLUMN `external_status_date` DATETIME AFTER `external_status` ;

  ALTER TABLE `gateway_log` 
    MODIFY COLUMN `date_tm` DATETIME ;
      
- add new library :
  . provider2beepcast-v2.1.18.jar
  . transaction2beepcast-v1.0.24.jar
  . model2beepcast-v1.0.26.jar

v1.0.08

- support report for mo , mt and dn buffer 

- support latency report for dn with delivered status .

- clean buggy log , below :
  08.02.2010 14.24.49:015 844 DEBUG   {RouterDRWorkerThread-0}RouterToClient [RouterMessage-MDM3647000] 
  Failed to store client dr message , the client profile is not support api mechanism

- add new library :
  . transaction2beepcast-v1.0.23.jar
  . billing2beepcast-v1.1.03.jar
  . model2beepcast-v1.0.24.jar
  . subscriber2beepcast-v1.0.02.jar
  . beepcast_onm-v1.1.03.jar
  . provider2beepcast-v2.1.17.jar

v1.0.07

- there is a race dead lock in the threads , please re-design the flow ...

- add flag to debug if the dr process slower or faster , it depends on the client dr queue .

- add feature to insert invalid number into a list , still configurable 
  in the online global properties .

- add new library :
  . transaction2beepcast-v1.0.21.jar
  . provider2beepcast-v2.1.16.jar
  . model2beepcast-v1.0.22.jar
  . subscriber2beepcast-v1.0.01.jar
  . dwh_model-v1.0.08.jar

v1.0.06

- fixed inside the retry mechanism to copy the channel_session_id also .

- perform switch provider for the next retry message , put in the
  online properties so it can be configure on the fly .

- add new library :
  . model2beepcast-v1.0.19.jar
  . beepcast_online_properties-v1.0.00.jar
  . provider2beepcast-v2.1.15.jar

v1.0.05

- add debug attribute at configuration file , router.xml

- make sure all fields in the mo_buffer will transfer , etc :
  message_id , message_type , source_node .

- add new library
  . model2beepcast-v1.0.15.jar
  . client2beepcast-v2.1.06.jar
  . 

v1.0.04

- apply hit's load function to monitor the MT , MO and DR load inside router module

- add new library :
  . model2beepcast-v1.0.14.jar
  . transaction2beepcast-v1.0.15.jar
  . provider2beepcast-v2.1.14.jar
  . beepcast_loadmanagement-v1.2.00.jar

v1.0.03

- no need to store in the client message when there is no client api supported

- restructure the MTWorker :
  . MTDBWorker function make simplified

- add new library :
  . provider library v2.1.11
  
- change the way to sent mt message to provider agent

v1.0.02

- add new library :
  . quartz and commons-collection
  . dbmanager library v1.1.06
  . billing libraray v1.1.01
  . client library v2.1.05
  . transaction library v1.0.03

v1.0.01

- simulate with test application and sql insert below :

    INSERT INTO mo_buffer
    ( provider , phone , message , date_inserted )
    VALUES
    ( 'MODEM 1' , '+6590517700' , 'adi' , NOW() )

- will use the new transaction library v1.0.01 with switch feature .

- restructure private singleton data member 

- restructure the bean , and add TransactionInput/OutputMessage feature from the new transaction 
  library

v1.0.00

- added a new java library named model2beepcast-v1.0.00.jar and transaction2beepcast-v1.0.00.jar

- added a new java library named provider2beepcast-v2.1.10.jar

- added a new java library named util2beepcast-v1.0.00.jar

- Just copy com.beepcast.router.* classes from beepadmin
