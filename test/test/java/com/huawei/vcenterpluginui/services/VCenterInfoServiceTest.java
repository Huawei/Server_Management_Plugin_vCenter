package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

/**
 * Created by Rays on 2018/4/9.
 */
public class VCenterInfoServiceTest extends ContextSupported {

    @Autowired
    private VCenterInfoService vCenterInfoService;

    MockHttpSession mockHttpSession = new MockHttpSession();

    @Test
    public void saveVCenterInfo() throws SQLException {
        VCenterInfo vCenterInfo = new VCenterInfo();
        vCenterInfo.setHostIp("192.168.11.32");
        vCenterInfo.setUserName("administrator@huaweitest.com");
        vCenterInfo.setPassword("Huawei12#$");
        vCenterInfo.setState(true);
        vCenterInfoService.saveVCenterInfo(vCenterInfo, mockHttpSession);
        //Thread.sleep(20000);
    }

    @Test
    public void disableVCenterInfo() throws SQLException {
        vCenterInfoService.disableVCenterInfo();
        Assert.assertTrue(!vCenterInfoService.getVCenterInfo().isState());
    }

    @Test
    public void deleteHAData() {
        vCenterInfoService.deleteHAData();
    }
}