package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Rays on 2018/4/9.
 */
public class VCenterHAServiceImpl implements VCenterHAService {

  public final static Logger LOGGER = LoggerFactory.getLogger(VCenterHAService.class);

  private VCenterInfoService vCenterInfoService;

  private String providerNamePrefix;

  private String providerNameVersion;

  @Autowired
  private ESightService eSightService;

  @Autowired
  private NotificationAlarmService notificationAlarmService;

  @Override
  public List<ESightHAServer> getServerList(VCenterInfo vCenterInfo) throws Exception {
    ConnectedVim connectedVim = getConnectedVim();
    return connectedVim.getServerList(vCenterInfo);
  }

  @Override
  public void removeMonitored(VCenterInfo vCenterInfo, List<ESightHAServer> list) {
    List<ESightHAServer> removeMonitoredServerList = new LinkedList<>();
    for (ESightHAServer eSightHAServer : list) {
      if (eSightHAServer.getStatus() != ESightHAServer.STATUS_ALREADY_SYNC
          && eSightHAServer.getHaHostSystem() != null) {
        removeMonitoredServerList.add(eSightHAServer);
      }
    }
    LOGGER.info("HA remove monitored server list size: " + removeMonitoredServerList.size());
    if (removeMonitoredServerList.isEmpty()) {
      return;
    }

    ConnectedVim connectedVim = getConnectedVim();
    try {
      connectedVim.removeMonitored(vCenterInfo, removeMonitoredServerList);
    } catch (Exception e) {
      LOGGER.warn("HA remove monitored exception", e);
    }
  }

  @Override
  public String createProvider(ConnectedVim connectedVim, boolean enable) {
    return connectedVim.createProvider(enable);
  }

  @Override
  public Boolean removeProvider(VCenterInfo vCenterInfo) {
    return getConnectedVim().removeProvider(vCenterInfo);
  }

  @Override
  public void registerAlarmDefInVcenterAndDB(final VCenterInfo vCenterInfo,
      final List<AlarmDefinition> alarmDefinitionList, final boolean result) {
    final ConnectedVim connectedVim = this.getConnectedVim();
    try {
      connectedVim.connect(vCenterInfo);
      List<AlarmDefinition> newAlarmDefinitionList = connectedVim
          .createAlarmDefinitions(alarmDefinitionList);
      vCenterInfoService.addAlarmDefinitions(newAlarmDefinitionList);
      if (result && newAlarmDefinitionList.size() == alarmDefinitionList.size()) {
        eSightService.updateAlarmDefinition(1);
      } else {
        eSightService.updateAlarmDefinition(2);
      }
    } catch (Exception e) {
      LOGGER.warn("can not register alarm definitions", e);
      throw new VcenterException("-90007", e.getMessage());
    } finally {
      connectedVim.disconnect();
    }
  }

  public ConnectedVim getConnectedVim() {
    return new ConnectedVim(providerNamePrefix, providerNameVersion);
  }

  @Override
  public int unregisterAlarmDef(VCenterInfo vCenterInfo, List<String> morList) {
    int result = getConnectedVim().removeAlarmDefinitions(vCenterInfo, morList);
    LOGGER.info("Removed alarm definition: " + result);
    return result;
  }

  public void setvCenterInfoService(VCenterInfoService vCenterInfoService) {
    this.vCenterInfoService = vCenterInfoService;
  }

  public void setProviderNamePrefix(String providerNamePrefix) {
    this.providerNamePrefix = providerNamePrefix;
  }

  public void setProviderNameVersion(String providerNameVersion) {
    this.providerNameVersion = providerNameVersion;
  }
}
