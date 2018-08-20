package com.huawei.vcenterpluginui.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
            // initialize supported version
            Collection<String> supportedVersion = vmActionService.getSupportedVersions();
            LOGGER.info("Supported version: " + supportedVersion);
        } catch (Exception e) {
            LOGGER.warn(e);
        }
    }
}
