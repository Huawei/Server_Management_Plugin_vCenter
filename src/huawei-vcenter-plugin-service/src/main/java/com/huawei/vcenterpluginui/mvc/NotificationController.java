package com.huawei.vcenterpluginui.mvc;

import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.services.*;
import com.huawei.vcenterpluginui.utils.CipherUtils;
import com.huawei.vcenterpluginui.utils.OpenIdSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

/**
 * 告警回调 控制层
 *
 * @author Horace
 */
@RequestMapping(value = "/services/notification")
public class NotificationController extends BaseController {

    private NotificationAlarmService notificationAlarmService;

    private SyncServerHostService syncServerHostService;

    private VCenterInfoService vCenterInfoService;

    @Autowired
    private SystemKeepAliveService systemKeepAliveService;

    private HttpSession globalSession = OpenIdSessionManager.getGlobalSession();

    @Autowired
    public NotificationController(@Qualifier("notificationAlarmService") NotificationAlarmService
                                          notificationAlarmService,
                                  @Qualifier("syncServerHostService") SyncServerHostService syncServerHostService,
                                  @Qualifier("vCenterInfoService") VCenterInfoService vCenterInfoService) {
        this.notificationAlarmService = notificationAlarmService;
        this.syncServerHostService = syncServerHostService;
        this.vCenterInfoService = vCenterInfoService;
    }

    // Empty controller to avoid compiler warnings in huawei-vcenter-plugin-ui's
    // bundle-context.xml
    // where the bean is declared
    public NotificationController() {
        notificationAlarmService = null;
    }

    /**
     * 告警回调
     *
     * @param openid
     * @param body
     * @return
     */
    @RequestMapping(value = "")
    @ResponseBody
    public ResponseBodyBean callback(HttpServletRequest request,
                                     @RequestHeader String openid,
                                     @RequestBody String body) throws SQLException {
        // 比较有变化的健康状态并推送到HA
        notificationAlarmService.handleAlarm(request.getParameter("data"), openid);
        return success();
    }

    // 卸载插件
    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBodyBean unsubscribeAll(HttpServletRequest request,
                                           @RequestParam String vcenterUsername,
                                           @RequestParam String vcenterPassword,
                                           @RequestParam(required = false) String action) throws SQLException {
        LOGGER.info("unsubscribeAll action: " + action);
        synchronized (globalSession) {
            VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
            Boolean uninstallProviderResult = null;
            if (vCenterInfo == null) {
                LOGGER.info("No vcenterHA info or vcenterHA is not enabled. Do not unsubscribe.");
                //uninstallProviderResult = notificationAlarmService.uninstallProvider();
            } else {
                if (vcenterUsername.equals(vCenterInfo.getUserName())
                        && vcenterPassword.equals(CipherUtils.aesDncode(vCenterInfo.getPassword()))) {
                    uninstallProviderResult = notificationAlarmService.uninstallProvider();
                    if (uninstallProviderResult == null || uninstallProviderResult) {
                        notificationAlarmService.unsubscribeAll();
                        //vCenterInfoService.disableVCenterInfo();
                    }
                } else {
                    LOGGER.info("vcenter username and passsword do not match. Do not unsubscribe.");
                    return failure("Please check username and password.");
                }
            }
            if (uninstallProviderResult == null) {
                if ("uninstall".equals(action)) {
                    vCenterInfoService.deleteHAData();
                }
                return failure("-1", "No HA provider can be removed."); // no provider
            } else if(uninstallProviderResult) {
                if ("uninstall".equals(action)) {
                    vCenterInfoService.deleteHAData();
                }
                return success();
            } else {
                return failure("Can't remove provider, please make sure HA provider is unchecked before uninstall.");
            }
        }
    }

    @RequestMapping(value = "/unsubscribe/{esightIp}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBodyBean unsubscribe(HttpServletRequest request, @PathVariable String esightIp) {
        return getResultByData(notificationAlarmService.unsubscribeAlarm(esightIp, request.getSession(), "unsubscribe by client, esight: " + esightIp));
    }

    @RequestMapping(value = "/subscribe/{esightIp}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBodyBean subscribe(HttpServletRequest request, @PathVariable String esightIp) {
        return getResultByData(notificationAlarmService.subscribeAlarm(esightIp, request.getSession(), "subscribe by client, esight: " + esightIp));
    }

    @RequestMapping(value = "/systemKeepAlive", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBodyBean systemKeepAliveCallback(HttpServletRequest request, @RequestHeader String openid) {
        systemKeepAliveService.handleMessage(openid);
        return success();
    }
}
