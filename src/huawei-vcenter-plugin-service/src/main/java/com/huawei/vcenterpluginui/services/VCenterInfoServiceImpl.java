package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.dao.ESightDao;
import com.huawei.vcenterpluginui.dao.ESightHAServerDao;
import com.huawei.vcenterpluginui.dao.VCenterInfoDao;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.Pair;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.exception.VersionNotSupportException;
import com.huawei.vcenterpluginui.utils.AlarmDefinitionConverter;
import com.huawei.vcenterpluginui.utils.CipherUtils;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import com.vmware.connection.BasicConnection;
import com.vmware.connection.ConnectionException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

public class VCenterInfoServiceImpl extends ESightOpenApiService implements VCenterInfoService {

  @Autowired
  private VCenterInfoDao vCenterInfoDao;

  @Autowired
  private SyncServerHostService syncServerHostService;

  @Autowired
  private NotificationAlarmService notificationAlarmService;

  @Autowired
  private ESightDao eSightDao;

  @Autowired
  private ESightHAServerDao eSightHAServerDao;

  @Autowired
  private VCenterHAService vCenterHAService;

  @Override
  public int addVCenterInfo(VCenterInfo vCenterInfo) throws SQLException {
    return vCenterInfoDao.addVCenterInfo(vCenterInfo);
  }

  private void encode(VCenterInfo vCenterInfo) {
    vCenterInfo.setPassword(CipherUtils.aesEncode(vCenterInfo.getPassword()));
  }

  @Override
  public int saveVCenterInfo(final VCenterInfo vCenterInfo, final HttpSession session)
      throws SQLException {
    VCenterInfo vCenterInfo1 = vCenterInfoDao.getVCenterInfo();
    int returnValue = 0;
    boolean supportHA = true;
    ConnectedVim connectedVim = vCenterHAService.getConnectedVim();
    try {
      if (vCenterInfo1 != null) {
        // update
        vCenterInfo1.setUserName(vCenterInfo.getUserName());
        vCenterInfo1.setState(vCenterInfo.isState());
        vCenterInfo1.setPushEvent(vCenterInfo.isPushEvent());
        vCenterInfo1.setPushEventLevel(vCenterInfo.getPushEventLevel());
        vCenterInfo1.setHostIp(vCenterInfo.getHostIp());
        if (vCenterInfo.getPassword() != null && !"".equals(vCenterInfo.getPassword())) {
          vCenterInfo1.setPassword(vCenterInfo.getPassword());
          encode(vCenterInfo1);
        }
        connectedVim.connect(vCenterInfo1);

        if (vCenterInfo1.isState()) {
          try {
            vCenterHAService.createProvider(connectedVim, false);
          } catch (VersionNotSupportException e) {
            LOGGER.info("Not supported HA");
            vCenterInfo1.setState(false);
            vCenterInfo.setState(false);
            supportHA = false;
          }
        }
        returnValue = vCenterInfoDao.updateVCenterInfo(vCenterInfo1);
      } else {
        // insert
        encode(vCenterInfo);
        connectedVim.connect(vCenterInfo);

        if (vCenterInfo.isState()) {
          try {
            vCenterHAService.createProvider(connectedVim, false);
          } catch (VersionNotSupportException e) {
            LOGGER.info("Not supported HA");
            vCenterInfo.setState(false);
            supportHA = false;
          }
        }
        returnValue = addVCenterInfo(vCenterInfo);
      }
    } finally {
      connectedVim.disconnect();
    }

    if (vCenterInfo.isState() || vCenterInfo.isPushEvent()) {
      syncServerHostService.syncServerHost(true);
    } else {
      syncServerHostService.syncServerHost();
      try {
        List<ESight> eSightList = eSightDao.getAllESights();
        for (ESight eSight : eSightList) {
          try {
            LOGGER.info("unsubscribe by client: " + eSight);
            if ("1".equals(eSight.getReservedInt1())) {
              notificationAlarmService.unsubscribeAlarm(eSight, session, "unsubscribe_by_client");
            }
            eSightHAServerDao.deleteAll(eSight.getId());
          } catch (Exception e) {
            LOGGER.error("Failed to unsubscribe alarm: " + eSight, e);
          }
        }
      } catch (SQLException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

    syncAlarmDefinitions();

    return !supportHA ? Integer.valueOf(VersionNotSupportException.getReturnCode()) : returnValue;
  }

  @Override
  public void syncAlarmDefinitions() {
    try {
      VCenterInfo vCenterInfo = getVCenterInfo();
      if (vCenterInfo == null || !vCenterInfo.isPushEvent()) {
        LOGGER.info("Alarm is disabled. Do not sync alarm definitions");
        return;
      }
      Pair<List<AlarmDefinition>, List<AlarmDefinition>> pair = vCenterInfoDao
          .getAlarmDefinitionDiff(new AlarmDefinitionConverter().parseAlarmDefinitionList());
      List<AlarmDefinition> staleAlarmDefinitions = pair.getKey();
      List<AlarmDefinition> newAlarmDefinitions = pair.getValue();
      LOGGER.info("AlarmDefinitions to be removed size: " + staleAlarmDefinitions.size());
      LOGGER.info("AlarmDefinitions to be created size: " + newAlarmDefinitions.size());

      // remove stale alarm definitions from vCenter and DB
      if (staleAlarmDefinitions != null && !staleAlarmDefinitions.isEmpty()) {
        List<String> morValues = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (AlarmDefinition staleAlarmDefinition : staleAlarmDefinitions) {
          if (staleAlarmDefinition.getMorValue() != null && !""
              .equalsIgnoreCase(staleAlarmDefinition.getMorValue().trim())) {
            morValues.add(staleAlarmDefinition.getMorValue());
          }
          ids.add(staleAlarmDefinition.getId());
        }
        if (!morValues.isEmpty()) {
          vCenterHAService.unregisterAlarmDef(vCenterInfo, morValues);
        }
        LOGGER.info("Removed alarmDefinitions from vCenter: " + morValues);
        vCenterInfoDao.deleteAlarmDefinitions(ids);
        LOGGER.info("Removed alarmDefinitions from DB: " + ids);
      }

      // add new alarm definitions into vCenter and DB
      if (newAlarmDefinitions != null && !newAlarmDefinitions.isEmpty()) {
        vCenterHAService.registerAlarmDefInVcenterAndDB(vCenterInfo, newAlarmDefinitions);
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to sync alarm definitions", e);
    }
  }

  @Override
  public Map<String, Object> findVCenterInfo() throws SQLException {
    Map<String, Object> returnMap = new HashMap<>();
    VCenterInfo vCenterInfo = vCenterInfoDao.getVCenterInfo();
    if (vCenterInfo != null) {
      returnMap.put("USER_NAME", vCenterInfo.getUserName());
      returnMap.put("STATE", vCenterInfo.isState());
      returnMap.put("HOST_IP", vCenterInfo.getHostIp());
      returnMap.put("PUSH_EVENT", vCenterInfo.isPushEvent());
      returnMap.put("PUSH_EVENT_LEVEL", vCenterInfo.getPushEventLevel());
    }
    return returnMap;
  }

  @Override
  public VCenterInfo getVCenterInfo() throws SQLException {
    return vCenterInfoDao.getVCenterInfo();
  }

  @Override
  public boolean disableVCenterInfo() {
    try {
      return vCenterInfoDao.disableVCenterInfo();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public void deleteHAData() {
    try {
      vCenterInfoDao.deleteHAData();
    } catch (Exception e) {
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public void deleteHASyncAndDeviceData() {
    vCenterInfoDao.deleteHASyncAndDeviceData();
  }

  @Override
  public List<AlarmDefinition> getAlarmDefinitions() {
    try {
      return vCenterInfoDao.getAlarmDefinitions();
    } catch (SQLException e) {
      LOGGER.error("Failed to get alarm definitions", e);
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public void addAlarmDefinitions(List<AlarmDefinition> alarmDefinitions) {
    try {
      vCenterInfoDao.addAlarmDefinitionss(alarmDefinitions);
    } catch (SQLException e) {
      LOGGER.error("Failed to add alarm definitions", e);
    }
  }

  @Override
  public void deleteAlarmDefinitions() throws SQLException {
    vCenterInfoDao.deleteAlarmDefinitions();
  }

}
