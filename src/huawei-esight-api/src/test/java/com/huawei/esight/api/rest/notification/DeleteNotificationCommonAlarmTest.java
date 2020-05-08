package com.huawei.esight.api.rest.notification;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2018/4/8.
 */
public class DeleteNotificationCommonAlarmTest {

    @Test
    public void testCall() {
        Map dataMap = new DeleteNotificationCommonAlarm<Map>(EsightHelper.getEsight()).doCall("NMSinfo1", "openid_hy", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
