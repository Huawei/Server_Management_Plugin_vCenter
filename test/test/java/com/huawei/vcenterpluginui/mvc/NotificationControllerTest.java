package com.huawei.vcenterpluginui.mvc;

import com.huawei.esight.utils.HttpRequestUtil;
import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.sql.SQLException;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PrepareForTest({HttpRequestUtil.class})
@PowerMockIgnore({"sun.security.*", "javax.net.*", "javax.crypto.*"})
public class NotificationControllerTest extends ContextSupported {

    private MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();

    @Autowired
    private NotificationController notificationController;

    @Before
    public void setUp() {
        mockHttpServletRequest.setParameter("data", "[{\"optType\":2,\"systemID\":\"HuaweiPlatform\",\"ackTime\":0,\"ackUser\":null,\"acked\":false,\"additionalInformation\":\"TAG=网管应用告警\",\"additionalText\":\"\",\"alarmId\":80011,\"alarmName\":\"系统通信告警\",\"alarmSN\":8,\"arrivedTime\":1523512971039,\"clearUser\":\"System+User\",\"cleared\":true,\"clearedTime\":1523528820000,\"clearedType\":2,\"commentTime\":0,\"commentUser\":\"\",\"comments\":\"\",\"devCsn\":0,\"eventTime\":1523330753000,\"eventType\":1,\"moDN\":\"OS=1\",\"moName\":\"LocalNMS\",\"neDN\":\"OS=1\",\"neName\":\"LocalNMS\",\"neType\":\"OMS\",\"objectInstance\":\"用于测试SNMP或HTTP协议的第三方系统是否和网管连接正常。\",\"perceivedSeverity\":4,\"probableCause\":-1,\"proposedRepairActions\":\"\",\"alarmEvent\":\"Alarms\",\"objectClass\":\"OMS\",\"userData\":{\"IEMP_FM_ARSTEP\":\"ARPOST\",\"FSN\":\"@4@\",\"latestLogTime\":\"1523512971021\",\"counter\":\"4\",\"ip_address\":\"192.168.10.84\",\"QMode\":\"1\",\"latestLogSn\":\"313418\",\"FTD\":\"16269000\",\"devCsn\":\"0\"}}]");
    }

    @Test
    public void callback() throws IOException, SQLException {
        PowerMockito.mockStatic(HttpRequestUtil.class);
        ResponseBodyBean ret = notificationController.callback(mockHttpServletRequest, null, ALARM);
        LOGGER.info(ret);
        Assert.assertEquals("0", ret.getCode());
    }

    @Test
    public void unsubscribe() throws SQLException {
        ResponseBodyBean responseBodyBean = notificationController.unsubscribeAll(null, "test1", "test2", "install");
        Assert.assertEquals("0", responseBodyBean.getCode());
    }

    @Test
    public void unsubscribeOne() {
        ResponseBodyBean responseBodyBean = notificationController.unsubscribe(mockHttpServletRequest, "192.168.10.84");
        Assert.assertEquals("0", responseBodyBean.getCode());
    }

    @Test
    public void subscribeOne() {
        ResponseBodyBean responseBodyBean = notificationController.subscribe(mockHttpServletRequest, "192.168.10.84");
        Assert.assertEquals("0", responseBodyBean.getCode());
    }

    private static final String ALARM = "[\n" +
            "    {\n" +
            "        \"optType\": 1,\n" +
            "        \"systemID\": \"HuaweiPlatform\",\n" +
            "        \"ackTime\": 0,\n" +
            "        \"ackUser\": null,\n" +
            "        \"acked\": false,\n" +
            "        \"additionalInformation\": \"TAG=网管应用告警\",\n" +
            "        \"additionalText\": \"\",\n" +
            "        \"alarmId\": 80011,\n" +
            "        \"alarmName\": \"系统通信告警\",\n" +
            "        \"alarmSN\": 4,\n" +
            "        \"arrivedTime\": 1523330753763,\n" +
            "        \"clearUser\": null,\n" +
            "        \"cleared\": false,\n" +
            "        \"clearedTime\": 0,\n" +
            "        \"clearedType\": 2,\n" +
            "        \"commentTime\": 0,\n" +
            "        \"commentUser\": \"\",\n" +
            "        \"comments\": \"\",\n" +
            "        \"devCsn\": 0,\n" +
            "        \"eventTime\": 1523330753000,\n" +
            "        \"eventType\": 1,\n" +
            "        \"moDN\": \"OS=1\",\n" +
            "        \"moName\": \"LocalNMS\",\n" +
            "        \"neDN\": \"NE=34603309\",\n" +
            "        \"neName\": \"LocalNMS\",\n" +
            "        \"neType\": \"OMS\",\n" +
            "        \"objectInstance\": \"用于测试SNMP或HTTP协议的第三方系统是否和网管连接正常。\",\n" +
            "        \"perceivedSeverity\": 4,\n" +
            "        \"probableCause\": -1,\n" +
            "        \"proposedRepairActions\": \"\",\n" +
            "        \"alarmEvent\": \"Alarms\",\n" +
            "        \"objectClass\": \"OMS\",\n" +
            "        \"userData\": {\n" +
            "            \"IEMP_FM_ARSTEP\": \"ARPOST\",\n" +
            "            \"FSN\": \"@4@\",\n" +
            "            \"counter\": \"1\",\n" +
            "            \"ip_address\": \"192.168.10.84\",\n" +
            "            \"subnet\": \"/\",\n" +
            "            \"QMode\": \"1\",\n" +
            "            \"ADAC_eventTime\": \"1523330753000\",\n" +
            "            \"latestLogSn\": \"277341\",\n" +
            "            \"devCsn\": \"0\"\n" +
            "        }\n" +
            "    }\n" +
            "]";

}
