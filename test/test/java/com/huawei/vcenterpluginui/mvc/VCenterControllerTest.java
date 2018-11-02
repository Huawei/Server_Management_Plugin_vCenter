package com.huawei.vcenterpluginui.mvc;

import com.huawei.esight.utils.HttpRequestUtil;
import com.huawei.esight.utils.JsonUtil;
import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.NoEsightException;
import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MultiValueMap;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PrepareForTest({HttpRequestUtil.class})
@PowerMockIgnore({"sun.security.*", "javax.net.*","javax.crypto.*"})
public class VCenterControllerTest extends ContextSupported {

    @Autowired
    private VCenterController servicesController;

    MockHttpSession mockHttpSession = new MockHttpSession();

    private static final String CODE_SUCCESS = "0";

    @Test
    public void saveVCenterInfo() throws IOException, SQLException {
        ResponseBodyBean responseBodyBean = servicesController
            .saveVCenterInfo(null, newVCenterInfo(), mockHttpSession);
        System.out.println(responseBodyBean.getCode());

        Assert.assertEquals(CODE_SUCCESS, responseBodyBean.getCode());
        Assert.assertEquals(null, responseBodyBean.getData());
        Assert.assertEquals(null, responseBodyBean.getDescription());

    }

    @Test
    public void getVCenterInfo() throws IOException, SQLException {
        servicesController.saveVCenterInfo(null, newVCenterInfo(), mockHttpSession);
        ResponseBodyBean responseBodyBean = servicesController.findVCenterInfo(null);
        Map<String,Object> map = JsonUtil.readAsMap(String.valueOf(responseBodyBean.getData()));
        Assert.assertEquals(CODE_SUCCESS, responseBodyBean.getCode());
        Assert.assertEquals(map.containsKey("USER_NAME"), true);
        Assert.assertEquals(map.containsKey("HOST_IP"), true);
        Assert.assertEquals(map.containsKey("STATE"), true);
        Assert.assertEquals(null, responseBodyBean.getDescription());
    }

    @Test
    public void findLocalIps() throws SocketException {
        ResponseBodyBean responseBodyBean = servicesController.findLocalIps(null);
        Assert.assertEquals(CODE_SUCCESS, responseBodyBean.getCode());
        Assert.assertTrue(responseBodyBean.getData() instanceof ArrayList);
        Assert.assertEquals(null, responseBodyBean.getDescription());
    }

    private VCenterInfo newVCenterInfo() {
        VCenterInfo vCenterInfo = new VCenterInfo();
        vCenterInfo.setUserName("openApiUser");
        vCenterInfo.setPassword("Simple.0");
        return vCenterInfo;
    }

}
