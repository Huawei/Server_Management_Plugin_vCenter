package com.huawei.esight.api.rest.notification;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2018/5/25.
 */
public class PutNotificationSystemKeepAliveApiTest {

    @Test
    public void testCall() {
        Map dataMap = new PutNotificationSystemKeepAliveApi<Map>(EsightHelper.getEsight()).doCall("NMSinfo2", "1b7a6992-5d5f-4091-a49f-ec2e09640a05", "http://192.168.10.83/notification", "JSON", "test_by_hy", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
