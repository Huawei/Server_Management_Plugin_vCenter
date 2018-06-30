package com.huawei.vcenterpluginui.constant;

/**
 * Created by Rays on 2018/4/26.
 */
public enum ESightServerType {
    /**
     * 机架服务器
     */
    RACK("rack"),

    /**
     * 刀片服务器
     */
    BLADE("blade"),

    /**
     * 高密服务器
     */
    HIGH_DENSITY("highdensity"),

    /**
     * 存储型服务器
     */
    STORAGE_NODE("storagenode"),

    /**
     * 第三方服务器
     */
    THIRD_PARTY_SERVER("thirdpartyserver"),

    /**
     * 昆仑服务器
     */
    KUN_LUN("kunlun");

    private final String value;

    ESightServerType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * 需与HA同步的服务器类型
     */
    public static String[] getServerTypesBySync() {
        return new String[]{
                RACK.value,
                BLADE.value,
                HIGH_DENSITY.value
//                ,KUN_LUN.value
        };
    }

    /**
     * 是否含有子服务器，高密和刀片服务器含有，机架服务器部分含有
     */
    public static boolean hasChildServer(String serverType) {
        return BLADE.value.equals(serverType) || HIGH_DENSITY.value.equals(serverType) || RACK.value.equals(serverType);
    }
}
