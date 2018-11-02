package com.huawei.vcenterpluginui.task;

import com.huawei.vcenterpluginui.services.NotificationAlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlarmComponentPollingTask {

    @Autowired
    private NotificationAlarmService notificationAlarmService;

    @Scheduled(fixedDelay = 30000L)
    public void pollingComponent() {
        notificationAlarmService.pollingComponent();
    }
}
