package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/30.
 */
public class DeleteTemplateApiTest {
    @Test
    public void testCall() {
        Map dataMap = new DeleteTemplateApi<Map>(EsightHelper.getEsight()).doCall("test003", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
