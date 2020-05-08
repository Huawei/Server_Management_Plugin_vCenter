package com.huawei.vcenterpluginui.task;

import com.huawei.vcenterpluginui.services.SyncServerHostService;
import com.huawei.vcenterpluginui.utils.VCClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("SyncServerHostJob")
public class SyncServerHostTask {

  @Autowired
  private SyncServerHostService syncServerHostService;

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncServerHostTask.class);

  /**
   * 同步eSight和vCenter的服务器主机
   */
  @Scheduled(fixedDelay = 30 * 60 * 1000L)
  public void syncServerHost() {
    if (VCClientUtils.isHtml5Client()) {
      LOGGER.info("Do not sync servers on H5 version");
      return;
    }
    LOGGER.info("schedule start.");
    syncServerHostService.syncServerHost();
    LOGGER.info("schedule end.");
  }

}
