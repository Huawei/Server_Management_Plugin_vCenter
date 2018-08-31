package com.huawei.vcenterpluginui.mvc;

import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.services.NotificationAlarmService;
import com.huawei.vcenterpluginui.services.SystemKeepAliveService;
import com.huawei.vcenterpluginui.services.VCenterHAService;
import com.huawei.vcenterpluginui.services.VCenterInfoService;
import com.huawei.vcenterpluginui.utils.CipherUtils;
import com.huawei.vcenterpluginui.utils.OpenIdSessionManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 告警回调 控制层
 *
 * @author Horace
 */
@RequestMapping(value = "/services/notification")
public class NotificationController extends BaseController {

  @Autowired
  private NotificationAlarmService notificationAlarmService;

  @Autowired
  private VCenterInfoService vCenterInfoService;

  @Autowired
  private SystemKeepAliveService systemKeepAliveService;

  @Autowired
  private VCenterHAService vCenterHAService;

  private HttpSession globalSession = OpenIdSessionManager.getGlobalSession();

  /**
   * 告警回调
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
      // unregister alarm definitions
      try {
        if ("uninstall".equals(action) && vCenterInfo != null) {
          List<AlarmDefinition> alarmDefinitions = vCenterInfoService
              .getAlarmDefinitions();
          if (!alarmDefinitions.isEmpty()) {
            List<String> morValList = new ArrayList<>();
            for (AlarmDefinition alarmDefinition : alarmDefinitions) {
              if (alarmDefinition.getMorValue() != null && !""
                  .equals(alarmDefinition.getMorValue().trim())) {
                morValList.add(alarmDefinition.getMorValue());
              }
            }
            LOGGER.info("Unregistering " + morValList.size() + " alarm definitions.");
            if (!morValList.isEmpty()) {
              vCenterHAService.unregisterAlarmDef(vCenterInfo, morValList);
              LOGGER.info("Removed alarm definition from vCenter");
            }
          }
          vCenterInfoService.deleteAlarmDefinitions();
        }
      } catch (Exception e) {
        LOGGER.error("Cannot remove alarm definitions", e);
      }

      Boolean uninstallProviderResult = null;
      if (vCenterInfo == null) {
        LOGGER.info("No vcenterHA info or vcenterHA is not enabled. Do not unsubscribe.");
      } else {
        if (vcenterUsername.equals(vCenterInfo.getUserName())
            && vcenterPassword.equals(CipherUtils.aesDncode(vCenterInfo.getPassword()))) {
          uninstallProviderResult = notificationAlarmService.uninstallProvider();
          if (uninstallProviderResult == null || uninstallProviderResult) {
            // 20180803: only uninstall plugin needs to unsubscribe
            if ("uninstall".equals(action)) {
              notificationAlarmService.unsubscribeAll();
            }
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
      } else if (uninstallProviderResult) {
        if ("uninstall".equals(action)) {
          vCenterInfoService.deleteHAData();
        }
        return success();
      } else {
        return failure(
            "Can't remove provider, please make sure HA provider is unchecked before uninstall.");
      }
    }
  }

  @RequestMapping(value = "/unsubscribe/{esightIp}", method = RequestMethod.POST)
  @ResponseBody
  public ResponseBodyBean unsubscribe(HttpServletRequest request, @PathVariable String esightIp) {
    return getResultByData(notificationAlarmService.unsubscribeAlarm(esightIp, request.getSession(),
        "unsubscribe by client, esight: " + esightIp));
  }

  @RequestMapping(value = "/subscribe/{esightIp}", method = RequestMethod.POST)
  @ResponseBody
  public ResponseBodyBean subscribe(HttpServletRequest request, @PathVariable String esightIp) {
    return getResultByData(notificationAlarmService.subscribeAlarm(esightIp, request.getSession(),
        "subscribe by client, esight: " + esightIp));
  }

  @RequestMapping(value = "/systemKeepAlive", method = RequestMethod.POST)
  @ResponseBody
  public ResponseBodyBean systemKeepAliveCallback(HttpServletRequest request,
      @RequestHeader String openid) {
    systemKeepAliveService.handleMessage(openid);
    return success();
  }
}
