package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.notification.DeleteNotificationSystemKeepAliveApi;
import com.huawei.esight.api.rest.notification.PutNotificationSystemKeepAliveApi;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.provider.SessionOpenIdProvider;
import com.huawei.vcenterpluginui.utils.CipherUtils;
import com.huawei.vcenterpluginui.utils.OpenIdSessionManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SystemKeepAliveServiceImpl extends ESightOpenApiService implements SystemKeepAliveService {

    @Autowired
    private VCenterInfoService vCenterInfoService;

    @Autowired
    private ESightService eSightService;

    @Autowired
    private NotificationAlarmService notificationAlarmService;

    // K-V: eSightIP-count
    private volatile Map<String, Long> KEEP_ALIVE_MAP = new ConcurrentHashMap<>();

    private HttpSession globalSession = OpenIdSessionManager.getGlobalSession();

    @Override
    public Map subscribeSystemKeepAlive(ESight eSight, String openID, OpenIdProvider openIdProvider, String desc) {
        String callbackUrl = getCallbackUrl();
        LOGGER.info("system keep alive callback url: " + callbackUrl);
        Map response = new PutNotificationSystemKeepAliveApi<Map>(eSight, openIdProvider).doCall(eSight.getSystemId(), openID,
                callbackUrl, "JSON", desc, Map.class);
        LOGGER.info("subscribe ska info: " + response);
        if (isSuccessResponse(response.get("code"))) {
            updateLastAliveTime(eSight.getHostIp());
        }
        return response;
    }

    @Override
    public Map unsubscribeSystemKeepAlive(ESight eSight, OpenIdProvider openIdProvider, String desc) {
        Map response = new DeleteNotificationSystemKeepAliveApi<Map>(eSight, openIdProvider).doCall(eSight.getSystemId(), desc, Map.class);
        LOGGER.info("unsubscribe ska info: " + response);
        if (isSuccessResponse(response.get("code"))) {
            KEEP_ALIVE_MAP.remove(eSight.getHostIp());
        }
        return response;
    }

    @Override
    public void handleMessage(final String esightIP) {
        updateLastAliveTime(esightIP);
    }

    @Override
    public void updateLastAliveTime(String eSightIP) {
        KEEP_ALIVE_MAP.put(eSightIP, KEEP_ALIVE_MAP.containsKey(eSightIP) ? KEEP_ALIVE_MAP.get(eSightIP) + 1 : 1);
    }

    @Override
    public void checkSubscription() {
        try {
            VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
            boolean isHAEnabled = (vCenterInfo != null && (vCenterInfo.isState() || vCenterInfo.isPushEvent()));
            if (isHAEnabled) {
                LOGGER.info("[Keepalive]start to check: " + KEEP_ALIVE_MAP);
            } else {
                LOGGER.info("[Keepalive]HA is disabled.");
                return;
            }
            List<ESight> eSightList = eSightService.getESightList(null, -1, -1);
            Set<String> allHost = new HashSet<>();
            for (ESight eSight : eSightList) {
                allHost.add(eSight.getHostIp());
                // the eSights in list don't have password, need to get again
                eSight = getESightByIp(eSight.getHostIp());
                if (!String.valueOf(eSight.getReservedInt1()).equalsIgnoreCase(
                        String.valueOf(eSight.getReservedInt2()))) {
                    boolean result = false;
                    if (eSight.getReservedInt1() == null
                            || "0".equals(eSight.getReservedInt1())
                            || "2".equals(eSight.getReservedInt1())) {
                        Map response = unsubscribeSystemKeepAlive(eSight,
                                new SessionOpenIdProvider(eSight, globalSession),
                                "unsubscribe_by_sync");
                        LOGGER.info("[Keepalive]unsubscribe system keep alive: " + response);
                        result = isSuccessResponse(response.get("code"));
                    } else if ("1".equals(eSight.getReservedInt1())) {
                        Map response = subscribeSystemKeepAlive(eSight, eSight.getHostIp(),
                                new SessionOpenIdProvider(eSight, globalSession),"subscribe_by_sync");
                        LOGGER.info("[Keepalive]subscribe system keep alive: " + response);
                        result = isSuccessResponse(response.get("code"));
                    } else {
                        throw new VcenterException("Invalid HA status: " + eSight.getReservedInt1());
                    }
                    if (result) {
                        eSightService.updateSystemKeepAliveStatus(eSight.getHostIp(), eSight.getReservedInt1());
                    }
                }
                // prepare data for SKA-timeout-checking
                if ("1".equals(eSight.getReservedInt1()) && !KEEP_ALIVE_MAP.containsKey(eSight.getHostIp())) {
                    updateLastAliveTime(eSight.getHostIp());
                }
                if (!"1".equals(eSight.getReservedInt1()) && KEEP_ALIVE_MAP.containsKey(eSight.getHostIp())) {
                    KEEP_ALIVE_MAP.remove(eSight.getHostIp());
                }
            }
            // subscribe timeout entries
            for (Map.Entry<String, Long> entry : KEEP_ALIVE_MAP.entrySet()) {
                // Remove invalid entries
                if (!allHost.contains(entry.getKey())) {
                    LOGGER.info(entry.getKey() + " is not valid, remove it.");
                    KEEP_ALIVE_MAP.remove(entry.getKey());
                    continue;
                }
                if (entry.getValue() < 1L) {
                    LOGGER.info(entry.getKey() + " timeout, start to re-subscribe.");
                    try {
                        ESight eSight = getESightByIp(entry.getKey());
                        notificationAlarmService.subscribeAlarm(eSight,
                                new SessionOpenIdProvider(eSight, globalSession),
                                "subscribe_by_ska");
                    } catch (Exception e) {
                        LOGGER.error("Failed to re-subscribe alarm", e);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Keepalive]Failed to check subscription", e);
        } finally {
            for (Map.Entry<String, Long> listenEntry : KEEP_ALIVE_MAP.entrySet()) {
                listenEntry.setValue(0L);
            }
        }
    }

    private String getCallbackUrl() {
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
            + "/vsphere-client/vcenterpluginui/rest/services/notification/systemKeepAlive";
    }

}
