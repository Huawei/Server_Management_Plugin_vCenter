package com.huawei.vcenterpluginui.utils;

import com.huawei.vcenterpluginui.constant.DeviceComponent;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.exception.VersionNotSupportException;
import com.vmware.common.Main;
import com.vmware.common.ssl.TrustAll;
import com.vmware.connection.BasicConnection;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.connection.Connection;
import com.vmware.vim25.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Rays on 2018/4/9.
 */
public class ConnectedVim extends ConnectedVimServiceBase {

    private static final Log LOGGER = LogFactory.getLog(ConnectedVim.class);

    private static final String DATA_CENTER = "Datacenter";
    private static final String CLUSTER = "ClusterComputeResource";
    private static final String HOST_SYSTEM = "HostSystem";
    private static final String HARDWARE = "hardware";

    private static final String COMPATIBLE_VCENTER_VERSION_SINCE = "6.5.0";

    private String providerNamePrefix;
    private String providerName;

    public ConnectedVim(String providerNamePrefix, String providerNameVersion) {
        super();
        this.providerNamePrefix = providerNamePrefix;
        this.providerName = providerNamePrefix + "." + providerNameVersion;
    }

    /**
     * 获取服务器列表
     */
    public List<ESightHAServer> getServerList(VCenterInfo vCenterInfo) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg, IOException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        connect(vCenterInfo);
        try {
            List<ESightHAServer> result = new LinkedList<>();

            String providerId = null;
            try {
                providerId = queryProviderId(getHealthUpdateManager());
            } catch (RuntimeFaultFaultMsg | NotFoundFaultMsg e) {
                LOGGER.warn("query providerId fail", e);
            }
            final boolean isRequestMonitorHost = StringUtils.hasLength(providerId);
            LOGGER.info("isRequestMonitorHost: " + isRequestMonitorHost);
            List<ManagedObjectReference> monitoredHostList = new LinkedList<>();

            Map<String, ManagedObjectReference> dataCenters = this.getDataCenters();
            for (ManagedObjectReference dataCenter : dataCenters.values()) {
                Map<String, ManagedObjectReference> hostMap = this.getMOREFs.inContainerByType(dataCenter, HOST_SYSTEM);
                for (ManagedObjectReference hostSystem : hostMap.values()) {

                    if (isRequestMonitorHost) {
                        handleNotMonitored(providerId, hostSystem, monitoredHostList);
                    }

                    HostHardwareInfo hardwareInfo = (HostHardwareInfo) this.getDynamicProperty(hostSystem, HARDWARE);
                    String uuid = hardwareInfo.getSystemInfo().getUuid();
                    if (!StringUtils.hasLength(uuid)) {
                        continue;
                    }
                    ESightHAServer eSightHAServer = new ESightHAServer();
                    eSightHAServer.setUuid(CommonUtils.formatUUID(uuid));
                    eSightHAServer.setHaHostSystem(hostSystem.getValue());
                    result.add(eSightHAServer);
                }
            }

            LOGGER.info("monitored host list: " + monitoredHostList.size());
            if (!monitoredHostList.isEmpty()) {
                try {
                    addMonitored(getHealthUpdateManager(), providerId, monitoredHostList);
                } catch (NotFoundFaultMsg | RuntimeFaultFaultMsg e) {
                    LOGGER.warn("add Monitor fail.", e);
                }
            }

            return result;
        } catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | NoSuchMethodException | IllegalAccessException |
                InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            this.disconnect();
        }
    }

    private void handleNotMonitored(String providerId, ManagedObjectReference hostSystem,
            List<ManagedObjectReference> monitoredList) {
        try {
            if (!this.vimPort.hasMonitoredEntity(getHealthUpdateManager(), providerId, hostSystem)) {
                monitoredList.add(hostSystem);
            }
        } catch (NotFoundFaultMsg | RuntimeFaultFaultMsg e) {
            LOGGER.warn("has Monitored exception.", e);
        }
    }

    /**
     * 取消监控同步失败的且已监控的服务器
     *
     * @param vCenterInfo          vCenter账户信息
     * @param removeMonitoredServerList 需取消监控列表
     */
    public void removeMonitored(VCenterInfo vCenterInfo, List<ESightHAServer> removeMonitoredServerList)
            throws IOException, NotFoundFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg {
        connect(vCenterInfo);
        try {
            ManagedObjectReference healthUpdateManager = getHealthUpdateManager();
            String providerId = getProviderId(healthUpdateManager);
            LOGGER.info("providerId: " + providerId);

            List<ManagedObjectReference> removeMonitoredEntities = new LinkedList<>();
            for (ESightHAServer eSightHAServer : removeMonitoredServerList) {
                ManagedObjectReference hostSystem = getHostSystem(eSightHAServer);
                if (this.vimPort.hasMonitoredEntity(healthUpdateManager, providerId, hostSystem)) {
                    removeMonitoredEntities.add(hostSystem);
                    eSightHAServer.setProviderSid(null);
                } else {
                    LOGGER.info("unMonitored: " + eSightHAServer.getUuid());
                }
            }
            LOGGER.info("remove monitored success list size: " + removeMonitoredEntities.size());
            if (!removeMonitoredEntities.isEmpty()) {
                this.vimPort.removeMonitoredEntities(healthUpdateManager, providerId, removeMonitoredEntities);
            }
        } catch (RuntimeFaultFaultMsg | NotFoundFaultMsg | InvalidStateFaultMsg e) {
            LOGGER.warn("remove monitored fail", e);
            throw e;
        } finally {
            this.disconnect();
        }
    }

    /**
     * 推送健康状态
     *
     * @param vCenterInfo         vCenter信息
     * @param serverDeviceDetails 接收的告警信息列表
     */
    public void pushHealth(VCenterInfo vCenterInfo, List<ESightHAServer> eSightHAServers,
            List<ServerDeviceDetail> serverDeviceDetails) throws IOException, RuntimeFaultFaultMsg, NotFoundFaultMsg {
        connect(vCenterInfo);
        try {
            ManagedObjectReference healthUpdateManager = getHealthUpdateManager();
            String providerId = getProviderId(healthUpdateManager);

            List<HealthUpdate> healthUpdates = getHealthUpdates(eSightHAServers, serverDeviceDetails,
                    healthUpdateManager, providerId);
            LOGGER.info("healthUpdates size: " + healthUpdates.size());
            if (healthUpdates.isEmpty()) {
                return;
            }
            this.vimPort.postHealthUpdates(healthUpdateManager, providerId, healthUpdates);
            LOGGER.info("push health update success.");
        } catch (RuntimeFaultFaultMsg | NotFoundFaultMsg e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            this.disconnect();
        }
    }

    private List<HealthUpdate> getHealthUpdates(List<ESightHAServer> eSightHAServers,
            List<ServerDeviceDetail> serverDeviceDetails, ManagedObjectReference healthUpdateManager, String providerId)
            throws NotFoundFaultMsg, RuntimeFaultFaultMsg {
        List<HealthUpdate> healthUpdates = new LinkedList<>();

        Map<String, Set<String>> dnHostSystemMap = new HashMap<>();
        for (ESightHAServer eSightHAServer : eSightHAServers) {
            dnHostSystemMap
                    .put(eSightHAServer.geteSightServerDN(), Collections.singleton(eSightHAServer.getHaHostSystem()));
            if (StringUtils.hasLength(eSightHAServer.geteSightServerParentDN())) {
                Set<String> hostSystems = dnHostSystemMap.get(eSightHAServer.geteSightServerParentDN());
                if (hostSystems == null) {
                    hostSystems = new HashSet<>();
                }
                hostSystems.add(eSightHAServer.getHaHostSystem());
                dnHostSystemMap.put(eSightHAServer.geteSightServerParentDN(), hostSystems);
            }
        }

        IdWorker idWorker = new IdWorker(1, 1, 1);
        Map<String, ManagedObjectReference> hostSystemMap = new HashMap<>();
        for (ServerDeviceDetail serverDeviceDetail : serverDeviceDetails) {
            String healthUpdateInfoId = DeviceComponent.getComponentId(serverDeviceDetail.getComponent());
            if (healthUpdateInfoId == null) {
                LOGGER.warn("push fail, component type error: " + serverDeviceDetail.getComponent());
                continue;
            }

            Set<String> hostSystems = dnHostSystemMap.get(serverDeviceDetail.getDn());
            if (hostSystems == null) {
                LOGGER.warn("push fail, not found host system, dn: " + serverDeviceDetail.getDn());
                continue;
            }
            for (String hostSystemValue : hostSystems) {
                ManagedObjectReference hostSystem = hostSystemMap.get(hostSystemValue);
                if (hostSystem == null) {
                    hostSystem = getHostSystem(hostSystemValue);
                    hostSystemMap.put(hostSystemValue, hostSystem);
                    addMonitored(healthUpdateManager, providerId, hostSystem);
                }

                HealthUpdate healthUpdate = new HealthUpdate();
                healthUpdate.setId(serverDeviceDetail.getComponent() + String.valueOf(idWorker.nextId()));
                healthUpdate.setHealthUpdateInfoId(healthUpdateInfoId);
                healthUpdate.setEntity(hostSystem);
                healthUpdate.setStatus(convertHealthStatus(serverDeviceDetail.getHealthState()));
                if (healthUpdate.getStatus() != ManagedEntityStatus.GREEN) {
                    healthUpdate.setRemediation("please refer to Proposed Repair Actions in eSight");
                } else {
                    healthUpdate.setRemediation("");
                }
                healthUpdates.add(healthUpdate);
            }
        }
        return healthUpdates;
    }

    private void addMonitored(ManagedObjectReference healthUpdateManager, String providerId,
            ManagedObjectReference hostSystem) throws NotFoundFaultMsg, RuntimeFaultFaultMsg {
        if (!this.vimPort.hasMonitoredEntity(healthUpdateManager, providerId, hostSystem)) {
            addMonitored(healthUpdateManager, providerId, Collections.singletonList(hostSystem));
        }
    }

    private void addMonitored(ManagedObjectReference healthUpdateManager, String providerId,
            List<ManagedObjectReference> hostSystem) throws NotFoundFaultMsg, RuntimeFaultFaultMsg {
        this.vimPort.addMonitoredEntities(healthUpdateManager, providerId, hostSystem);
    }

    /**
     * 组件状态转为健康状态
     *
     * @param healthState “0”：正常
     *                    “-1”：离线
     *                    “-2”：未知
     *                    其他：故障
     * @return ManagedEntityStatus
     */
    private ManagedEntityStatus convertHealthStatus(String healthState) {
        switch (healthState) {
            case "0":
                return ManagedEntityStatus.GREEN;
            case "4":
                return ManagedEntityStatus.RED;
            case "2":
            case "3":
            case "5":
                return ManagedEntityStatus.YELLOW;
            default:
                return ManagedEntityStatus.RED;
        }
    }

    private ManagedObjectReference getHostSystem(ESightHAServer eSightHAServer) {
        return getHostSystem(eSightHAServer.getHaHostSystem());
    }

    private ManagedObjectReference getHostSystem(String value) {
        ManagedObjectReference host = new ManagedObjectReference();
        host.setType(HOST_SYSTEM);
        host.setValue(value);
        return host;
    }

    public static void checkVersionCompatible(String version) {
        boolean isCompatibleVersion = true;
        try {
            String[] targetVersionsSince = COMPATIBLE_VCENTER_VERSION_SINCE.split("\\.");
            String[] currentVersions = version.split("\\.");
            for (int i = 0; i < Math.max(targetVersionsSince.length, currentVersions.length); i++) {
                String currentVersion = currentVersions.length - 1 < i ? "0" : currentVersions[i];
                String targetVersion = targetVersionsSince.length - 1 < i ? "0" : targetVersionsSince[i];
                if (Integer.parseInt(currentVersion) < Integer.parseInt(targetVersion)) {
                    isCompatibleVersion = false;
                    break;
                } else if (Integer.parseInt(currentVersion) > Integer.parseInt(targetVersion)) {
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getCause(), e);
        }
        if (!isCompatibleVersion) {
            throw new VersionNotSupportException(version);
        }
    }

    /**
     * create provider if it doesn't exist
     * @param vCenterInfo vCenter account info
     * @param enable Whether to enable
     * @return providerId
     */
    public String createProvider(VCenterInfo vCenterInfo, boolean enable) {
        try {
            connect(vCenterInfo);
            String version = getVersion();
            LOGGER.info("Current version is: " + version);
            checkVersionCompatible(version);

            ManagedObjectReference healthUpdateManager = getHealthUpdateManager();
            String providerId = getProviderId(healthUpdateManager);
            if (enable) {
                reconfigureClusterProvider(providerId, true);
            }
            return providerId;
        } catch (VersionNotSupportException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Failed to create provider", e);
            throw new VcenterException("Cannot create provider");
        } finally {
            disconnect();
        }
    }

    /**
     * remove provider if it exist
     * @param vCenterInfo vCenter account info
     * @return  null: provider not exist<br/>
     *          true: remove success<br/>
     *          false: remove fail or other exception
     */
    public Boolean removeProvider(VCenterInfo vCenterInfo) {
        Boolean result = null;
        try {
            connect(vCenterInfo);
            ManagedObjectReference healthUpdateManager = getHealthUpdateManager();
            List<String> providerIdList = this.vimPort.queryProviderList(healthUpdateManager);
            for (String it : providerIdList) {
                String providerName = this.vimPort.queryProviderName(healthUpdateManager, it);
                if (providerName.startsWith(providerNamePrefix)) {
                    LOGGER.info("unregister health update provider, providerName: " + providerName);
                    result = unregisterHealthUpdateProvider(healthUpdateManager, it);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to remove provider, " + e.getMessage());
            result = false;
        } finally {
            disconnect();
        }
        return result;
    }

    /**
     * 获取providerId，没有就注册生成一个
     */
    private String getProviderId(ManagedObjectReference healthUpdateManager) throws RuntimeFaultFaultMsg,
            NotFoundFaultMsg {
        String providerId = queryProviderId(healthUpdateManager);
        if (providerId == null) {
            providerId = registerHealthUpdateProvider(healthUpdateManager);
        }
        return providerId;
    }

    /**
     * 根据providerName查询providerId
     */
    private String queryProviderId(ManagedObjectReference healthUpdateManager)
            throws RuntimeFaultFaultMsg, NotFoundFaultMsg {
        String providerId = null;
        List<String> providerIdList = this.vimPort.queryProviderList(healthUpdateManager);
        for (String it : providerIdList) {
            String providerName = this.vimPort.queryProviderName(healthUpdateManager, it);
            if (this.providerName.equals(providerName)) {
                providerId = it;
                LOGGER.info("exist providerId: " + providerId);
            } else if (providerName.startsWith(providerNamePrefix)) {
                LOGGER.info("unregister health update provider, providerName: " + providerName);
                unregisterHealthUpdateProvider(healthUpdateManager, it);
            }
        }
        return providerId;
    }

    private boolean unregisterHealthUpdateProvider(ManagedObjectReference healthUpdateManager, String providerId) {
        int count = 0;
        try {
            count = reconfigureClusterProvider(providerId, false);
            LOGGER.info("Modified the configured number of clusters, count: " + count);
        } catch (Exception e) {
            LOGGER.warn("reconfigureClusterProvider Exception, errorMsg: " + e.getMessage());
        }
        // You need to keep some time waiting for the cluster configuration to complete.
        int maxFrequency = count > 0 ? 3 : 1;
        for (int i = 0; i < maxFrequency; i++) {
            try {
                if (count > 0) {
                    Thread.sleep(1000L);
                }
                this.vimPort.unregisterHealthUpdateProvider(healthUpdateManager, providerId);
                LOGGER.info("unregister health update provider success, providerId: " + providerId);
                return true;
            } catch (Exception e) {
                LOGGER.warn(String.format(
                        "unregister health update provider fail, providerId: %s, errorMsg: %s, frequency: %s",
                        providerId, e.getMessage(), i + 1));
            }
        }
        return false;
    }

    private String registerHealthUpdateProvider(ManagedObjectReference healthUpdateManager) throws
            RuntimeFaultFaultMsg {
        LOGGER.info("register health update provider.");
        List<HealthUpdateInfo> healthUpdateInfos = DeviceComponent.getHealthUpdateInfos();
        String providerId = this.vimPort.registerHealthUpdateProvider(healthUpdateManager, this.providerName,
                healthUpdateInfos);
        LOGGER.info("new providerId: " + providerId);
        return providerId;
    }

    /**
     * Configure the Cluster to Enable the Proactive HA Provider
     * @param providerId Proactive HA Provider
     * @param enable Whether to enable
     * @return Modified the configured number of clusters.
     */
    private int reconfigureClusterProvider(String providerId, boolean enable)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        int count = 0;
        LOGGER.info("reconfigureClusterProvider, provider: " + providerId + ", enable: " + enable);
        Map<String, ManagedObjectReference> clusters = this.getMOREFs
                .inContainerByType(this.serviceContent.getRootFolder(), CLUSTER);
        LOGGER.info("clusters size: " + clusters.size());
        for (ManagedObjectReference cluster : clusters.values()) {
            ClusterConfigInfoEx clusterConfigInfoEx = (ClusterConfigInfoEx) this
                    .getDynamicProperty(cluster, "configurationEx");
            ClusterInfraUpdateHaConfigInfo infraUpdateHaConfig = clusterConfigInfoEx.getInfraUpdateHaConfig();
            if (infraUpdateHaConfig.getProviders().contains(providerId) == enable) {
                LOGGER.info("No need to configure, cluster: " + cluster.getValue());
                continue;
            }
            if (enable) {
                infraUpdateHaConfig.getProviders().add(providerId);
            } else {
                infraUpdateHaConfig.getProviders().remove(providerId);
            }

            ClusterConfigSpecEx clusterConfigSpecEx = new ClusterConfigSpecEx();
            clusterConfigSpecEx.setInfraUpdateHaConfig(infraUpdateHaConfig);
            this.vimPort.reconfigureComputeResourceTask(cluster, clusterConfigSpecEx, true);
            LOGGER.info("reconfigureClusterProvider success, cluster: " + cluster.getValue());
            count++;
        }
        return count;
    }

    /**
     * 连接vim
     */
    private void connect(VCenterInfo vCenterInfo) throws IOException {
        connect(vCenterInfo.getHostIp(), vCenterInfo.getUserName(), CipherUtils.aesDncode(vCenterInfo.getPassword()));
    }

    /**
     * 连接vim
     */
    private void connect(String host, String username, String password) throws IOException {
        Connection basicConnection = new BasicConnection();
        URL sdkUrl = new URL("https", host, "/sdk");
        basicConnection.setPassword(password);
        basicConnection.setUrl(sdkUrl.toString());
        basicConnection.setUsername(username);
        this.setIgnoreCert();
        this.setHostConnection(true);
        this.setConnection(basicConnection);
        LOGGER.info("host: " + host + " username: " + username + " password: ******");
        Connection connect = this.connect();
        if (connect == null || !connect.isConnected()) {
            throw new IOException("connect vim fail.");
        }
        LOGGER.info("connect vim success.");
    }

    @Override
    public Connection disconnect() {
        try {
            if (connection != null) {
                return super.disconnect();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to disconnect vcenter, " + e.getMessage());
        }
        return connection;
    }

    private void setIgnoreCert() {
        System.setProperty(Main.Properties.TRUST_ALL, Boolean.TRUE.toString());
        try {
            TrustAll.trust();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 获取数据中心列表
     */
    private Map<String, ManagedObjectReference> getDataCenters() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        return this.getMOREFs.inContainerByType(this.serviceContent.getRootFolder(), DATA_CENTER);
    }

    private ManagedObjectReference getHealthUpdateManager() {
        return this.serviceContent.getHealthUpdateManager();
    }

    private String getVersion() {
        return this.serviceContent.getAbout().getVersion();
    }

    private Object getDynamicProperty(ManagedObjectReference mor, String propertyName) throws
            InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        ObjectContent[] objContent = getObjectProperties(mor, new String[]{propertyName});

        Object propertyValue = null;
        if (objContent != null) {
            List<DynamicProperty> listdp = objContent[0].getPropSet();
            if (listdp != null) {
                Object dynamicPropertyVal = listdp.get(0).getVal();
                String dynamicPropertyName = dynamicPropertyVal.getClass().getName();
                if (dynamicPropertyName.contains("ArrayOf")) {
                    String methodName = dynamicPropertyName.substring(
                            dynamicPropertyName.indexOf("ArrayOf") + "ArrayOf".length(), dynamicPropertyName.length());
                    if (methodExists(dynamicPropertyVal, "get" + methodName)) {
                        methodName = "get" + methodName;
                    } else {
                        methodName = "get_" + methodName.toLowerCase();
                    }
                    Method getMorMethod = dynamicPropertyVal.getClass().getDeclaredMethod(methodName, (Class[]) null);
                    propertyValue = getMorMethod.invoke(dynamicPropertyVal, (Object[]) null);
                } else if (dynamicPropertyVal.getClass().isArray()) {
                    propertyValue = dynamicPropertyVal;
                } else {
                    propertyValue = dynamicPropertyVal;
                }
            }
        }
        return propertyValue;
    }

    /**
     * Determines of a method 'methodName' exists for the Object 'obj'.
     *
     * @param obj        The Object to check
     * @param methodName The method name
     * @return true if the method exists, false otherwise
     */
    @SuppressWarnings("rawtypes")
    private boolean methodExists(Object obj, String methodName) throws NoSuchMethodException {
        boolean exists = false;
        Method method = obj.getClass().getMethod(methodName, (Class[]) null);
        if (method != null) {
            exists = true;
        }
        return exists;
    }

    /**
     * Retrieve contents for a single object based on the property collector
     * registered with the service.
     *
     * @param mobj       Managed Object Reference to get contents for
     * @param properties names of properties of object to retrieve
     * @return retrieved object contents
     */
    private ObjectContent[] getObjectProperties(ManagedObjectReference mobj, String[] properties) throws
            InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        if (mobj == null) {
            return null;
        }

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        PropertySpec propertySpec = spec.getPropSet().get(0);
        if ((properties == null || properties.length == 0)) {
            propertySpec.setAll(Boolean.TRUE);
        } else {
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.getPathSet().addAll(Arrays.asList(properties));
        }
        propertySpec.setType(mobj.getType());
        spec.getObjectSet().add(new ObjectSpec());
        ObjectSpec objectSpec = spec.getObjectSet().get(0);
        objectSpec.setObj(mobj);
        objectSpec.setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> listpfs = new ArrayList<>(1);
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        return listobjcont.toArray(new ObjectContent[0]);
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method
     */
    private List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs) throws
            InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference propCollectorRef = serviceContent.getPropertyCollector();
        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<>();

        RetrieveResult rslts = this.vimPort.retrievePropertiesEx(propCollectorRef, listpfs, propObjectRetrieveOpts);
        if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty()) {
            rslts = this.vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }
        return listobjcontent;
    }
}
