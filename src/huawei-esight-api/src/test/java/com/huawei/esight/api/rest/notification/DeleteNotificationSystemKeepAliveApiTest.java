package com.huawei.esight.api.rest.notification;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2018/5/25.
 */
public class DeleteNotificationSystemKeepAliveApiTest {

    @Test
    public void testCall() {
        Map dataMap = new DeleteNotificationSystemKeepAliveApi<Map>(EsightHelper.getEsight()).doCall("NMSinfo2", "test_by_hy", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
