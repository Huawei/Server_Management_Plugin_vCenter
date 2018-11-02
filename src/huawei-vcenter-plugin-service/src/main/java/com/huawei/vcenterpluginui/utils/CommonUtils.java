package com.huawei.vcenterpluginui.utils;

/**
 * Created by Rays on 2018/4/16.
 */
public class CommonUtils {

    public static String formatUUID(String uuid) {
        return uuid != null ? uuid.toLowerCase() : null;
    }

    public static String concatESightHostIdAndDN(int eSightHostId, String dn) {
        return String.format("%d_%s", eSightHostId, dn);
    }
}
