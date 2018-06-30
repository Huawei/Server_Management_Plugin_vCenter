package com.huawei.vcenterpluginui.services;

import com.google.gson.Gson;
import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.dao.VCenterInfoDao;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rays on 2018/4/9.
 */
public class VCenterHAServiceTest extends ContextSupported {

    @Autowired
    private VCenterHAService vCenterHAService;

    @Autowired
    private VCenterInfoDao vCenterInfoDao;

    @Test
    public void getServiceList() throws Exception {
        // 获取vCenter配置信息
        VCenterInfo vCenterInfo = getvCenterInfo();
        if (vCenterInfo == null) return;

        List<ESightHAServer> serviceList = vCenterHAService.getServerList(vCenterInfo);
        System.out.println(new Gson().toJson(serviceList));
    }

    private VCenterInfo getvCenterInfo() throws SQLException {
        VCenterInfo vCenterInfo = vCenterInfoDao.getVCenterInfo();
        if (vCenterInfo == null) {
            LOGGER.info("vCenter info not exist.");
            return null;
        } else if (!vCenterInfo.isState()) {
            LOGGER.info("vCenter info is disabled.");
            return null;
        }
        return vCenterInfo;
    }

    @Test
    public void pushHealth() {
        ESightHAServer eSightHAServer = new ESightHAServer();
        eSightHAServer.setHaHostSystem("host-29");

        ArrayList<ServerDeviceDetail> serverDeviceDetails = new ArrayList<>();
        serverDeviceDetails.add(new ServerDeviceDetail(1, "", "PSU", "", "-10", ""));

        vCenterHAService.pushHealth(eSightHAServer, serverDeviceDetails);
    }

    @Test
    public void removeProvider() throws SQLException {
        VCenterInfo vCenterInfo = getvCenterInfo();
        if (vCenterInfo == null) return;
        Boolean removeProvider = vCenterHAService.removeProvider(vCenterInfo);
        System.out.println(removeProvider);
    }

    @Test
    public void createProvider() throws Exception {
        VCenterInfo vCenterInfo = getvCenterInfo();
        if (vCenterInfo == null) return;
        String providerId = vCenterHAService.createProvider(vCenterInfo, true);
        System.out.println(providerId);
    }
}