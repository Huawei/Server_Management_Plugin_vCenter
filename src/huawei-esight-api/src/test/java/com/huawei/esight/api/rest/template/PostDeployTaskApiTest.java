package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.EsightHelper;
import com.huawei.esight.api.rest.server.GetServerDeviceApi;
import com.huawei.esight.api.rest.template.PostDeployTaskApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public class PostDeployTaskApiTest {
    @Test
    public void testCall() {
        Map dataMap = new PostDeployTaskApi<Map>(EsightHelper.getEsight()).doCall("rack", "1", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
