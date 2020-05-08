package com.huawei.esight.api.rest.server;

import com.huawei.esight.api.EsightHelper;
import com.huawei.esight.api.rest.server.GetServerDeviceApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetServerDeviceApiTest {
    @Test
    public void testCall() {
        Map dataMap = new GetServerDeviceApi<Map>(EsightHelper.getEsight()).doCall("rack", "1", "5", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
