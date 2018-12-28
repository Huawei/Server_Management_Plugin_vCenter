package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.utils.ThumbprintsUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;

/**
 * Created by hyuan on 2017/6/8.
 */
public class InstantiationBeanServiceImpl implements
        ApplicationListener<ContextRefreshedEvent>, InstantiationBeanService {

    private static final Log LOGGER = LogFactory.getLog(InstantiationBeanServiceImpl.class);

    private SystemService systemService;

    private VmActionService vmActionService;

    @Autowired
    private VCenterInfoService vCenterInfoService;

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

    public void setVmActionService(VmActionService vmActionService) {
        this.vmActionService = vmActionService;
    }

    @Override
    public void init() {
        try {
            systemService.initDB();
            ThumbprintsUtils.updateContextTrustThumbprints(vCenterInfoService.getThumbprints());
            vCenterInfoService.syncAlarmDefinitions();

            // initialize supported version
            //Collection<String> supportedVersion = vmActionService.getSupportedVersions();
            //LOGGER.info("Supported version: " + supportedVersion);

            // log current version
            // LOGGER.info("Current version: " + vmActionService.getVersion());
        } catch (Exception e) {
            LOGGER.warn(e);
        }
    }
}
