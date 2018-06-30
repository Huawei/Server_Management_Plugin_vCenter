package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.DefaultOpenIdProvider;
import com.huawei.vcenterpluginui.constant.ESightServerType;
import com.huawei.vcenterpluginui.dao.ESightDao;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.provider.SessionOpenIdProvider;
import com.huawei.vcenterpluginui.utils.CommonUtils;
import com.huawei.vcenterpluginui.utils.OpenIdSessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Rays on 2018/4/8.
 */
public class SyncServerHostServiceImpl implements SyncServerHostService {

    private static final Log LOGGER = LogFactory.getLog(SyncServerHostService.class);

    @Autowired
    private ESightDao eSightDao;

    @Autowired
    private ESightHAServerService eSightHAServerService;

    @Autowired
    private VCenterInfoService vCenterInfoService;

    @Autowired
    private ServerApiService serverApiService;

    @Autowired
    private VCenterHAService vCenterHAService;

    @Autowired
    private NotificationAlarmService notificationAlarmService;

    private HttpSession globalSession = OpenIdSessionManager.getGlobalSession();

    @Override
    public void syncServerHost(boolean subscribeAlarm) {
        synchronized (globalSession) {
            LOGGER.info("sync server start.");
            Map<ESight, DefaultOpenIdProvider> eSightDefaultOpenIdProviderMap = null;
            try {
                // 获取eSight列表
                List<ESight> allESights = eSightDao.getAllESights();
                LOGGER.info("eSight list size: " + allESights.size());
                if (allESights.isEmpty()) {
                    return;
                }

                // 获取vCenter配置信息
                VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
                if (vCenterInfo == null) {
                    LOGGER.info("vCenter info not exist.");
                    return;
                } else if (!vCenterInfo.isState()) {
                    LOGGER.info("vCenter info is disabled.");
                    return;
                }

                // 获取eSight下的服务器列表
                Set<String> eSightFailSet = new HashSet<>();
                List<ESightHAServer> eSightServerList = new LinkedList<>();
                eSightDefaultOpenIdProviderMap = new HashMap<>();
                for (ESight eSight : allESights) {
                    SessionOpenIdProvider openIdProvider = new SessionOpenIdProvider(eSight, globalSession);
                    eSightDefaultOpenIdProviderMap.put(eSight, openIdProvider);
                    LOGGER.debug(String.format("eSight id: %d, hostIp: %s", eSight.getId(), eSight.getHostIp()));
                    Map<String, List<ESightHAServer>> eSightAllServerList = getESightAllServerList(eSight, openIdProvider);
                    for (Map.Entry<String, List<ESightHAServer>> entry : eSightAllServerList.entrySet()) {
                        if (entry.getValue() == null) {
                            eSightFailSet.add(entry.getKey());
                        } else {
                            eSightServerList.addAll(entry.getValue());
                        }
                    }
                }
                LOGGER.info("eSight server list: " + eSightServerList.size());
                LOGGER.info("get eSight server fail set: " + eSightFailSet.size());

                // 获取vCenter下的服务器列表
                List<ESightHAServer> haServerList = vCenterHAService.getServerList(vCenterInfo);
                LOGGER.info("vCenter HA server list: " + haServerList.size());

                // 合并eSight和vCenter的服务器
                Map<String, ESightHAServer> remoteUuidServerMap = mergeRemoteServer(eSightServerList, haServerList);
                LOGGER.info("merge remote server map: " + remoteUuidServerMap.size());

                // 本地服务器列表
                List<ESightHAServer> localESightHAServers = eSightHAServerService.getAllESightHAServers();
                LOGGER.info("local server list: " + localESightHAServers.size());

                // 与本地数据库合并
                Map<String, ESightHAServer> localUuidServerMap = toUuidMap(localESightHAServers);
                localUuidServerMap = mergeLocalServer(remoteUuidServerMap, localUuidServerMap, eSightFailSet);
                LOGGER.info("merge remote to local map: " + localUuidServerMap.size());

                // 排除失效的
                List<ESightHAServer> result = removeRelationElement(localUuidServerMap);
                LOGGER.info("result: " + result.size());
                if (result.isEmpty()) {
                    eSightHAServerService.deleteAll(null);
                    return;
                }

                // 保存到数据库
                List<ESightHAServer> eSightHAServers = eSightHAServerService.deleteAllAndBatchAdd(result);
                LOGGER.info("save list: " + eSightHAServers);

                // 删除未同步dn的部件详情
                deleteNotSyncedDeviceDetails();

                // 同步服务器详情
                syncServerDeviceDetails(eSightHAServers, eSightDefaultOpenIdProviderMap);

                // 同步部件（发部件轮询任务）
                putAlarmTask(eSightHAServers, allESights);
            } catch (Exception e) {
                LOGGER.error("sync server exception.", e);
            } finally {
                if (eSightDefaultOpenIdProviderMap != null && subscribeAlarm) {
                    subscribeAlarm(eSightDefaultOpenIdProviderMap);
                }
                LOGGER.info("sync server end.");
            }
        }
    }

    private void deleteNotSyncedDeviceDetails() {
        try {
            int result = notificationAlarmService.deleteNotSyncedDeviceDetails();
            LOGGER.info("Deleted " + result + " not-synced device details.");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void putAlarmTask(List<ESightHAServer> eSightHAServers, List<ESight> eSightList) {
        Map<Integer, String> esightIdIPMap = new HashMap<>();
        for (ESight eSight : eSightList) {
            esightIdIPMap.put(eSight.getId(), eSight.getHostIp());
        }
        for (ESightHAServer eSightHAServer : eSightHAServers) {
            String esightIp = esightIdIPMap.get(eSightHAServer.geteSightHostId());
            String dn = eSightHAServer.geteSightServerDN();
            // 发子板任务
            notificationAlarmService.putAlarmIfNotExist(esightIp, dn, 1);

            // 刀片手动发父板任务
            String serverType = eSightHAServer.geteSightServerType();
            String parentDN = eSightHAServer.geteSightServerParentDN();
            if (ESightServerType.BLADE.value().equalsIgnoreCase(serverType)
                    && StringUtils.hasLength(parentDN)) {
                notificationAlarmService.putAlarmIfNotExist(esightIp, parentDN, 1);
            }
        }
    }

    @Override
    public void syncServerHost() {
        syncServerHost(false);
    }

    private void syncServerDeviceDetails(List<ESightHAServer> eSightHAServers, Map<ESight, DefaultOpenIdProvider>
            eSightDefaultOpenIdProviderMap) throws SQLException {
        Set<String> eSightHostIdAndDNs = notificationAlarmService.getESightHostIdAndDNs();
        LOGGER.info("eSightHostIdAndDNs: " + eSightHostIdAndDNs.size());

        Map<Integer, Set<String>> eSightDNMap = new HashMap<>();

        Map<String, String> dnParentDNMap = new HashMap<>();
        for (ESightHAServer eSightHAServer : eSightHAServers) {
            if (eSightHAServer.getStatus() == ESightHAServer.STATUS_ALREADY_SYNC) {
                dnParentDNMap.put(eSightHAServer.geteSightServerDN(), eSightHAServer.geteSightServerParentDN());
                Set<String> dnSet = eSightDNMap.get(eSightHAServer.geteSightHostId());
                if (dnSet == null) {
                    dnSet = new HashSet<>();
                    eSightDNMap.put(eSightHAServer.geteSightHostId(), dnSet);
                }

                addChildDN(eSightHAServer.geteSightHostId(), eSightHAServer.geteSightServerDN(), eSightHostIdAndDNs,
                        dnSet);
                if (StringUtils.hasLength(eSightHAServer.geteSightServerParentDN())) {
                    addChildDN(eSightHAServer.geteSightHostId(), eSightHAServer.geteSightServerParentDN(),
                            eSightHostIdAndDNs, dnSet);
                }
            }
        }
        LOGGER.info("eSightDNMap: " + eSightDNMap);

        for (Map.Entry<ESight, DefaultOpenIdProvider> entry : eSightDefaultOpenIdProviderMap.entrySet()) {
            ESight eSight = entry.getKey();
            Set<String> dnSet = eSightDNMap.get(eSight.getId());
            if (dnSet != null && !dnSet.isEmpty()) {
                notificationAlarmService.syncServerDeviceDetails(eSight, new ArrayList<>(dnSet), entry.getValue(),
                        dnParentDNMap);
            }

        }
    }

    private void addChildDN(int eSightHostId, String dn, Set<String> eSightHostIdAndDNs, Set<String> dnSet) {
        String eSightHostIdAndDN = CommonUtils.concatESightHostIdAndDN(eSightHostId, dn);
        if (!eSightHostIdAndDNs.contains(eSightHostIdAndDN)) {
            dnSet.add(dn);
        }
    }

    private void subscribeAlarm(Map<ESight, DefaultOpenIdProvider> eSightDefaultOpenIdProviderMap) {
        LOGGER.info("subscribe alarm...");
        for (Map.Entry<ESight, DefaultOpenIdProvider> entry : eSightDefaultOpenIdProviderMap.entrySet()) {
            if (!"1".equals(entry.getKey().getReservedInt1())) {
                subscribeAlarm(entry.getKey(), entry.getValue());
            }
        }
    }

    private void subscribeAlarm(ESight eSight, DefaultOpenIdProvider openIdProvider) {
        try {
            Map map = notificationAlarmService.subscribeAlarm(eSight, openIdProvider, null);
            String code = String.valueOf(map.get("code"));
            LOGGER.info(String.format("subscribe alarm, code: %s, hostIp: %s", code, eSight.getHostIp()));
        } catch (Exception e) {
            LOGGER.warn(String.format("subscribe alarm fail, hostIp: %s, errorMsg: %s", eSight.getHostIp(),
                    e.getMessage()));
        }
    }

    private List<ESightHAServer> removeRelationElement(Map<String, ESightHAServer> localUuidServerMap) {
        List<ESightHAServer> result = new ArrayList<>(localUuidServerMap.values());
        Iterator<ESightHAServer> iterator = result.iterator();
        while (iterator.hasNext()) {
            ESightHAServer eSightHAServer = iterator.next();
            if (eSightHAServer.geteSightHostId() == 0) {
                iterator.remove();
            }
        }
        return result;
    }

    private Map<String, ESightHAServer> mergeRemoteServer(List<ESightHAServer> eSightServerList, List<ESightHAServer>
            haServerList) {
        Map<String, ESightHAServer> result = toUuidMap(eSightServerList);

        for (ESightHAServer haServer : haServerList) {
            ESightHAServer eSightHAServer = result.get(haServer.getUuid());
            if (eSightHAServer == null) {
                result.put(haServer.getUuid(), haServer);
            } else if (eSightHAServer.geteSightHostId() > 0) {
                eSightHAServer.setStatus(ESightHAServer.STATUS_ALREADY_SYNC);
                eSightHAServer.setHaHostSystem(haServer.getHaHostSystem());
            }
        }

        return result;
    }

    private Map<String, ESightHAServer> toUuidMap(List<ESightHAServer> list) {
        Map<String, ESightHAServer> result = new HashMap<>();
        for (ESightHAServer eSightHAServer : list) {
            result.put(eSightHAServer.getUuid(), eSightHAServer);
        }
        return result;
    }

    private Map<String, ESightHAServer> mergeLocalServer(Map<String, ESightHAServer> remoteUuidServerMap,
            Map<String, ESightHAServer> localUuidServerMap,
            Set<String> eSightFailSet) {
        Set<String> uuidSet = new HashSet<>(remoteUuidServerMap.keySet());
        uuidSet.addAll(localUuidServerMap.keySet());
        for (String uuid : uuidSet) {
            localUuidServerMap.put(uuid, mergeESightHAServer(remoteUuidServerMap.get(uuid), localUuidServerMap.get
                    (uuid), eSightFailSet));
        }
        return localUuidServerMap;
    }

    private ESightHAServer mergeESightHAServer(ESightHAServer remote, ESightHAServer local, Set<String> eSightFailSet) {
        if (local == null) {
            return remote;
        } else if (remote == null) {
            if (!isESightFail(eSightFailSet, local)) {
                switch (local.getStatus()) {
                    case ESightHAServer.STATUS_ALREADY_SYNC:
                    case ESightHAServer.STATUS_ESIGHT_DISABLE:
                    case ESightHAServer.STATUS_HA_DISABLE:
                        local.setStatus(ESightHAServer.STATUS_ESIGHT_AND_HA_DISABLE);
                        break;
                }
            }
        } else {

            if (remote.getStatus() == ESightHAServer.STATUS_ALREADY_SYNC) {
                local.setStatus(ESightHAServer.STATUS_ALREADY_SYNC);
            } else {
                final boolean fromESight = remote.geteSightHostId() > 0;
                if (!(!fromESight && isESightFail(eSightFailSet, local))) {
                    switch (local.getStatus()) {
                        case ESightHAServer.STATUS_ALREADY_SYNC:
                        case ESightHAServer.STATUS_ESIGHT_AND_HA_DISABLE:
                            local.setStatus(fromESight ? ESightHAServer.STATUS_HA_DISABLE : ESightHAServer
                                    .STATUS_ESIGHT_DISABLE);
                            break;
                        case ESightHAServer.STATUS_ESIGHT_DISABLE:
                            if (fromESight) {
                                local.setStatus(ESightHAServer.STATUS_HA_DISABLE);
                            }
                            break;
                        case ESightHAServer.STATUS_HA_DISABLE:
                            if (!fromESight) {
                                local.setStatus(ESightHAServer.STATUS_ESIGHT_DISABLE);
                            }
                            break;
                    }
                }
            }

            if (remote.geteSightHostId() > 0) {
                local.seteSightHostId(remote.geteSightHostId());
                local.seteSightServerStatus(remote.geteSightServerStatus());
                local.seteSightServerType(remote.geteSightServerType());
                local.seteSightServerDN(remote.geteSightServerDN());
                local.seteSightServerParentDN(remote.geteSightServerParentDN());
            }
            if (remote.getHaHostSystem() != null) {
                local.setHaHostSystem(remote.getHaHostSystem());
            }
        }

        return local;
    }

    /**
     * 获取eSight下服务器类型为"rack", "blade", "highdensity", "kunlun"的服务器列表
     */
    private Map<String, List<ESightHAServer>> getESightAllServerList(ESight eSight, DefaultOpenIdProvider
            openIdProvider) {
        return serverApiService.getESightAllServerList(eSight, openIdProvider, ESightServerType.getServerTypesBySync());
    }

    private boolean isESightFail(Set<String> eSightFailSet, ESightHAServer eSightHAServer) {
        return eSightFailSet.contains(serverApiService.getESightFailKey(eSightHAServer.geteSightHostId(),
                eSightHAServer.geteSightServerType()));
    }
}
