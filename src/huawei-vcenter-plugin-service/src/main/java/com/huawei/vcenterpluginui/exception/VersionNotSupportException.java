package com.huawei.vcenterpluginui.exception;

/**
 * Created by hyuan on 2017/6/9.
 */
public class VersionNotSupportException extends IgnorableException {
    public VersionNotSupportException(String currentVersion) {
        super("-90006", "Version doesn't support: " + currentVersion);
    }

}
