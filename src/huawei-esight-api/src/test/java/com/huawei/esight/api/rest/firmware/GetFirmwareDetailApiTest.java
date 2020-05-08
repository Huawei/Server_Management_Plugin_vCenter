package com.huawei.esight.api.rest.firmware;

import com.huawei.esight.api.EsightHelper;
import com.huawei.esight.api.rest.server.GetServerDeviceApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetFirmwareDetailApiTest {
    @Test
    public void testCall() {
        Map dataMap = new GetFirmwareDetailApi<Map>(EsightHelper.getEsight()).doCall("rack", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
