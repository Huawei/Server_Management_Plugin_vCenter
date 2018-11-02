package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.vcenterpluginui.entity.ESight;

import java.util.Map;

public interface SystemKeepAliveService {

    static final long POLLING_INTERVAL = 10 * 60 * 1000L;

    /**
     * 更新最新存活时间
     * @param eSightIP
     */
    void updateLastAliveTime(String eSightIP);

    /**
     * 订阅系统保活
     * @param eSight
     * @param openID
     * @param openIdProvider
     * @param desc
     * @return
     */
    Map subscribeSystemKeepAlive(ESight eSight, String openID, OpenIdProvider openIdProvider, String desc);

    /**
     * 退订系统保活
     * @param eSight
     * @param openIdProvider
     * @param desc
     * @return
     */
    Map unsubscribeSystemKeepAlive(ESight eSight, OpenIdProvider openIdProvider, String desc);

    /**
     * 处理保活消息
     * @param esightIP
     */
    void handleMessage(String esightIP);

    /**
     * 检查系统保活
     */
    void checkSubscription();

}
