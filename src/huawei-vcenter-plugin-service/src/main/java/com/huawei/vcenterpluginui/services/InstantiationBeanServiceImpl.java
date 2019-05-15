package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.constant.SqlFileConstant;
import com.huawei.vcenterpluginui.dao.H2DataBaseDao;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VersionNotSupportException;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import com.huawei.vcenterpluginui.utils.FileUtils;
import com.huawei.vcenterpluginui.utils.ThumbprintsUtils;
import com.huawei.vcenterpluginui.utils.VCClientUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by hyuan on 2017/6/8.
 */
public class InstantiationBeanServiceImpl implements
    ApplicationListener<ContextRefreshedEvent>, InstantiationBeanService {

  private static final Log LOGGER = LogFactory.getLog(InstantiationBeanServiceImpl.class);

  private SystemService systemService;

  @Autowired
  private ESightService eSightService;

  @Autowired
  private VCenterInfoService vCenterInfoService;

  @Autowired
  private VCenterHAService vCenterHAService;

  @Autowired
  private NotificationAlarmService notificationAlarmService;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    init();
  }

  public SystemService getSystemService() {
    return systemService;
  }

  public void setSystemService(SystemService systemService) {
    this.systemService = systemService;
  }

  @Override
  public void init() {
    try {
//      boolean mergeDBAndUpdateAlarmDef = (systemService.isTableExists(SqlFileConstant.HW_VCENTER_INFO) && !systemService
//          .isColumnExists(SqlFileConstant.HW_VCENTER_INFO,
//              SqlFileConstant.HW_VCENTER_INFO_HOSTPORT));

      LOGGER.info("OS: " + System.getProperty("os.name"));
      LOGGER.info("Current web client: " + VCClientUtils.getWebClient());
      LOGGER.info("Is HTML5 client: " + VCClientUtils.isHtml5Client());
      LOGGER.info("Is Flash client: " + VCClientUtils.isFlashClient());

      // (linux only) MV files
      if (!FileUtils.isWindows()) { // Linux
        try {
          File newDbFile = new File(FileUtils.getPath(true) + "/" + H2DataBaseDao.getDBFileName());
          String oldDbFile = FileUtils.getOldDBFolder() + "/" + H2DataBaseDao.getDBFileName();
          if (new File(oldDbFile).exists() && !newDbFile.exists()) { // no DB file in new path
            // move db file
            LOGGER.info("Copying DB file from " + H2DataBaseDao.getDBFileName() + " to " + newDbFile
                .getName());
            Files.copy(Paths.get(oldDbFile),
                Paths.get(newDbFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            // move key files
            String oldFolder = FileUtils.getOldFolder();
            String newFolder = FileUtils.getPath();
            LOGGER.info("Copying key files...");
            Files.copy(Paths.get(oldFolder + "/" + FileUtils.BASE_FILE_NAME),
                Paths.get(newFolder + "/" + FileUtils.BASE_FILE_NAME),
                StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(oldFolder + "/" + FileUtils.WORK_FILE_NAME),
                Paths.get(newFolder + "/" + FileUtils.WORK_FILE_NAME),
                StandardCopyOption.REPLACE_EXISTING);
            FileUtils.setFilePermission(new File(newFolder + "/" + FileUtils.BASE_FILE_NAME));
            FileUtils.setFilePermission(new File(newFolder + "/" + FileUtils.WORK_FILE_NAME));
          }
        } catch (Exception e) {
          LOGGER.warn("Cannot move file");
        }
      }

      if ((systemService.isTableExists(SqlFileConstant.HW_VCENTER_INFO) && !systemService
          .isColumnExists(SqlFileConstant.HW_VCENTER_INFO,
              SqlFileConstant.HW_VCENTER_INFO_HOSTPORT))) {
        notificationAlarmService.getBgTaskExecutor().execute(new Runnable() {
          @Override
          public void run() {
            // unregister all alarm definitions
            List<AlarmDefinition> alarmDefinitions = vCenterInfoService.getAlarmDefinitions();
            if (!alarmDefinitions.isEmpty()) {
              final Collection<String> morValList = new HashSet<>();
              for (AlarmDefinition alarmDefinition : alarmDefinitions) {
                if (alarmDefinition.getMorValue() != null && !""
                    .equals(alarmDefinition.getMorValue().trim())) {
                  morValList.add(alarmDefinition.getMorValue());
                }
              }
              LOGGER.info("Unregistering " + morValList.size() + " alarm definitions.");
              if (!morValList.isEmpty()) {
                try {
                  vCenterHAService
                      .unregisterAlarmDef(vCenterInfoService.getVCenterInfo(),
                          new ArrayList<String>(morValList));
                } catch (Exception e) {
                  LOGGER.error("Cannot delete alarm definitions in vCenter.", e);
                }
                try {
                  vCenterInfoService.deleteAlarmDefinitions();
                } catch (Exception e) {
                  LOGGER.error("Cannot delete alarm definitions in DB.");
                }
                LOGGER.info("Removed alarm definition from vCenter");
              }
            }
          }
        });
      }

      systemService.initDB();

      ThumbprintsUtils.updateContextTrustThumbprints(vCenterInfoService.getThumbprints());

      if (VCClientUtils.isFlashClient()) {
        vCenterInfoService.syncAlarmDefinitions();

        // start alarm handling
        notificationAlarmService.start();

        final VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
        notificationAlarmService.getBgTaskExecutor().execute(new Runnable() {
          @Override
          public void run() {
            try {
              if (vCenterInfo != null) {
                ConnectedVim connectedVim = null;
                try {
                  connectedVim = vCenterHAService.getConnectedVim();
                  connectedVim.connect(vCenterInfo); // get vcenter version
                  // create provider
                  if (vCenterInfo.isState()) {
                    try {
                      vCenterHAService.createProvider(connectedVim, false);
                      eSightService.updateHAProvider(1);
                    } catch (VersionNotSupportException e) {
                      LOGGER.info("Not supported HA");
                    } catch (Exception e) {
                      LOGGER.error("Cannot create provider", e);
                      eSightService.updateHAProvider(2);
                    }
                  }
                } catch (Exception e) {
                  LOGGER.error("Cannot connect to vcenter");
                } finally {
                  if (connectedVim != null) {
                    connectedVim.disconnect();
                  }
                }
              }
            } catch (Exception e) {
              LOGGER.error("Cannot get vcenter info");
            }
          }
        });

        List<ESight> eSightList = null;
        try {
          eSightList = eSightService.getESightListWithPassword(null, -1, -1);
        } catch (SQLException e) {
          LOGGER.error("Cannot get eSight list");
        }

        if (eSightList != null) {
          for (ESight eSight : eSightList) {
            try {
              notificationAlarmService.syncHistoricalEvents(eSight, true,
                  (vCenterInfo != null && (vCenterInfo.isPushEvent() || vCenterInfo.isState())));
            } catch (Exception e) {
              LOGGER.error("Cannot sync historical event from " + eSight.getHostIp(), e);
            }
          }
        }
      }

    } catch (Exception e) {
      LOGGER.warn(e);
    }
  }
}
