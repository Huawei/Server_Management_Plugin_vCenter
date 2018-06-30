package com.huawei.vcenterpluginui.task;

import com.huawei.vcenterpluginui.services.SyncServerHostService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("SyncServerHostJob")
public class SyncServerHostTask {

    @Autowired
    private SyncServerHostService syncServerHostService;

    private static final Log LOGGER = LogFactory.getLog(SyncServerHostTask.class);

    /**
     * 同步eSight和vCenter的服务器主机
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000L)
    public void syncServerHost() {
        LOGGER.info("schedule start.");
        syncServerHostService.syncServerHost();
        LOGGER.info("schedule end.");
    }

}
