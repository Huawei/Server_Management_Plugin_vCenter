package com.huawei.esight.api.rest.firmware;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public class PostFirmwareUploadApiTest {
    @Test
    public void testCall() {
        Map dataMap = new PostFirmwareUploadApi<Map>(EsightHelper.getEsight()).doCall(Collections.<String, String>emptyMap(), Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
