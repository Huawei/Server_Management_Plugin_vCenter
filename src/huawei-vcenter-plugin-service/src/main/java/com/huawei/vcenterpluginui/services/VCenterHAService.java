package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import java.util.List;

/**
 * Created by Rays on 2018/4/9.
 */
public interface VCenterHAService {

  List<ESightHAServer> getServerList(VCenterInfo vCenterInfo) throws Exception;

  void removeMonitored(VCenterInfo vCenterInfo, List<ESightHAServer> list);

  @Deprecated
  boolean pushHealth(ESightHAServer eSightHAServer, List<ServerDeviceDetail> serverDeviceDetails);

  boolean pushHealth(List<ESightHAServer> eSightHAServers,
      List<ServerDeviceDetail> serverDeviceDetails);

  /**
   * create provider if it doesn't exist
   *
   * @param enable Whether to enable
   * @return providerId
   */
  String createProvider(ConnectedVim connectedVim, boolean enable);

  /**
   * remove provider if it exist
   *
   * @param vCenterInfo vCenter account info
   * @return null: provider not exist<br/> true: remove success<br/> false: remove fail or other
   * exception
   */
  Boolean removeProvider(VCenterInfo vCenterInfo);

  /**
   * create alarm definition in vcenter and DB in a new lowest priority thread
   */
  void registerAlarmDefInVcenterAndDB(VCenterInfo vCenterInfo,
      List<AlarmDefinition> alarmDefinitionList, boolean result);

  ConnectedVim getConnectedVim();

  /**
   * unregister alarm definitions from vCenter
   */
  int unregisterAlarmDef(VCenterInfo vCenterInfo, List<String> morList);

}
