package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.constant.DeviceComponent;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

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
    public boolean pushHealth(ESightHAServer eSightHAServer, List<ServerDeviceDetail> serverDeviceDetails) {
        return pushHealth(Collections.singletonList(eSightHAServer), serverDeviceDetails);
    }

    @Override
    public boolean pushHealth(List<ESightHAServer> eSightHAServers, List<ServerDeviceDetail> serverDeviceDetails) {
        try {
            // 20180523: don't push health when status is -1 or -2
            List<ServerDeviceDetail> newServerDeviceDetails = new ArrayList<>();
            for (ServerDeviceDetail serverDeviceDetail : serverDeviceDetails) {
                if(DeviceComponent.getPushHealthState().contains(serverDeviceDetail.getHealthState())) {
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
            } else if (!vCenterInfo.isState()) {
                LOGGER.info("vCenter info is disabled.");
                return false;
            }

            ConnectedVim connectedVim = getConnectedVim();
            connectedVim.pushHealth(vCenterInfo, eSightHAServers, newServerDeviceDetails);
            return true;
        } catch (VcenterException | SQLException | RuntimeFaultFaultMsg | IOException | NotFoundFaultMsg e) {
            LOGGER.error("push health fail", e);
        }
        return false;
    }

    @Override
    public String createProvider(VCenterInfo vCenterInfo, boolean enable) {
        ConnectedVim connectedVim = getConnectedVim();
        return connectedVim.createProvider(vCenterInfo, enable);
    }

    @Override
    public Boolean removeProvider(VCenterInfo vCenterInfo) {
        return getConnectedVim().removeProvider(vCenterInfo);
    }

    private ConnectedVim getConnectedVim() {
        return new ConnectedVim(providerNamePrefix, providerNameVersion);
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
