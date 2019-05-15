package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.DefaultOpenIdProvider;
import com.huawei.esight.utils.JsonUtil;
import com.huawei.vcenterpluginui.dao.ESightHAServerDao;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.NoEsightException;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.exception.VersionNotSupportException;
import com.huawei.vcenterpluginui.provider.SessionOpenIdProvider;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the EchoService interface
 */
public class ESightServiceImpl extends ESightOpenApiService implements ESightService {

  private NotificationAlarmService notificationAlarmService;

  @Autowired
  private VCenterInfoService vCenterInfoService;

  @Autowired
  private ESightHAServerDao eSightHAServerDao;

  @Autowired
  private VCenterHAService vCenterHAService;

  @Override
  public int saveESight(final ESight eSight, HttpSession session) throws SQLException {
    ESight existEsight = eSightDao.getESightByIp(eSight.getHostIp());
    int result;
    boolean isAdd = false;
    if (existEsight == null) {
      // 更新session中的openId
      new SessionOpenIdProvider(eSight, session).updateOpenId();
      // 加密esight密码
      ESight.updateEsightWithEncryptedPassword(eSight);
      result = eSightDao.saveESight(eSight);
      isAdd = true;
    } else {
      if (eSight.getLoginAccount() == null || eSight.getLoginAccount().isEmpty()) {
        // 未更新用户名密码
        eSight.setLoginAccount(existEsight.getLoginAccount());
        eSight.setLoginPwd(existEsight.getLoginPwd());
        ESight.updateEsightWithEncryptedPassword(eSight);
      } else {
        // 更新session中的openId
        new SessionOpenIdProvider(eSight, session).updateOpenId();
        // 更新了用户名密码，加密esight密码
        ESight.updateEsightWithEncryptedPassword(eSight);
      }
      eSight.setId(existEsight.getId());
      result = eSightDao.updateESight(eSight);
    }

    // update HA and alarm status
    final VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
    if (vCenterInfo != null && (vCenterInfo.isPushEvent() || vCenterInfo.isState())) {
      notificationAlarmService.getBgTaskExecutor().execute(new Runnable() {
        @Override
        public void run() {
          LOGGER.info("Updating HA and alarm status for eSight: " + eSight.getHostIp());
          if (vCenterInfo.isPushEvent()) {
            vCenterInfoService.syncAlarmDefinitions();
          }
          if (vCenterInfo.isState()) { // HA
            ConnectedVim connectedVim = vCenterHAService.getConnectedVim();
            connectedVim.connect(vCenterInfo);
            try {
              vCenterHAService.createProvider(connectedVim, false);
              updateHAProvider(1);
            } catch (VersionNotSupportException e) {
              LOGGER.info("Not supported HA");
            } catch (Exception e) {
              LOGGER.error("Cannot create provider" + e.getMessage());
              updateHAProvider(2);
            } finally {
              connectedVim.disconnect();
            }
          }
        }
      });
      notificationAlarmService.syncHistoricalEvents(getESightByIp(eSight.getHostIp()), isAdd,
          vCenterInfo.isPushEvent() || vCenterInfo.isState());
    }
    return result;
  }

  @Override
  public List<ESight> getESightList(String ip, int pageNo, int pageSize) throws SQLException {
    List<ESight> eSightList = eSightDao.getESightList(ip, pageNo, pageSize);

    if (eSightList.isEmpty()) {
      throw new NoEsightException();
    }

    return eSightList;
  }

  @Override
  public List<ESight> getESightListWithPassword(String ip, int pageNo, int pageSize)
      throws SQLException {
    List<ESight> eSightList = eSightDao.getESightListWithPwd(ip, pageNo, pageSize);

    if (eSightList.isEmpty()) {
      throw new NoEsightException();
    }

    return eSightList;
  }

  @Override
  public Map connect(ESight eSight) {
    return new DefaultOpenIdProvider(eSight).call(null, null, Map.class);
  }

  @Override
  public boolean updateHAStatus(String ip, String status) {
    try {
      return eSightDao.updateHAStatus(ip, status);
    } catch (SQLException e) {
      LOGGER.error("Failed to update HA status" + e.getMessage());
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public boolean updateSystemKeepAliveStatus(String ip, String status) {
    try {
      return eSightDao.updateSystemKeepAliveStatus(ip, status);
    } catch (SQLException e) {
      LOGGER.error("Failed to update system keep alive status: " + e.getMessage());
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public boolean updateHAProvider(int status) {
    try {
      return eSightDao.updateHAProvider(status);
    } catch (SQLException e) {
      LOGGER.error("Failed to update HA provider status: " + e.getMessage());
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public boolean updateAlarmDefinition(int status) {
    try {
      return eSightDao.updateAlarmDefinition(status);
    } catch (SQLException e) {
      LOGGER.error("Failed to update HA provider status: " + e.getMessage());
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public int deleteESights(String ids, HttpSession session) throws SQLException, IOException {
    Map<String, Object> idMap = JsonUtil.readAsMap(ids);
    List<Integer> id = (List<Integer>) idMap.get("ids");

    // 待取消订阅报警列表
    List<ESight> eSightList = new ArrayList<>();
    VCenterInfo vCenterInfo = null;
    vCenterInfo = vCenterInfoService.getVCenterInfo();
    if (vCenterInfo != null && (vCenterInfo.isState() || vCenterInfo.isPushEvent())) {
      for (Integer esightId : id) {
        eSightList.add(eSightDao.getESightById(esightId));
      }
    }

    int result = eSightDao.deleteESight(id);

    // 删除成功，取消订阅报警
    if (result > 0) {
      for (ESight eSight : eSightList) {
        try {
          LOGGER.info("unsubscribe by deleting esight: " + eSight);
          notificationAlarmService.unsubscribeAlarm(eSight, session, "vcenter_del_esight");
          eSightHAServerDao.deleteAll(eSight.getId());
        } catch (Exception e) {
          LOGGER.error("Failed to unsubscribe alarm: " + eSight + ": " + e.getMessage());
        }
      }
    }
    return result;
  }

  @Override
  public int getESightListCount(String ip) throws SQLException {
    return eSightDao.getESightListCount(ip);
  }

  public void setNotificationAlarmService(NotificationAlarmService notificationAlarmService) {
    this.notificationAlarmService = notificationAlarmService;
  }

}
