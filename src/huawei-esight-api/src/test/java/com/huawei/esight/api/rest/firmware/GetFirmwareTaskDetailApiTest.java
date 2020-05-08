package com.huawei.esight.api.rest.firmware;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetFirmwareTaskDetailApiTest {
    @Test
    public void testCall() {
        Map dataMap = new GetFirmwareTaskDetailApi<Map>(EsightHelper.getEsight()).doCall("rack", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
