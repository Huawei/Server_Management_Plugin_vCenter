package com.huawei.esight.api;

import com.huawei.esight.bean.Esight;

/**
 * Created by hyuan on 2017/6/29.
 */
public class EsightHelper {
    public static Esight getEsight() {
//        return new Esight("192.168.10.72", 32102, "openApiUser", "Simple.0");
//        return new Esight("192.168.11.35", 32102, "openApiUser", "Simple.0");
        return new Esight("192.168.8.154", 32102, "openApiUser", "Simple.0");
    }
}
