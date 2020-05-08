package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.EsightHelper;
import com.huawei.esight.api.rest.template.PostDeployTaskApi;
import com.huawei.esight.api.rest.template.PostDeployTaskDetailApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public class PostDeployTaskDetailApiTest {
    @Test
    public void testCall() {
        Map dataMap = new PostDeployTaskDetailApi<Map>(EsightHelper.getEsight()).doCall("rack", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
