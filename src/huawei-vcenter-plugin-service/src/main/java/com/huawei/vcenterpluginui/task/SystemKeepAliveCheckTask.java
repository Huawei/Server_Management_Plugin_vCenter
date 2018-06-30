package com.huawei.vcenterpluginui.task;

import com.huawei.vcenterpluginui.services.SystemKeepAliveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SystemKeepAliveCheckTask {

    @Autowired
    private SystemKeepAliveService systemKeepAliveService;

    /**
     * 定时检查系统保活
     */
    @Scheduled(fixedDelay = SystemKeepAliveService.POLLING_INTERVAL)
    public void pollingComponent() {
        systemKeepAliveService.checkSubscription();
    }

}
