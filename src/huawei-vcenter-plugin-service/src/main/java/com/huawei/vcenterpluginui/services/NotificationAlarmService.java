package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.vcenterpluginui.entity.ESight;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.servlet.http.HttpSession;

public interface NotificationAlarmService {

    /**
     * 订阅消息
     * @param eSight
     * @param openIdProvider
     * @param desc
     * @return
     */
    Map subscribeAlarm(ESight eSight, OpenIdProvider openIdProvider, String desc);

    /**
     * 订阅消息
     * @param eSight
     * @param session
     * @param desc
     * @return
     * @throws SQLException
     */
    Map subscribeAlarm(ESight eSight, HttpSession session, String desc);

    /**
     * 订阅消息
     * @param esightIp
     * @param session
     * @param desc
     * @return
     */
    Map subscribeAlarm(String esightIp, HttpSession session, String desc);

    /**
     * 退订消息
     * @param eSight
     * @param openIdProvider
     * @param desc
     * @return
     * @throws SQLException
     */
    Map unsubscribeAlarm(ESight eSight, OpenIdProvider openIdProvider, String desc);

    /**
     * 退订消息
     * @param eSight
     * @param session
     * @param desc
     * @return
     * @throws SQLException
     */
    Map unsubscribeAlarm(ESight eSight, HttpSession session, String desc);

    /**
     * 退订消息
     * @param esightIp
     * @param session
     * @param desc
     * @return
     * @throws SQLException
     */
    Map unsubscribeAlarm(String esightIp, HttpSession session, String desc);

    /**
     * 告警回调函数
     *
     * @param alarmBody
     * @param fromIP
     *
     */
    void handleCallbackEvent(String alarmBody, String fromIP) throws SQLException;

    /**
     * 处理事件队列
     */
    void start();

    /**
     * 取消所有告警订阅
     */
    void unsubscribeAll();

    /**
     * 删除所有provider
     * @return
     */
    Boolean uninstallProvider();

    /**
     * 同步eSight历史告警
     * @param eSight
     */
    void syncHistoricalEvents(ESight eSight, boolean syncHost, boolean subscribeAlarm);

    /**
     * 同步eSight历史告警
     * @param eSight
     * @param neDN
     * @param syncHost
     * @param subscribeAlarm
     */
    void syncHistoricalEvents(ESight eSight, String neDN, boolean syncHost, boolean subscribeAlarm);

    /**
     * 同步所有eSight历史告警
     * @param syncHost
     */
    void syncHistoricalEvents(boolean syncHost, boolean subscribeAlarm);

    /**
     * 获取后台任务执行器
     * @return
     */
    Executor getBgTaskExecutor();

    /**
     * delete data from all tables
     */
    void cleanData();

    int deleteAlarmAndHADn(int esightHostId, String dn);
}
