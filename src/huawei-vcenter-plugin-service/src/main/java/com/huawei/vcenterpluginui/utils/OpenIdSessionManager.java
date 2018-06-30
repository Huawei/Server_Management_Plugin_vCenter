package com.huawei.vcenterpluginui.utils;

import com.huawei.vcenterpluginui.services.GlobalSession;

import javax.servlet.http.HttpSession;

/**
 * Created by hyuan on 2017/6/29.
 */
public class OpenIdSessionManager {

    private static final int SESSION_TIMEOUT = 30 * 60;

    private static final String PREFIX_ATTR = "openId_";

    private volatile static HttpSession GLOBAL_SESSION = new GlobalSession();

    public static HttpSession getGlobalSession() {
        return GLOBAL_SESSION;
    }

    public static String getOpenIdFromSession(HttpSession session, String esightIp) {
        return session == null ? null : (String) session.getAttribute(getAttributeKey(esightIp));
    }

    public static void setOpenIdToSession(HttpSession session, String openId, String esightIp) {
        //setTimeout(session);
        session.setAttribute(getAttributeKey(esightIp), openId);
    }
    
    private static String getAttributeKey(String esightIp) {
        return PREFIX_ATTR + esightIp;
    }

    private static void setTimeout(HttpSession session) {
        session.setMaxInactiveInterval(SESSION_TIMEOUT);
    }
}
