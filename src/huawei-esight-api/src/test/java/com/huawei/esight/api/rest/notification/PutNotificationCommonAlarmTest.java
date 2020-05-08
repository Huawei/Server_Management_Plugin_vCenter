package com.huawei.esight.api.rest.notification;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hyuan on 2018/4/8.
 */
public class PutNotificationCommonAlarmTest {

    @Test
    public void testCall() {
        Map dataMap = new PutNotificationCommonAlarm<Map>(EsightHelper.getEsight()).doCall("NMSinfo1", "1b7a6992-5d5f-4091-a49f-ec2e09640a05", "http://192.168.10.83/notification", "JSON", "desc", Map.class);
        Assert.assertEquals(0, dataMap.get("code"));
    }
}
