package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.constant.DeviceComponent;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Rays on 2018/4/9.
 */
public class VCenterHAServiceImpl implements VCenterHAService {

  public final static Log LOGGER = LogFactory.getLog(VCenterHAService.class);

  private VCenterInfoService vCenterInfoService;

  private String providerNamePrefix;

  private String providerNameVersion;

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
  public boolean pushHealth(ESightHAServer eSightHAServer,
      List<ServerDeviceDetail> serverDeviceDetails) {
    return pushHealth(Collections.singletonList(eSightHAServer), serverDeviceDetails);
  }

  @Override
  public boolean pushHealth(List<ESightHAServer> eSightHAServers,
      List<ServerDeviceDetail> serverDeviceDetails) {
    try {
      // 20180523: don't push health when status is -1 or -2
      List<ServerDeviceDetail> newServerDeviceDetails = new ArrayList<>();
      for (ServerDeviceDetail serverDeviceDetail : serverDeviceDetails) {
        if (DeviceComponent.getPushHealthState().contains(serverDeviceDetail.getHealthState())) {
          newServerDeviceDetails.add(serverDeviceDetail);
        }
      }
      if (newServerDeviceDetails.isEmpty()) {
        LOGGER.info("All components healthState are invalid, Discard.");
        return true;
      }

      VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
      if (vCenterInfo == null) {
        LOGGER.info("vCenter info not exist.");
        return false;
      } else if (!vCenterInfo.isState() && !vCenterInfo.isPushEvent()) {
        LOGGER.info("vCenter info is disabled.");
        return false;
      }

      ConnectedVim connectedVim = getConnectedVim();
      connectedVim.pushHealth(vCenterInfo, eSightHAServers, newServerDeviceDetails);
      return true;
    } catch (Exception e) {
      LOGGER.error("push health fail", e);
    }
    return false;
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
      final List<AlarmDefinition> alarmDefinitionList) {
    final ConnectedVim connectedVim = this.getConnectedVim();
    try {
      Thread t1 = new Thread(new Runnable() {
        @Override
        public void run() {
          synchronized (VCenterHAServiceImpl.class) {
            try {
              connectedVim.connect(vCenterInfo);
              List<AlarmDefinition> newAlarmDefinitionList = connectedVim
                  .createAlarmDefinitions(vCenterInfo, alarmDefinitionList);
              vCenterInfoService.addAlarmDefinitions(newAlarmDefinitionList);
            } catch (Exception e) {
              LOGGER.error("Failed to create alarm definition", e);
            } finally {
              connectedVim.disconnect();
            }
          }
        }
      });
      t1.setPriority(1);
      t1.start();
    } catch (Exception e) {
      LOGGER.warn("can not connect to vCenter, ", e);
      throw new VcenterException("-90007", e.getMessage());
    }
  }

  public ConnectedVim getConnectedVim() {
    return new ConnectedVim(providerNamePrefix, providerNameVersion);
  }

  @Override
  public void unregisterAlarmDef(VCenterInfo vCenterInfo, List<String> morList) {
    int result = getConnectedVim().removeAlarmDefinitions(vCenterInfo, morList);
    LOGGER.info("Removed alarm definition: " + result);
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
