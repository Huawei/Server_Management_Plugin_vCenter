package com.huawei.vcenterpluginui.services;

import com.google.gson.Gson;
import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.notification.DeleteNotificationCommonAlarm;
import com.huawei.esight.api.rest.notification.PutNotificationCommonAlarm;
import com.huawei.vcenterpluginui.constant.DeviceComponent;
import com.huawei.vcenterpluginui.constant.ESightServerType;
import com.huawei.vcenterpluginui.dao.ESightDao;
import com.huawei.vcenterpluginui.dao.NotificationAlarmDao;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.Pair;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.NoEsightException;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.provider.SessionOpenIdProvider;
import com.huawei.vcenterpluginui.utils.AlarmDefinitionConverter;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import com.huawei.vcenterpluginui.utils.OpenIdSessionManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

public class NotificationAlarmServiceImpl extends ESightOpenApiService implements NotificationAlarmService {

    private String subscribeUrl;

    private ServerApiService serverApiService;

    private ESightService eSightService;

    private VCenterHAService vCenterHAService;

    private VCenterInfoService vCenterInfoService;

    private ESightHAServerService eSightHAServerService;

    private NotificationAlarmDao notificationAlarmDao;

    @Autowired
    private SystemKeepAliveService systemKeepAliveService;

    private static final int INIT_ALARM_COUNT = 1;
    private static final int INIT_EVENT_COUNT = 1;
    private static final int INIT_EVENT_RETRY_COUNT = 1;

    @Autowired
    private ESightDao eSightDao;

    private static final Gson GSON = new Gson();

    @Value("${component.polling.times}")
    private int pollingTimes;

    private volatile ConcurrentMap<String, Counter> pollingDNMap = new ConcurrentHashMap<>();

    private HttpSession globalSession = OpenIdSessionManager.getGlobalSession();

    class Counter {
        private AtomicInteger alarmCount;
        private AtomicInteger retryCount;
        public Counter(AtomicInteger alarmCount, AtomicInteger retryCount) {
            this.alarmCount = alarmCount;
            this.retryCount = retryCount;
        }
        public void increaseAlarmCount() {
            this.alarmCount.incrementAndGet();
        }
        public void decreaseAlarmCount() {
            this.alarmCount.decrementAndGet();
        }
        public void decreaseRetryCount() {
            this.retryCount.decrementAndGet();
        }
        public void resetRetryCount() {
            this.retryCount.set(pollingTimes);
        }
        public AtomicInteger getAlarmCount() {
            return alarmCount;
        }
        public AtomicInteger getRetryCount() {
            return retryCount;
        }
        @Override
        public String toString() {
            return "alarm count: " + alarmCount.intValue() + ", retry count: " + retryCount.intValue();
        }
    }

    @Override
    public Map subscribeAlarm(ESight eSight, OpenIdProvider openIdProvider, String desc) {
        String callbackUrl;
        try {
            callbackUrl = getSubscribeUrl();
            LOGGER.info("notification callback url: " + callbackUrl);
            String openID = eSight.getHostIp();
            Map response = new PutNotificationCommonAlarm<Map>(eSight, openIdProvider).doCall(eSight.getSystemId(), openID,
                    callbackUrl, "JSON", desc, Map.class);
            LOGGER.info("subscribe info: " + response);
            if (isSuccessResponse(response.get("code"))) {
                eSightService.updateHAStatus(eSight.getHostIp(), "1");
                // subscribe system keep alive
                Map map = systemKeepAliveService.subscribeSystemKeepAlive(eSight, openID, openIdProvider, desc);
                if (isSuccessResponse(map.get("code"))) {
                    eSightService.updateSystemKeepAliveStatus(eSight.getHostIp(), "1");
                }
            }
            return response;
        } catch (VcenterException e) {
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e.getMessage(), e);
            throw new VcenterException(e.getMessage());
        }
    }

    @Override
    public Map unsubscribeAlarm(ESight eSight, OpenIdProvider openIdProvider, String desc) {
        Map response = new DeleteNotificationCommonAlarm<Map>(eSight, openIdProvider).doCall(eSight.getSystemId(), desc, Map.class);
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
            LOGGER.error(e.getMessage(), e);
            throw new VcenterException(e.getMessage());
        }
    }

    private ComponentState getServerDeviceDetail(ESight eSight, String dn) throws SQLException {
        Map<String, Object> serverDetailMap = null;
        serverDetailMap = GSON.fromJson(serverApiService.queryDeviceDetail(eSight.getHostIp(), dn, globalSession), Map.class);

        if (!isSuccessResponse(serverDetailMap.get("code"))) {
            LOGGER.error("Alarm callback failed to call server detail: " + serverDetailMap);
            throw new VcenterException("Cannot get server detail: " + serverDetailMap);
        }
        List<Map<String, Object>> dataList = (List) (serverDetailMap.get("data"));
        if (dataList == null || dataList.isEmpty()) {
            LOGGER.warn("Alarm doesn't have data. Discard.");
            return null;
        }

        // convert to bean
        ComponentState componentState = new ComponentState();
        for (Map<String, Object> dataMap : dataList) {
            ComponentState tmpComponentState = convertToComponents(eSight.getId(), dn, dataMap, DeviceComponent
                    .getAlarmComponents());
            componentState.getComponentList().addAll(tmpComponentState.getComponentList());
            componentState.getComponentEmpty().addAll(tmpComponentState.getComponentEmpty());
            componentState.getUuidNotReady().addAll(tmpComponentState.getUuidNotReady());
        }
        return componentState;
    }

    @Override
    public void handleAlarm(final String alarmBody, final String fromIP) throws SQLException {
        try {
            VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
            if (vCenterInfo == null || (!vCenterInfo.isState() && !vCenterInfo.isPushEvent())) {
                LOGGER.info("HA is disabled. Discard.");
                return;
            }
            List<Map<String, Object>> alarmList = GSON.fromJson(alarmBody, List.class);
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
                ESight eSight = getESightByIp(fromIP);
                if (null == eSight) {
                    LOGGER.warn("Invalid eSight: " + fromIP);
                    continue;
                }
                if (vCenterInfo.isState()) {
                    putNewAlarm(fromIP, neDN);
                }

                if (vCenterInfo.isPushEvent()) {
                    // 2：清除告警
                    if (optType == 2.0) {
                        LOGGER.warn("OptType is 2. Don't push event queue.");
                        continue;
                    }
                    // 0：不确定 1：紧急 2：重要 3：次要 4：提示 5：已清除
                    int perceivedSeverity = ((Double) elementMap.get("perceivedSeverity"))
                        .intValue();
                    if (perceivedSeverity < 1 || perceivedSeverity > 4) {
                        LOGGER.info("Discard perceivedSeverity value: " + perceivedSeverity);
                        continue;
                    }
                    // 调整3为4方便比较
                    int myPushEventLevel =
                        vCenterInfo.getPushEventLevel() == 3 ? 4 : vCenterInfo.getPushEventLevel();
                    // myPushEventLevel - perceivedSeverity
                    // 1 - 1
                    // 2 - 1,2
                    // 4 - 1,2,3,4
                    // is the severity in scope
                    if (myPushEventLevel < perceivedSeverity) {
                        LOGGER.info("Discard perceivedSeverity not in my scope: " + perceivedSeverity);
                        continue;
                    }
                    // find alarm definition
                    AlarmDefinition alarmDefinition = new AlarmDefinitionConverter()
                        .findAlarmDefinition(((Double) elementMap.get("alarmId")).intValue());
                    if (alarmDefinition == null) {
                        LOGGER.info("Cannot find correct eventTypeID to push. Discard");
                        continue;
                    }
                    putNewEvent(eSight, neDN, alarmDefinition.getEventTypeID());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle alarm", e);
        }
    }

    @Override
    public void syncServerDeviceDetails(ESight eSight, List<String> dnList, OpenIdProvider openIdProvider,
                                        Map<String, String> dnParentDNMap) {
        if (dnList == null) {
            return;
        }
        for (String dn : dnList) {
            try {
                LOGGER.info("Get server details, DN: " + dn);
                ComponentState componentState = getServerDeviceDetail(eSight, dn);
                LOGGER.info(dn + ", Component: " + componentState);
                List<ServerDeviceDetail> deviceDetailList = componentState.getComponentList();
                List<ServerDeviceDetail> parentDeviceDetailList;
                List<ServerDeviceDetail> allDeviceDetailList = new ArrayList<>(deviceDetailList);

                // parent DN
                if (StringUtils.hasLength(dnParentDNMap.get(dn)) && !dnList.contains(dnParentDNMap.get(dn)) ) {
                    String parentDN = dnParentDNMap.get(dn);
                    LOGGER.info(dn + " has parent dn: " + parentDN);
                    ComponentState parentComponentState = getServerDeviceDetail(eSight, parentDN);
                    parentDeviceDetailList = parentComponentState.getComponentList();
                    // don't use parent DN to avoid health update's spread
                    for (ServerDeviceDetail serverDeviceDetail : parentDeviceDetailList) {
                        serverDeviceDetail.setDn(dn);
                    }
                    allDeviceDetailList.addAll(parentDeviceDetailList);
                }

                // 推送健康状态为红色或黄色的，即非0状态
                List<ServerDeviceDetail> reqPushList = new LinkedList<>();

                // 部件全部不在位则推送一个红色告警
                // componentHasPresentState: 部件-是否有在位的
                Map<String, Boolean> componentHasPresentState = new HashMap<>();
                for (ServerDeviceDetail serverDeviceDetail : allDeviceDetailList) {
                    String component = serverDeviceDetail.getComponent();
                    if (componentHasPresentState.containsKey(component)) {
                        if (!componentHasPresentState.get(component)
                                && !"-1".equals(serverDeviceDetail.getHealthState())) {
                            componentHasPresentState.put(component, Boolean.TRUE);
                        }
                    } else {
                        componentHasPresentState.put(component, !"-1".equals(serverDeviceDetail.getHealthState()));
                    }

                    if (!"0".equals(serverDeviceDetail.getHealthState())) {
                        reqPushList.add(serverDeviceDetail);
                    }
                }
                LOGGER.info("Components have present state: " + componentHasPresentState);
                // 手动构建红色告警推送部件全不在位情况
                for (Map.Entry<String, Boolean> entry : componentHasPresentState.entrySet()) {
                    if (!entry.getValue()) {
                        ServerDeviceDetail aNotPresentState = new ServerDeviceDetail(eSight.getId(), dn, entry.getKey());
                        // 改为红色告警
                        aNotPresentState.setHealthState("4");
                        reqPushList.add(aNotPresentState);
                    }
                }

                LOGGER.info("reqPushList size: " + reqPushList.size());
                if (!reqPushList.isEmpty()) {
                    List<ESightHAServer> eSightHAServerList = eSightHAServerService
                            .getESightHAServersByDN(eSight.getId(), dn);
                    LOGGER.info("eSightHAServerList size: " + eSightHAServerList.size());
                    boolean pushSuccess = vCenterHAService.pushHealth(eSightHAServerList, reqPushList);
                    LOGGER.info("pushHealthSuccess: " + pushSuccess);
                }

                notificationAlarmDao.updateDeviceDetail(eSight.getId(), dn, deviceDetailList);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Set<String> getESightHostIdAndDNs() throws SQLException {
        return notificationAlarmDao.getESightHostIdAndDNs();
    }

    @Override
    public Boolean uninstallProvider() {
        try {
            return vCenterHAService.removeProvider(vCenterInfoService.getVCenterInfo());
        } catch (SQLException e) {
            LOGGER.info("Failed to get vcenter info", e);
            return false;
        }
    }

    private static List<ESightHAServer> noDuplicated(List<ESightHAServer> eSightHAServers) {
        Set<Integer> ids = new HashSet<>();
        List<ESightHAServer> newESightHAServers = new ArrayList<>();
        for (ESightHAServer eSightHAServer : eSightHAServers) {
            if (!ids.contains(eSightHAServer.getId())) {
                ids.add(eSightHAServer.getId());
                newESightHAServers.add(eSightHAServer);
            }
        }
        return newESightHAServers;
    }

    private void pushAlarm(Map.Entry<String, Counter> entry) {
        boolean pushSuccess = false;
        String[] keys = entry.getKey().split(",,,");
        ESight eSight = null;
        String dn;
        boolean hasInvalidStateComponent = false;
        try {
            eSight = getESightByIp(keys[0]);
            dn = keys[1];
            if (eSight == null) {
                throw new NoEsightException();
            }
            List<ESightHAServer> eSightHAServers = eSightHAServerService.getESightHAServersByDN(eSight.getId(), dn);
            // 高密类型如果当前DN未同步，还需要判断管理板部件变化，有变化的需要推送到已同步的子板
            if (eSightHAServers.isEmpty()) {
                ESightHAServer eSightHAServer = eSightHAServerService.getESightHAServerByDN(eSight.getId(), dn);
                LOGGER.info("[Polling]Not a synchronized server: " + (eSightHAServer == null ? entry.getKey() : eSightHAServer));
                if (eSightHAServer != null
                    && ESightServerType.HIGH_DENSITY.value().equalsIgnoreCase(eSightHAServer.geteSightServerType())
                    && StringUtils.hasLength(eSightHAServer.geteSightServerParentDN())) {
                    // 高密：添加所有已同步的子板
                    eSightHAServers.addAll(eSightHAServerService.getESightHAServersByDN(eSight.getId(),
                        eSightHAServer.geteSightServerParentDN()));
                    // 去重
                    eSightHAServers = noDuplicated(eSightHAServers);
                }
            } else if (ESightServerType.HIGH_DENSITY.value().equalsIgnoreCase(eSightHAServers.get(0).geteSightServerType())
                && StringUtils.hasLength(eSightHAServers.get(0).geteSightServerParentDN())) {
                // 高密：添加所有已同步的子板
                eSightHAServers.addAll(eSightHAServerService.getESightHAServersByDN(eSight.getId(),
                    eSightHAServers.get(0).geteSightServerParentDN()));
                // 去重
                eSightHAServers = noDuplicated(eSightHAServers);
            }

            if (eSightHAServers.isEmpty()) {
                LOGGER.info("[Polling]Discard server hasn't sync, ESight: " + eSight
                    + ", dn: " + dn);
                return;
            }
            LOGGER.info("[Polling]eSightHAServers: " + eSightHAServers);

            // DNs need to query server details
            Set<String> affectedDNs = new HashSet<>();
            ESightHAServer haServerInstance = eSightHAServers.get(0);
            String serverType = haServerInstance.geteSightServerType();
            String parentDN = haServerInstance.geteSightServerParentDN();
            boolean isHighDensity = false;
            boolean isBlade = false;
            if (ESightServerType.RACK.value().equalsIgnoreCase(serverType)) {
                affectedDNs.add(haServerInstance.geteSightServerDN());
                if (StringUtils.hasLength(parentDN)) {
                    affectedDNs.add(parentDN);
                }
            } else if (ESightServerType.HIGH_DENSITY.value().equalsIgnoreCase(serverType)) {
                isHighDensity = true;
                affectedDNs.add(haServerInstance.geteSightServerDN());
                if (StringUtils.hasLength(parentDN)) {
                    affectedDNs.add(parentDN);
                }
            } else if (ESightServerType.BLADE.value().equalsIgnoreCase(serverType)) {
                isBlade = true;
                affectedDNs.add(dn);
            } else {
                affectedDNs.add(dn);
            }
            LOGGER.info("[Polling]DNs: " + affectedDNs);

            List<ServerDeviceDetail> pushList = new ArrayList<>();
            List<ServerDeviceDetail> fullDeviceDetailList = new ArrayList<>();
            boolean hasFirstData = false;
            Set<String> noStateComponents = new HashSet<>();
            Set<String> notReadyUUIDs = new HashSet<>();
            for (String affectedDN : affectedDNs) {
                ComponentState componentState = getServerDeviceDetail(eSight, affectedDN);
                LOGGER.info(affectedDN + ", Component: " + componentState);
                List<ServerDeviceDetail> deviceDetailList = componentState.getComponentList();
                noStateComponents.addAll(componentState.getComponentEmpty());
                notReadyUUIDs.addAll(componentState.getUuidNotReady());

                fullDeviceDetailList.addAll(deviceDetailList);
                boolean isFirstData = notificationAlarmDao.getServerDeviceDetailCount(eSight.getId(), Collections.singletonList(affectedDN)) == 0;

                // 判断是否为首次
                if (isFirstData) {
                    hasFirstData = true;
                    for (ServerDeviceDetail serverDeviceDetail : deviceDetailList) {
                        // 首次推送非正常状态部件
                        if (!"0".equals(serverDeviceDetail.getHealthState())) {
                            pushList.add(serverDeviceDetail);
                        }
                    }
                    LOGGER.info("[Polling]Server device detail initial size: " + pushList.size() + ", data: " + pushList);
                } else {
                    // 取状态变化的
                    Collection<String> turnGreenComponents;
                    List<ServerDeviceDetail> diffDeviceDetailList;
                    try {
                        Pair<Collection<String>, List<ServerDeviceDetail>> resultPair = notificationAlarmDao
                            .getServerDeviceDetailDiff(deviceDetailList);
                        turnGreenComponents = resultPair.getKey(); // 绿色告警
                        diffDeviceDetailList = resultPair.getValue(); // 非绿告警
                        LOGGER.info("Green: " + turnGreenComponents + ", Non-Green: " + diffDeviceDetailList);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        continue;
                    }
                    // 添加恢复告警
                    for (String resumeAlarmComponent : turnGreenComponents) {
                        pushList.add(new ServerDeviceDetail(eSight.getId(), affectedDN,
                            resumeAlarmComponent, null, "0", "1"));
                    }
                    // 添加故障告警
                    pushList.addAll(diffDeviceDetailList);

                    LOGGER.info("[Polling]Server device detail different size: " + pushList.size() + ", data: " + pushList);
                }
            }

            // 部件全部不在位则推送一个红色告警
            // componentHasPresentState: 部件-是否有在位的
            Map<String, Boolean> componentHasPresentState = new HashMap<>();
            for (ServerDeviceDetail serverDeviceDetail : fullDeviceDetailList) {
                String component = serverDeviceDetail.getComponent();
                if (componentHasPresentState.containsKey(component)) {
                    if (!componentHasPresentState.get(component)
                        && !"-1".equals(serverDeviceDetail.getHealthState())) {
                        componentHasPresentState.put(component, Boolean.TRUE);
                    }
                } else {
                    componentHasPresentState.put(component, !"-1".equals(serverDeviceDetail.getHealthState()));
                }
            }
            LOGGER.info("Components have present state: " + componentHasPresentState);
            // 手动构建红色告警推送部件全不在位情况
            // 将要推送的部件
            Collection<String> notPresentStateComponents = notificationAlarmDao.getNotPresentStateComponents();
            LOGGER.info("All not present state components: " + notPresentStateComponents);
            for (Map.Entry<String, Boolean> sdd : componentHasPresentState.entrySet()) {
                // 前次没推送过(数据库部件非全-1)才推送全不在位告警
                if (!sdd.getValue() && !notPresentStateComponents.contains(sdd.getKey())) {
                    ServerDeviceDetail aNotPresentState = null;
                    if ((isHighDensity || isBlade) && ("Fan".equalsIgnoreCase(sdd.getKey()) || "PSU"
                        .equalsIgnoreCase(sdd.getKey())) && StringUtils.hasLength(parentDN)) {
                        aNotPresentState = new ServerDeviceDetail(eSight.getId(), parentDN, sdd.getKey(), null, "4", "0");
                    } else {
                        aNotPresentState = new ServerDeviceDetail(eSight.getId(), dn, sdd.getKey(), null, "4", "0");
                    }
                    pushList.add(aNotPresentState);
                }
            }

            // 非首次数据：部件无变化，不推送，不更新数据库部件信息
            if (!hasFirstData && pushList.isEmpty()) {
                return;
            }

            hasInvalidStateComponent = (!noStateComponents.isEmpty() || !notReadyUUIDs.isEmpty());

            // push
            pushSuccess = vCenterHAService.pushHealth(eSightHAServers, pushList);
            if (pushSuccess) {
                if (hasInvalidStateComponent) {
                    // 忽略-2和空部件，更新其他部件
                    List<String> validComponents = new ArrayList<>(Arrays.asList(DeviceComponent.getAlarmComponents()));
                    validComponents.removeAll(noStateComponents);
                    notificationAlarmDao.updateDeviceDetail(eSight.getId(), affectedDNs, fullDeviceDetailList, validComponents, notReadyUUIDs);
                } else {
                    notificationAlarmDao.updateDeviceDetail(eSight.getId(), affectedDNs, fullDeviceDetailList);
                }
            }
        } catch (Exception e) {
            pushSuccess = false;
            LOGGER.error("[Polling]Failed to polling component", e);
        } finally {
            if (eSight == null) {
                this.pollingDNMap.remove(entry.getKey());
            } else if (pushSuccess && !hasInvalidStateComponent) {
                entry.getValue().decreaseAlarmCount();
                deleteNoCount(entry.getKey());
            } else {
                entry.getValue().decreaseRetryCount();
                deleteNoCount(entry.getKey());
            }
        }
    }

    private void pushEvent(Map.Entry<String, Counter> entry, VCenterInfo vCenterInfo) {
        String[] keys = entry.getKey().split(",,,");
        String haHostSystem = keys[0];
        String eventTypeId = keys[1];
        while (entry.getValue().getAlarmCount().intValue() > 0) {
            try {
                ConnectedVim connectedVim = vCenterHAService.getConnectedVim();
                connectedVim.postEvent(vCenterInfo, eventTypeId, haHostSystem);
            } catch (Exception e) {
                LOGGER.error("Failed to push event " + eventTypeId, e);
            } finally {
                entry.getValue().decreaseAlarmCount();
            }
        }
        deleteNoCount(entry.getKey());
    }

    @Override
    public void pollingComponent() {
        synchronized (globalSession) {
            LOGGER.info("[Polling]" + this.pollingDNMap);
            if (pollingDNMap.isEmpty()) {
                return;
            }
            VCenterInfo vCenterInfo = null;
            try {
                vCenterInfo = vCenterInfoService.getVCenterInfo();
            } catch (SQLException e) {
                LOGGER.info("Failed to get vcenter info", e);
            }
            if (vCenterInfo == null) {
                LOGGER.info("No vCenterInfo found. Discard.");
                for (Map.Entry<String, Counter> entry : pollingDNMap.entrySet()) {
                    this.pollingDNMap.remove(entry.getKey());
                }
                return;
            }
            for (Map.Entry<String, Counter> entry : pollingDNMap.entrySet()) {
                LOGGER.info("[Polling]start " + entry);
                String[] keys = entry.getKey().split(",,,");
                if (keys[0].matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                    pushAlarm(entry);
                } else {
                    pushEvent(entry, vCenterInfo);
                }
            }
        }
    }

    @Override
    public void putAlarmIfNotExist(String eSightIP, String dn, int retryCount) {
        String key = eSightIP + ",,," + dn;
        if (!pollingDNMap.containsKey(key)) {
            pollingDNMap.put(key, new Counter(new AtomicInteger(INIT_ALARM_COUNT), new AtomicInteger(1)));
        }
    }

    @Override
    public int deleteNotSyncedDeviceDetails() {
        try {
            return notificationAlarmDao.deleteNotSyncedDeviceDetails();
        } catch (SQLException e) {
            throw new VcenterException("Can't not delete device details");
        }
    }

    private void deleteNoCount(String key) {
        Counter counter = pollingDNMap.get(key);
        if (counter.getAlarmCount().intValue() < 1 || counter.getRetryCount().intValue() < 1) {
            pollingDNMap.remove(key);
        }
    }

    private void putNewEvent(ESight eSight, String dn, String eventTypeId) {
        try {
            ESightHAServer eSightHAServer = eSightHAServerService
                .getESightHAServerByDN(eSight.getId(), dn);
            if (eSightHAServer == null || eSightHAServer.getStatus() != 1) {
                LOGGER.warn("DN is not sync: " + dn);
                return;
            }
            String haHostSystem = eSightHAServer.getHaHostSystem();
            String key = haHostSystem + ",,," + eventTypeId;
            if (pollingDNMap.containsKey(key)) {
                pollingDNMap.get(key).increaseAlarmCount();
            } else {
                pollingDNMap.put(key, new Counter(new AtomicInteger(INIT_EVENT_COUNT),
                    new AtomicInteger(INIT_EVENT_RETRY_COUNT)));
            }
        } catch (SQLException e) {
            LOGGER.info("Failed to put event, cannot getESightHAServersByDN: " + dn);
        }
    }

    private synchronized void putNewAlarm(String eSightIP, String dn) {
        String key = eSightIP + ",,," + dn;
        if (pollingDNMap.containsKey(key)) {
            pollingDNMap.get(key).increaseAlarmCount();
            pollingDNMap.get(key).resetRetryCount();
        } else {
            pollingDNMap.put(key,
                new Counter(new AtomicInteger(INIT_ALARM_COUNT), new AtomicInteger(pollingTimes)));
        }
    }

    @Override
    public void unsubscribeAll() {
        List<ESight> eSightList;
        try {
            eSightList = eSightDao.getAllESights();
            for (ESight eSight : eSightList) {
                try {
                    eSightHAServerService.deleteAll(eSight.getId());
                    if (!"1".equals(eSight.getReservedInt1())) {
                        continue;
                    }
                    LOGGER.info("unsubscribe by deployer: " + eSight);
                    unsubscribeAlarm(eSight, new SessionOpenIdProvider(eSight, globalSession), "unsubscribe_by_deployer");
                } catch (Exception e) {
                    LOGGER.error("Failed to unsubscribe alarm: " + eSight, e);
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private ComponentState convertToComponents(int esightId,
                                                         String dn,
                                                         Map<String, Object> dataMap,
                                                         String... componentNames) {
        ComponentState componentState = new ComponentState();
        for (String componentName : componentNames) {
            if (dataMap.containsKey(componentName)) {
                Object componentObj = dataMap.get(componentName);
                if (componentObj instanceof List) {
                    if (((List<Map<String, Object>>) componentObj).isEmpty()) {
                        LOGGER.info(componentName + " is empty");
                        componentState.addEmptyComponent(componentName);
                        continue;
                    }
                    for (Map<String, Object> componentMap : (List<Map<String, Object>>) componentObj) {
                        if ("-2".equals(String.valueOf(componentMap.get("healthState")).split("\\.")[0])) {
                            String uuid = String.valueOf(componentMap.get("uuid"));
                            LOGGER.info(uuid + " has -2 health state");
                            componentState.addUUIDNotReady(uuid);
                        }
                        componentState.addComponent(new ServerDeviceDetail(esightId, dn, componentName, componentMap));
                    }
                }
            }
        }
        return componentState;
    }

    class ComponentState {
        private Set<String> uuidNotReady = new HashSet<>();
        private Set<String> componentEmpty = new HashSet<>();
        private List<ServerDeviceDetail> componentList = new ArrayList<>();
        public void addComponent(ServerDeviceDetail serverDeviceDetail) {
            componentList.add(serverDeviceDetail);
        }
        public void addEmptyComponent(String component) {
            componentEmpty.add(component);
        }
        public void addUUIDNotReady(String uuid) {
            uuidNotReady.add(uuid);
        }
        public Set<String> getUuidNotReady() {
            return uuidNotReady;
        }
        public Set<String> getComponentEmpty() {
            return componentEmpty;
        }
        public List<ServerDeviceDetail> getComponentList() {
            return componentList;
        }
        @Override
        public String toString() {
            return "ComponentState{" +
                    "uuidNotReady=" + uuidNotReady +
                    ", componentEmpty=" + componentEmpty +
                    ", componentList=" + componentList.size() +
                    '}';
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
            return "https://" + vCenterInfo.getHostIp() + "/vsphere-client/vcenterpluginui/rest/services/notification";
        }
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

    public int getPollingTimes() {
        return pollingTimes;
    }

    public void setPollingTimes(int pollingTimes) {
        this.pollingTimes = pollingTimes;
    }

}
