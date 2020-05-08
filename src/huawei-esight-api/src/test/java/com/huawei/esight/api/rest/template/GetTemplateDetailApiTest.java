package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.EsightHelper;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hyuan on 2019/4/12.
 */
public class GetTemplateDetailApiTest {
    @Test
    public void testCall() {
        Map dataMap = new GetTemplateDetailApi<Map>(EsightHelper.getEsight()).doCall("899999", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
