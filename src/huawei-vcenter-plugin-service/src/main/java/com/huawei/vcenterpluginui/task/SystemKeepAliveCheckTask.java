package com.huawei.vcenterpluginui.task;

import com.huawei.vcenterpluginui.services.SystemKeepAliveService;
import com.huawei.vcenterpluginui.utils.VCClientUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SystemKeepAliveCheckTask {

  private static final Log LOGGER = LogFactory.getLog(SystemKeepAliveCheckTask.class);

  @Autowired
  private SystemKeepAliveService systemKeepAliveService;


  /**
   * 定时检查系统保活
   */
  @Scheduled(fixedDelay = SystemKeepAliveService.POLLING_INTERVAL)
  public void pollingComponent() {
    if (VCClientUtils.isHtml5Client()) {
      LOGGER.info("Do not resubscribing on H5 version");
      return;
    }
    systemKeepAliveService.checkSubscription();
  }

}
