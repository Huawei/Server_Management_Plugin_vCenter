package com.huawei.vcenterpluginui.task;

import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.services.ESightService;
import com.huawei.vcenterpluginui.services.NotificationAlarmService;
import com.huawei.vcenterpluginui.utils.VCClientUtils;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("SyncHistoricEventTask")
public class SyncHistoricEventTask {

  private static final Log LOGGER = LogFactory.getLog(SyncHistoricEventTask.class);

  @Autowired
  private ESightService eSightService;

  @Autowired
  private NotificationAlarmService notificationAlarmService;

  @Scheduled(cron = "0 0 0/4 * * ?")
  public void job() {
    if (VCClientUtils.isHtml5Client()) {
      LOGGER.info("Do not sync historical events on H5 version");
      return;
    }
    synchronized (RefreshKeyTask.class) {
      LOGGER.info("Sync historical events...");

      List<ESight> eSightList = null;
      try {
        eSightList = eSightService.getESightListWithPassword(null, -1, -1);
      } catch (SQLException e) {
        LOGGER.error("Cannot get eSight list: " + e.getMessage());
      }

      if (eSightList != null) {
        for (ESight eSight : eSightList) {
          try {
            notificationAlarmService.syncHistoricalEvents(eSight, false, false);
          } catch (Exception e) {
            LOGGER.error(
                "Cannot sync historical event from " + eSight.getHostIp() + ": " + e.getMessage());
          }
        }
      }
    }
  }
}
