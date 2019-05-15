package com.huawei.vcenterpluginui.services;

import com.google.gson.Gson;
import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.alarm.GetAlarmApi;
import com.huawei.esight.api.rest.notification.DeleteNotificationCommonAlarm;
import com.huawei.esight.api.rest.notification.PutNotificationCommonAlarm;
import com.huawei.vcenterpluginui.constant.ESightServerType;
import com.huawei.vcenterpluginui.dao.ESightDao;
import com.huawei.vcenterpluginui.dao.NotificationAlarmDao;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.AlarmRecord;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.HAComponent;
import com.huawei.vcenterpluginui.entity.HAEventDef;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.exception.VersionNotSupportException;
import com.huawei.vcenterpluginui.provider.SessionOpenIdProvider;
import com.huawei.vcenterpluginui.utils.AlarmDefinitionConverter;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import com.huawei.vcenterpluginui.utils.HAEventHelper;
import com.huawei.vcenterpluginui.utils.OpenIdSessionManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class NotificationAlarmServiceImpl extends ESightOpenApiService implements
    NotificationAlarmService {

  private String subscribeUrl;

  private ServerApiService serverApiService;

  private ESightService eSightService;

  private VCenterHAService vCenterHAService;

  private VCenterInfoService vCenterInfoService;

  private ESightHAServerService eSightHAServerService;

  private NotificationAlarmDao notificationAlarmDao;

  @Autowired
  private SystemKeepAliveService systemKeepAliveService;

  @Autowired
  private SyncServerHostService syncServerHostService;

  private BlockingQueue<Map<String, Object>> eventQueue = new LinkedBlockingQueue<>(2000);

  private ConnectedVim connectedVim = null;

  private ExecutorService eventExecutor = Executors.newSingleThreadExecutor();

  private ExecutorService backgroundTaskExecutor = Executors.newSingleThreadExecutor();

  @Autowired
  private ESightDao eSightDao;

  private static final Gson GSON = new Gson();

  private HttpSession globalSession = OpenIdSessionManager.getGlobalSession();

  @Override
  public Map subscribeAlarm(ESight eSight, OpenIdProvider openIdProvider, String desc) {
    String callbackUrl;
    try {
      callbackUrl = getSubscribeUrl();
      LOGGER.info("notification callback url: " + callbackUrl);
      String openID = eSight.getHostIp();
      Map response = new PutNotificationCommonAlarm<Map>(eSight, openIdProvider)
          .doCall(eSight.getSystemId(), openID,
              callbackUrl, "JSON", desc, Map.class);
      LOGGER.info("subscribe info: " + response);
      if (isSuccessResponse(response.get("code"))) {
        eSightService.updateHAStatus(eSight.getHostIp(), "1");
        // subscribe system keep alive
        Map map = systemKeepAliveService
            .subscribeSystemKeepAlive(eSight, openID, openIdProvider, desc);
        if (isSuccessResponse(map.get("code"))) {
          eSightService.updateSystemKeepAliveStatus(eSight.getHostIp(), "1");
        }
      }
      return response;
    } catch (VcenterException e) {
      LOGGER.error("Failed to subscribe alarm: " + e.getMessage());
      throw e;
    }
  }

  @Override
  public Map subscribeAlarm(ESight eSight, HttpSession session, String desc) {
    return subscribeAlarm(eSight, new SessionOpenIdProvider(eSight, session), desc);
  }

  @Override
  public Map subscribeAlarm(String esightIp, HttpSession session, String desc) {
    try {
      return subscribeAlarm(getESightByIp(esightIp), session, desc);
    } catch (SQLException e) {
      LOGGER.error("Failed to subscribe alarm: " + e.getMessage());
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public Map unsubscribeAlarm(ESight eSight, OpenIdProvider openIdProvider, String desc) {
    Map response = new DeleteNotificationCommonAlarm<Map>(eSight, openIdProvider)
        .doCall(eSight.getSystemId(), desc, Map.class);
    LOGGER.info("unsubscribe info: " + response);
    if (isSuccessResponse(response.get("code"))) {
      eSightService.updateHAStatus(eSight.getHostIp(), "2");
      // unsubscribe system keep alive
      Map map = systemKeepAliveService.unsubscribeSystemKeepAlive(eSight, openIdProvider, desc);
      LOGGER.info("unsubscribe system keep alive info: " + map);
      if (isSuccessResponse(map.get("code"))) {
        eSightService.updateSystemKeepAliveStatus(eSight.getHostIp(), "2");
      }
    }
    return response;
  }

  @Override
  public Map unsubscribeAlarm(ESight eSight, HttpSession session, String desc) {
    return unsubscribeAlarm(eSight, new SessionOpenIdProvider(eSight, session), desc);
  }

  @Override
  public Map unsubscribeAlarm(String esightIp, HttpSession session, String desc) {
    try {
      return unsubscribeAlarm(getESightByIp(esightIp), session, desc);
    } catch (SQLException e) {
      LOGGER.error("Failed to unsubscribe alarm: " + e.getMessage());
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public void handleCallbackEvent(final String alarmBody, final String fromIP) {
    try {
      // LOGGER.info("Receiving alarm: " + alarmBody);
      List<Map<String, Object>> alarmList = GSON.fromJson(alarmBody, List.class);
      // eSight
      ESight eSight = getESightByIp(fromIP);
      if (eSight == null) {
        LOGGER.warn("Invalid eSight: " + fromIP);
        return;
      }
      for (Map<String, Object> elementMap : alarmList) {
        // optType
        Object ooptType = elementMap.get("optType");
        if (ooptType == null) {
          LOGGER.warn("Alarm doesn't have optType. Discard.");
          continue;
        }
        double optType = (Double) ooptType;
        // 过滤掉3：确认告警，4：反确认告警
        if (optType == 3.0 || optType == 4.0) {
          LOGGER.warn("OptType is 3 or 4. Discard.");
          continue;
        }
        // neDN
        String neDN = (String) elementMap.get("neDN");
        if (neDN == null) {
          LOGGER.warn("Alarm doesn't have neDN. Discard.");
          continue;
        }
        // alarmSN
        int alarmSN = ((Double) elementMap.get("alarmSN")).intValue();
        // alarmId
        long alarmId = ((Double) elementMap.get("alarmId")).longValue();
        // severity
        int severity = ((Double) elementMap.get("perceivedSeverity")).intValue();
        if (severity < 1 || severity > 4) {
          LOGGER.info("Discard perceivedSeverity value: " + severity);
          continue;
        }
        // put to queue
        putEventToQueue(eSight.getId(), alarmSN, alarmId, severity, optType == 2.0, neDN,
            elementMap.get("objectInstance").toString());
      }
    } catch (Exception e) {
      LOGGER.error("Failed to push event to queue: " + e.getMessage());
    }
  }

  @Override
  public void start() {
    eventExecutor.execute(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            getAndHandleEventFromQueue();
          } catch (InterruptedException e) {
            LOGGER.info("Event handler is interrupted: " + e.getMessage());
            break;
          } catch (Exception e) {
            LOGGER.error("Cannot handle event: " + e.getMessage());
          }
        }
      }
    });
  }

  private void putEventToQueue(int esightId, int sn, long alarmId, int severity, boolean isResume,
      String neDN, String objectInstance) {
    Map<String, Object> eventDataMap = new HashMap<>();
    eventDataMap.put("esightId", esightId);
    eventDataMap.put("alarmId", alarmId);
    eventDataMap.put("sn", sn);
    eventDataMap.put("severity", severity);
    eventDataMap.put("resume", isResume);
    eventDataMap.put("neDN", neDN);
    eventDataMap.put("objectInstance", objectInstance);
    putEventToQueue(eventDataMap);
  }

  private void putEventToQueue(Map<String, Object> eventDataMap) {
    LOGGER.info("[EventQ]Putting into queue: " + eventDataMap);
    if (!eventQueue.offer(eventDataMap)) {
      LOGGER.info("[EventQ]Event queue is full, discard " + eventDataMap);
    }
  }

  private void getAndHandleEventFromQueue() throws InterruptedException {
    LOGGER.info("[EventQ]Event queue size: " + eventQueue.size());
    if (eventQueue.isEmpty()) {
      if (connectedVim != null) {
        connectedVim.disconnect();
        connectedVim = null;
      }
    }
    Map<String, Object> eventDataMap = eventQueue.take();
    VCenterInfo vCenterInfo;
    try {
      vCenterInfo = vCenterInfoService.getVCenterInfo();
    } catch (SQLException e) {
      LOGGER.info("Cannot get vCenter info", e);
      throw new VcenterException("Cannot get vCenter info");
    }
    if (vCenterInfo == null) {
      LOGGER.info("No vCenter setting");
      return;
    }
    if (connectedVim == null) {
      connectedVim = vCenterHAService.getConnectedVim();
      connectedVim.connect(vCenterInfo);
    }
    doHandleEvent(eventDataMap, vCenterInfo);
  }

  private void doHandleEvent(final Map<String, Object> eventDataMap, VCenterInfo vCenterInfo) {
    int esightId = (int) eventDataMap.get("esightId");

    int sn = (int) eventDataMap.get("sn");
    int severity = (int) eventDataMap.get("severity");
    boolean isResume = (boolean) eventDataMap.get("resume");
    String neDN = eventDataMap.get("neDN").toString();
    long alarmId = (long) eventDataMap.get("alarmId");
    String objectInstance = eventDataMap.get("objectInstance").toString();

    LOGGER.info("[eSightEvent]Start handling event: " + eventDataMap + ", sn: " + sn);
    List<ESightHAServer> eSightHAServers;
    try {
      eSightHAServers = eSightHAServerService.getESightHAServersByDN(esightId, neDN);
    } catch (SQLException e) {
      LOGGER.error("[eSightEvent]Cannot get sync servers");
      return;
    }
    if (eSightHAServers.isEmpty()) {
      LOGGER.info("[eSightEvent]No sync neDN: " + neDN);
      return;
    }
    Collection<String> hostSystems = new HashSet<>();
    for (ESightHAServer eSightHAServer : eSightHAServers) {
      // 高密服务器：父、子节点都往自身推送
      // 高密有父节点和子节点dn一样的，说明是父节点dn的告警
      if (ESightServerType.HIGH_DENSITY.value()
          .equalsIgnoreCase(eSightHAServer.geteSightServerType()) && !neDN
          .equalsIgnoreCase(eSightHAServer.geteSightServerDN())) {
        continue;
      }
      hostSystems.add(eSightHAServer.getHaHostSystem());
    }
    if (hostSystems.isEmpty()) {
      LOGGER.info("[eSightEvent]No host to push");
      return;
    }
    ESight eSight;
    try {
      eSight = getESightById(esightId);
    } catch (SQLException e) {
      LOGGER.info("[eSightEvent]Cannot find by eSight ID: " + esightId);
      return;
    }
    if (eSight == null) {
      LOGGER.info("[eSightEvent]eSight doesn't exists: " + esightId);
      return;
    }

    boolean isBladeParentAlarm = (ESightServerType.BLADE.value()
        .equalsIgnoreCase(eSightHAServers.get(0).geteSightServerType()) &&
        neDN.equalsIgnoreCase(eSightHAServers.get(0).geteSightServerParentDN()));
    // true when matches all following scenarios
    // 1. blade
    // 2. alarm is on parent dn
    // 3. not fan not ps
    boolean isBladeParentAlarmNotOnFanOrPS = (isBladeParentAlarm && !(alarmId == 71434002L
        || alarmId == 134348799 || alarmId == 138477320 || alarmId == 138477322
        || alarmId == 671350783));

    // Alarm
    if (vCenterInfo.isPushEvent()) {
      // 调整3为4方便比较
      AlarmDefinition alarmDefinition;
      int myPushEventLevel =
          vCenterInfo.getPushEventLevel() == 3 ? 4 : vCenterInfo.getPushEventLevel();
      // myPushEventLevel - perceivedSeverity
      // 1 - 1
      // 2 - 1,2
      // 4 - 1,2,3,4
      // is the severity in scope
      if (myPushEventLevel < severity) {
        LOGGER.info("[eSightEvent]Discard perceivedSeverity not in setting scope: " + severity);
      } else if ((alarmDefinition = new AlarmDefinitionConverter().findAlarmDefinition(alarmId))
          == null) {
        LOGGER.info("[eSightEvent]Cannot find correct eventTypeID to push. Discard");
      } else if (isBladeParentAlarmNotOnFanOrPS) {
        LOGGER.info("[eSightEvent]Do not push parent not-Fan/PS alarm for alarmId: " + alarmId);
      } else {
        AlarmRecord toBeAddedAlarmRecord = null;
        AlarmRecord toBeDeletedAlarmRecord = null;
        AlarmRecord ar = new AlarmRecord();
        ar.setEsightHostId(esightId);
        ar.setSn(sn);
        ar.setEventId(alarmDefinition.getEventTypeID());
        ar.setDn(neDN);
        ar.setCreateTime(new Date());
        AlarmRecord alarmRecord;
        try {
          alarmRecord = notificationAlarmDao.getAlarmRecord(esightId, sn, neDN);
        } catch (SQLException e) {
          LOGGER.error("[eSightEvent]Cannot get alarm record: " + e.getMessage());
          throw new VcenterException("Cannot get alarm record");
        }
        if (!isResume) { // 1. alarm
          if (alarmRecord == null) {
            toBeAddedAlarmRecord = ar;
          } else {
            LOGGER.info("[eSightEvent]Existing Alarm sn data: " + alarmRecord);
          }
        } else { // 2. resume alarm
          if (alarmRecord == null) {
            LOGGER.info("[eSightEvent]No sn data to resume Alarm, sn: " + sn);
          } else {
            toBeDeletedAlarmRecord = ar;
          }
        }

        if (toBeAddedAlarmRecord != null) {
          pushAlarmEvent(vCenterInfo.getUserName(), alarmDefinition.getVcEventId(), hostSystems);
          try {
            notificationAlarmDao.addAlarmRecord(toBeAddedAlarmRecord);
          } catch (SQLException e) {
            LOGGER.error("Cannot add alarm record: " + e.getMessage());
            throw new VcenterException("Cannot add alarm record");
          }
        }
        if (toBeDeletedAlarmRecord != null) {
          // resume
          // check if any eventId exists in the eSight DN
          try {
            int alarmRecordEventIdCount = notificationAlarmDao
                .getAlarmRecordEventIdCount(toBeDeletedAlarmRecord);
            LOGGER.info(
                "[eSightEvent]Number of " + alarmDefinition.getEventTypeID() + " left to resume: "
                    + (alarmRecordEventIdCount - 1));
            if (alarmRecordEventIdCount < 2) {
              pushAlarmEvent(vCenterInfo.getUserName(), alarmDefinition.getVcResumeEventId(),
                  hostSystems);
            }
            notificationAlarmDao.deleteAlarmRecord(toBeDeletedAlarmRecord);
          } catch (SQLException e) {
            LOGGER.error("[eSightEvent]Cannot get/delete alarm record: " + e.getMessage());
            throw new VcenterException("Cannot get/delete alarm record");
          }
        }
      }
    }
    // Proactive HA
    if (vCenterInfo.isState()) {
      HAEventDef haEventDef = HAEventHelper.getInstance().getHaEventDef(alarmId);
      if (haEventDef == null) {
        LOGGER.info("[eSightEvent]eventsForHA doesn't include eventId: " + alarmId);
      } else if (isBladeParentAlarm) {
        LOGGER.info("[eSightEvent]Do not push blade parent DN for alarmId: " + alarmId);
      } else {
        HAComponent haComponent = new HAComponent();
        haComponent.setEsightHostId(esightId);
        haComponent.setComponent(haEventDef.getEventComponent());
        haComponent.setDn(neDN);
        haComponent.setSn(sn);
        List<HAComponent> haComponentList;
        HAComponent toBeAddedHAComponent = null;
        HAComponent toBeDeletedHAComponent = null;
        boolean isComponentAllResumed = false;
        boolean doPush = false;
        try {
          haComponentList = notificationAlarmDao.getHAComponents(haComponent);
        } catch (SQLException e) {
          LOGGER.error("[eSightEvent]Cannot get HA components: " + e.getMessage());
          throw new VcenterException("Cannot get HA components");
        }
        if (!isResume) { // alarm
          if (haComponentList.isEmpty()) {
            haComponent.setCreateTime(new Date());
            toBeAddedHAComponent = haComponent;
            doPush = true;
          } else {
            LOGGER.info("[eSightEvent]Existing HA sn data: " + haComponentList.get(0));
          }
        } else { // resume alarm
          if (haComponentList.isEmpty()) {
            LOGGER.info("[eSightEvent]No sn data to resume HA, sn: " + sn);
          } else {
            toBeDeletedHAComponent = haComponent;
            int componentTypeCount;
            try {
              componentTypeCount = notificationAlarmDao.getComponentTypeCount(haComponent);
            } catch (SQLException e) {
              LOGGER.error("[eSightEvent]Cannot get component count: " + e.getMessage());
              throw new VcenterException("Cannot get component count");
            }
            if (componentTypeCount < 2) {
              isComponentAllResumed = true;
              doPush = true;
            }
            LOGGER.info(
                "[eSightEvent]Number of the component left to resume: " + (componentTypeCount == 0
                    ? 0 : componentTypeCount - 1));
          }
        }

        if (doPush) {
          pushHA(haEventDef.getEventComponent(), hostSystems, isComponentAllResumed);
          LOGGER.info("[eSightEvent]Push HA completed");
        }
        if (toBeAddedHAComponent != null) {
          LOGGER.info("[eSightEvent]New HA alarm component: " + toBeAddedHAComponent + ", alarmId: "
              + alarmId);
          try {
            notificationAlarmDao.addHAComponents(toBeAddedHAComponent);
          } catch (SQLException e) {
            LOGGER.error("[eSightEvent]Cannot add HA component: " + e.getMessage());
            throw new VcenterException("Cannot add HA component");
          }
        }
        if (toBeDeletedHAComponent != null) {
          LOGGER.info(
              "[eSightEvent]Resumed HA component: " + toBeDeletedHAComponent + ", alarmId: "
                  + alarmId);
          try {
            notificationAlarmDao.deleteHAComponent(haComponent);
          } catch (SQLException e) {
            LOGGER.error("[eSightEvent]Cannot delete HA component: " + e.getMessage());
            throw new VcenterException("Cannot delete HA component");
          }
        }
      }
    }
  }

  @Override
  public Boolean uninstallProvider() {
    try {
//      try {
//        SSLUtils.turnOffSslChecking();
//      } catch (Exception e) {
//        LOGGER.error("Cannot turn off ssl checking", e);
//      }
      ConnectedVim.checkVersionCompatible();
      Boolean removeProviderResult = vCenterHAService
          .removeProvider(vCenterInfoService.getVCenterInfo());
      if (removeProviderResult == null || removeProviderResult) {
        eSightService.updateHAProvider(0);
      } else {
        eSightService.updateHAProvider(2);
      }
      return removeProviderResult;
    } catch (VersionNotSupportException e) {
      LOGGER.info("Cannot remove provider from vCenter version, " + e.getMessage());
      return null;
    } catch (Exception e) {
      LOGGER.error("Failed to uninstall provider: " + e.getMessage());
      return false;
    }
  }

  private void pushAlarmEvent(String username, String eventId, Collection<String> hostSystems) {
    LOGGER.info("[eSightEvent]Pushing event " + eventId + " to host: " + hostSystems);
    for (String hostSystem : hostSystems) {
      connectedVim.pushEvent(username, eventId, hostSystem);
    }
  }

  private void pushHA(String haComponent, Collection<String> hostSystems,
      boolean isComponentAllResumed) {
    connectedVim.pushHealth(haComponent, hostSystems, isComponentAllResumed ? "5" : "4", false);
  }

  @Override
  public void cleanData() {
    LOGGER.info("Clean data from all tables...");
    notificationAlarmDao.cleanAllData();
  }

  @Override
  public int deleteAlarmAndHADn(int esightHostId, String dn) {
    try {
      return notificationAlarmDao.deleteAlarmRecord(esightHostId, dn) + notificationAlarmDao
          .deleteHAComponent(esightHostId, dn);
    } catch (SQLException e) {
      LOGGER.error("Cannot delete alarm and HA record: " + esightHostId + dn);
    }
    return 0;
  }

  @Override
  public void syncHistoricalEvents(final ESight eSight, final boolean syncHost,
      final boolean subscribeAlarm) {
    syncHistoricalEvents(eSight, null, syncHost, subscribeAlarm);
  }

  @Override
  public void syncHistoricalEvents(final ESight eSight, final String neDN, final boolean syncHost,
      final boolean subscribeAlarm) {
    backgroundTaskExecutor.execute(new Runnable() {
      @Override
      public void run() {
        if (syncHost) {
          syncServerHostService.syncServerHost(subscribeAlarm);
        }
        LOGGER
            .info("[eSightHistoricalAlarm]Sync historic events is starting: " + eSight.getHostIp());
        VCenterInfo vCenterInfo;
        try {
          vCenterInfo = vCenterInfoService.getVCenterInfo();
        } catch (SQLException e) {
          LOGGER.error("[eSightHistoricalAlarm]Cannot get vCenter setting: " + e.getMessage());
          throw new VcenterException("Cannot get vCenter setting");
        }
        if (vCenterInfo == null || (!vCenterInfo.isState() && !vCenterInfo.isPushEvent())) {
          LOGGER.info("[eSightHistoricalAlarm]Alarm and HA is not enabled");
          return;
        }
        int pageSize = 100;
        int pageNo = 1;
        while (true) {
          try {
            while (eventQueue.size() > 200) { // wait if more than 200 events left in queue
              Thread.sleep(5000L);
            }
          } catch (InterruptedException e) {
            LOGGER.info("[eSightHistoricalAlarm]Waiting queue is interrupted");
            break;
          }

          Map<String, Object> resultMap = new GetAlarmApi<Map>(eSight)
              .doCall(null, null, null, null, null, null, null, null, neDN, pageNo, pageSize,
                  Map.class);
          String code = String.valueOf(resultMap.get("code"));
          if (!"0".equals(code)) {
            LOGGER.warn(
                "[eSightHistoricalAlarm]Cannot get historical events: " + code + ", " + resultMap);
            return;
          }
          Collection<Map<String, Object>> dataList = (Collection<Map<String, Object>>) resultMap
              .get("data");
          LOGGER.info(
              "[eSightHistoricalAlarm]Page " + pageNo + " data size: " + (dataList == null ? 0
                  : dataList.size()));
          if (dataList == null || dataList.isEmpty()) {
            break;
          }
          for (Map<String, Object> dataMap : dataList) {
            long alarmId = Long.parseLong(String.valueOf(dataMap.get("alarmId")));
            String neDN = dataMap.get("neDN").toString();
            int sn = Integer.parseInt(String.valueOf(dataMap.get("alarmSN")));
            int severity = Integer.parseInt(String.valueOf(dataMap.get("perceivedSeverity")));
            String objectInstance = dataMap.get("objectInstance").toString();
            putEventToQueue(eSight.getId(), sn, alarmId, severity, (boolean) dataMap.get("cleared"),
                neDN, objectInstance);
          }

          if (pageNo == 999 || dataList.size() < pageSize) { // max page or end
            break;
          }

          pageNo++;
        }
      }
    });
  }

  @Override
  public void syncHistoricalEvents(final boolean syncHost, final boolean subscribeAlarm) {
    backgroundTaskExecutor.execute(new Runnable() {
      @Override
      public void run() {
        if (syncHost) {
          syncServerHostService.syncServerHost(subscribeAlarm);
        }
        try {
          List<ESight> eSightList = eSightService.getESightListWithPassword(null, -1, -1);
          for (ESight eSight : eSightList) {
            syncHistoricalEvents(eSight, false, false);
          }
        } catch (SQLException e) {
          LOGGER.error("Cannot get esight list");
        }
      }
    });

  }

  @Override
  public Executor getBgTaskExecutor() {
    return backgroundTaskExecutor;
  }

  @Override
  public void unsubscribeAll() {
    List<ESight> eSightList;
    try {
      eSightList = eSightDao.getAllESights();
      for (ESight eSight : eSightList) {
        try {
          //eSightHAServerService.deleteAll(eSight.getId());
          if (!"1".equals(eSight.getReservedInt1())) {
            continue;
          }
          LOGGER.info("unsubscribe by deployer: " + eSight);
          unsubscribeAlarm(eSight, new SessionOpenIdProvider(eSight, globalSession),
              "unsubscribe_by_deployer");
        } catch (Exception e) {
          LOGGER.error("Failed to unsubscribe alarm: " + eSight + ": " + e.getMessage());
        }
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to unsubscribe all: " + e.getMessage());
    }
  }

  public String getSubscribeUrl() throws VcenterException {
    if (StringUtils.hasLength(subscribeUrl)) {
      return subscribeUrl;
    } else {
      VCenterInfo vCenterInfo;
      try {
        vCenterInfo = vCenterInfoService.getVCenterInfo();
      } catch (SQLException e) {
        throw new VcenterException("No vcenterHA info config");
      }
      if (vCenterInfo == null) {
        throw new VcenterException("vCenter info not exist.");
      } else if (!vCenterInfo.isState() && !vCenterInfo.isPushEvent()) {
        throw new VcenterException("vCenter info is disabled.");
      }
      return "https://" + vCenterInfo.getHostIp() + ":" + vCenterInfo.getHostPort()
          + "/vsphere-client/vcenterpluginui/rest/services/notification";
    }
  }

  private void destroy() {
    backgroundTaskExecutor.shutdownNow();
    backgroundTaskExecutor = null;

    eventExecutor.shutdownNow();
    eventExecutor = null;

    connectedVim = null;
    eventQueue.clear();
    eventQueue = null;
  }

  public void setSubscribeUrl(String subscribeUrl) {
    this.subscribeUrl = subscribeUrl;
  }

  public void setServerApiService(ServerApiService serverApiService) {
    this.serverApiService = serverApiService;
  }

  public void seteSightService(ESightService eSightService) {
    this.eSightService = eSightService;
  }

  public void setvCenterHAService(VCenterHAService vCenterHAService) {
    this.vCenterHAService = vCenterHAService;
  }

  public void setvCenterInfoService(VCenterInfoService vCenterInfoService) {
    this.vCenterInfoService = vCenterInfoService;
  }

  public void seteSightHAServerService(ESightHAServerService eSightHAServerService) {
    this.eSightHAServerService = eSightHAServerService;
  }

  public void setNotificationAlarmDao(NotificationAlarmDao notificationAlarmDao) {
    this.notificationAlarmDao = notificationAlarmDao;
  }

}
