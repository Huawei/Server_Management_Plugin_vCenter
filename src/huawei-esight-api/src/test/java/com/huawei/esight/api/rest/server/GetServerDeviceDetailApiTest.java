package com.huawei.esight.api.rest.server;

import com.huawei.esight.api.EsightHelper;
import com.huawei.esight.api.rest.server.GetServerDeviceDetailApi;
import com.huawei.esight.bean.Esight;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetServerDeviceDetailApiTest {
    @Test
    public void testCall() {
        Map dataMap = new GetServerDeviceDetailApi<Map>(EsightHelper.getEsight()).doCall("NE=34603009", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
