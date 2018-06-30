package com.huawei.vcenterpluginui.constant;

import com.vmware.vim25.HealthUpdateInfo;
import com.vmware.vim25.HealthUpdateInfoComponentType;

import java.util.*;

/**
 * Created by Rays on 2018/4/16.
 */
public class DeviceComponent {

    private static final String KEY_COMPONENT_NAME = "KEY_COMPONENT_NAME";
    private static final String KEY_COMPONENT_TYPE = "KEY_COMPONENT_TYPE";
    private static final String KEY_COMPONENT_ID = "KEY_COMPONENT_ID";
    private static final String KEY_COMPONENT_DESCRIPTION = "KEY_COMPONENT_DESCRIPTION";

    // 注意：如果component有变更，可能需要更改配置文件中的vcenter.ha.provider.name.version，重新注册provider
    private static final Map<String, Map<String, String>> componentMap;

    private static final List<String> ALL_HEALTH_STATE = Arrays.asList("0", "-1", "-2", "2", "3", "4", "5", "6", "7", "8");
    private static final List<String> PUSH_HEALTH_STATE = Arrays.asList("0", "4", "6", "7", "8");

    static {
        componentMap = new HashMap<>();
//        build(componentMap, "CPU", "CPU", "CPU was error");
        build("Memory", "Memory", "DIMM configuration error", HealthUpdateInfoComponentType.MEMORY.value());
        build("Disk", "Disk", "Disk was failure", HealthUpdateInfoComponentType.STORAGE.value());
        build("PSU", "PowerSupply", "Power supply was failure.", HealthUpdateInfoComponentType.POWER.value());
        build("Fan", "Fan", "Fan redundancy was failure.", HealthUpdateInfoComponentType.FAN.value());
//        build(componentMap, "RAID", "RAIDCard", "RAID card was failure.");


    }

    private static void build(String componentName, String id, String description, String componentType) {
        HashMap<String, String> map = new HashMap<>();
        map.put(KEY_COMPONENT_NAME, componentName);
        map.put(KEY_COMPONENT_TYPE, componentType);
        map.put(KEY_COMPONENT_ID, id);
        map.put(KEY_COMPONENT_DESCRIPTION, description);
        DeviceComponent.componentMap.put(componentName, map);
    }

    public static List<String> getValidHealthState() {
        return ALL_HEALTH_STATE;
    }

    public static List<String> getPushHealthState() {
        return PUSH_HEALTH_STATE;
    }

    public static String[] getAlarmComponents() {
        return componentMap.keySet().toArray(new String[0]);
    }

    public static String getComponentId(String componentName) {
        Map<String, String> map = componentMap.get(componentName);
        if (map != null) {
            return map.get(KEY_COMPONENT_ID);
        }
        return null;
    }

    public static List<HealthUpdateInfo> getHealthUpdateInfos() {
        List<HealthUpdateInfo> healthUpdateInfos = new LinkedList<>();
        for (Map<String, String> map : componentMap.values()) {
            HealthUpdateInfo healthUpdateInfo = new HealthUpdateInfo();
            healthUpdateInfo.setComponentType(map.get(KEY_COMPONENT_TYPE));
            healthUpdateInfo.setId(map.get(KEY_COMPONENT_ID));
            healthUpdateInfo.setDescription(map.get(KEY_COMPONENT_DESCRIPTION));
            healthUpdateInfos.add(healthUpdateInfo);
        }
        return healthUpdateInfos;
    }
}
