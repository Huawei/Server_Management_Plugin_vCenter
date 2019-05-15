package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.dao.ESightDao;
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
import com.huawei.vcenterpluginui.utils.ThumbprintsUtils;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

public class VCenterInfoServiceImpl extends ESightOpenApiService implements VCenterInfoService {

  @Autowired
  private VCenterInfoDao vCenterInfoDao;

  @Autowired
  private NotificationAlarmService notificationAlarmService;

  @Autowired
  private ESightDao eSightDao;

  @Autowired
  private ESightService eSightService;

  @Autowired
  private VCenterHAService vCenterHAService;

  @Autowired
  private SyncServerHostService syncServerHostService;

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
    boolean isAlarmNewEnabled = false;
    boolean isHANewEnabled = false;

    try {
      if (vCenterInfo1 != null) {
        isHANewEnabled = (vCenterInfo.isState() && !vCenterInfo1.isState());
        isAlarmNewEnabled = (vCenterInfo.isPushEvent() && !vCenterInfo1.isPushEvent());
        // update
        vCenterInfo1.setUserName(vCenterInfo.getUserName());
        vCenterInfo1.setState(vCenterInfo.isState());
        vCenterInfo1.setPushEvent(vCenterInfo.isPushEvent());
        vCenterInfo1.setPushEventLevel(vCenterInfo.getPushEventLevel());
        vCenterInfo1.setHostIp(vCenterInfo.getHostIp());
        vCenterInfo1.setHostPort(vCenterInfo.getHostPort());
        if (vCenterInfo.getPassword() != null && !"".equals(vCenterInfo.getPassword())) {
          vCenterInfo1.setPassword(vCenterInfo.getPassword());
          encode(vCenterInfo1);
        }
        connectedVim.connect(vCenterInfo1);

        if (vCenterInfo1.isState()) {
          try {
            vCenterHAService.createProvider(connectedVim, false);
            eSightService.updateHAProvider(1);
          } catch (VersionNotSupportException e) {
            LOGGER.info("Not supported HA");
            vCenterInfo1.setState(false);
            vCenterInfo.setState(false);
            supportHA = false;
          } catch (Exception e) {
            LOGGER.error("Cannot create provider: " + e.getMessage());
            vCenterInfo1.setState(false);
            vCenterInfo.setState(false);
            eSightService.updateHAProvider(2);
          }
        }
        returnValue = vCenterInfoDao.updateVCenterInfo(vCenterInfo1);
      } else {
        // insert
        encode(vCenterInfo);
        connectedVim.connect(vCenterInfo);

        isHANewEnabled = vCenterInfo.isState();
        isAlarmNewEnabled = vCenterInfo.isPushEvent();

        if (vCenterInfo.isState()) {
          try {
            vCenterHAService.createProvider(connectedVim, false);
            eSightService.updateHAProvider(1);
          } catch (VersionNotSupportException e) {
            LOGGER.info("Not supported HA");
            vCenterInfo.setState(false);
            supportHA = false;
          } catch (Exception e) {
            LOGGER.error("Cannot create provider: " + e.getMessage());
            vCenterInfo.setState(false);
            eSightService.updateHAProvider(2);
          }
        }
        returnValue = addVCenterInfo(vCenterInfo);
      }
    } finally {
      connectedVim.disconnect();
    }

    syncServerHostService.syncServerHost(false); // subscribe alarm below
    syncAlarmDefinitions();

    if (isAlarmNewEnabled || isHANewEnabled) {
      notificationAlarmService
          .syncHistoricalEvents(false, false);
    }

    try {
      List<ESight> eSightList = eSightDao.getAllESights();
      boolean subscribeAlarm = (vCenterInfo.isPushEvent() || vCenterInfo.isState());
      for (ESight eSight : eSightList) {
        try {
          if (!subscribeAlarm && "1".equals(eSight.getReservedInt1())) {
            LOGGER.info("Unsubscribe by client: " + eSight);
            notificationAlarmService.unsubscribeAlarm(eSight, session, "unsubscribe_by_client");
          } else if (subscribeAlarm && !"1".equals(eSight.getReservedInt1())) {
            LOGGER.info("Subscribe by client: " + eSight);
            notificationAlarmService.subscribeAlarm(eSight, session, "subscribe_by_client");
          }
          //eSightHAServerDao.deleteAll(eSight.getId());
        } catch (Exception e) {
          LOGGER.error("Failed to unsubscribe alarm: " + eSight + ": " + e.getMessage());
        }
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to save vCenter info: " + e.getMessage());
    }

    return !supportHA ? Integer.valueOf(VersionNotSupportException.getReturnCode()) : returnValue;
  }

  @Override
  public void syncAlarmDefinitions() {
    notificationAlarmService.getBgTaskExecutor().execute(new Runnable() {
      @Override
      public void run() {
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

          boolean success = true;

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
              int removed = vCenterHAService.unregisterAlarmDef(vCenterInfo, morValues);
              success = (removed == morValues.size());
            }
            LOGGER.info("Removed alarmDefinitions from vCenter: " + morValues);
            vCenterInfoDao.deleteAlarmDefinitions(ids);
            LOGGER.info("Removed alarmDefinitions from DB: " + ids);
          }

          // add new alarm definitions into vCenter and DB
          if (newAlarmDefinitions != null && !newAlarmDefinitions.isEmpty()) {
            vCenterHAService.registerAlarmDefInVcenterAndDB(vCenterInfo, newAlarmDefinitions, success);
          }
          eSightService.updateAlarmDefinition(success ? 1 : 2);
        } catch (Exception e) {
          LOGGER.error("Failed to sync alarm definitions: " + e.getMessage());
          eSightService.updateAlarmDefinition(2);
        }
      }
    });
  }

  @Override
  public Map<String, Object> findVCenterInfo() throws SQLException {
    Map<String, Object> returnMap = new HashMap<>();
    VCenterInfo vCenterInfo = vCenterInfoDao.getVCenterInfo();
    if (vCenterInfo != null) {
      returnMap.put("USER_NAME", vCenterInfo.getUserName());
      returnMap.put("STATE", vCenterInfo.isState());
      returnMap.put("HOST_IP", vCenterInfo.getHostIp());
      returnMap.put("HOST_PORT", vCenterInfo.getHostPort());
      returnMap.put("PUSH_EVENT", vCenterInfo.isPushEvent());
      returnMap.put("PUSH_EVENT_LEVEL", vCenterInfo.getPushEventLevel());
    }
    boolean supportSetting = eSightService.getESightListCount(null) > 0;
    returnMap.put("SUPPORT_SETTING", supportSetting);
    returnMap.put("SUPPORT_ALARM", supportSetting ? true : false);
    if (supportSetting) {
      try {
        ConnectedVim.checkVersionCompatible();
        returnMap.put("SUPPORT_HA", true);
      } catch (VersionNotSupportException e) {
        returnMap.put("SUPPORT_HA", false);
      } catch (Exception e) {
        LOGGER.warn("Cannot get version: " + e.getMessage());
        returnMap.put("SUPPORT_HA", true);
      }
    } else {
      returnMap.put("SUPPORT_HA", false);
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
      LOGGER.error("Failed to disable vCenter info");
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
      LOGGER.error("Failed to get alarm definitions");
      throw new VcenterException(e.getMessage());
    }
  }

  @Override
  public void addAlarmDefinitions(List<AlarmDefinition> alarmDefinitions) {
    try {
      vCenterInfoDao.addAlarmDefinitionss(alarmDefinitions);
    } catch (SQLException e) {
      LOGGER.error("Failed to add alarm definitions");
    }
  }

  @Override
  public void deleteAlarmDefinitions() throws SQLException {
    vCenterInfoDao.deleteAlarmDefinitions();
  }

  @Override
  public synchronized int saveJksThumbprints(InputStream inputStream, String password) {
    try {
      String[] thumbprints = ThumbprintsUtils.getThumbprintsFromJKS(inputStream, password);
      String[] tp = vCenterInfoDao.mergeSaveAndLoadAllThumbprints(thumbprints);
      LOGGER.info("Thumbprints have been saved, new list size: " + tp.length);
      ThumbprintsUtils.updateContextTrustThumbprints(tp);
      return RESULT_SUCCESS_CODE;
    } catch (IOException e) {
      LOGGER.warn("Cannot get thumbprints from JKS " + e.getMessage());
      return RESULT_READ_CERT_ERROR;
    } catch (Exception e) {
      LOGGER.error("Cannot get/save thumbprints from JKS " + e.getMessage());
      return FAIL_CODE;
    }
  }

  @Override
  public synchronized void saveThumbprints(String[] thumbprints) {
    try {
      String[] tp = vCenterInfoDao.mergeSaveAndLoadAllThumbprints(thumbprints);
      LOGGER.info("Thumbprints have been saved, new list size: " + tp.length);
      ThumbprintsUtils.updateContextTrustThumbprints(tp);
    } catch (Exception e) {
      LOGGER.warn("Cannot save thumbprints: " + e.getMessage());
    }
  }

  @Override
  public String[] getThumbprints() throws SQLException {
    String[] thumbprints = vCenterInfoDao.loadThumbprints();
    LOGGER.info("Thumbprints have been loaded, size: " + thumbprints.length);
    return thumbprints;
  }

}
