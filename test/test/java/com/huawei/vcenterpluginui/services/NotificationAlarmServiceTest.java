package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.DefaultOpenIdProvider;
import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.mvc.ESightControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by Horace on 2018/4/17.
 */
public class NotificationAlarmServiceTest extends ContextSupported {

    @Autowired
    private NotificationAlarmService notificationAlarmService;

    @Test
    public void subscribeAlarm() {
        ESight eSight = ESightControllerTest.newESight();
        Map<String, Object> result = notificationAlarmService.subscribeAlarm(eSight, new DefaultOpenIdProvider(eSight), "test");
        Assert.assertEquals(1508, result.get("code"));
    }

    @Test
    public void pollingComponent() {
        notificationAlarmService.pollingComponent();
    }

}