package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.utils.CommonUtils;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    void handleAlarm(String alarmBody, String fromIP) throws SQLException;

    /**
     * 取消所有告警订阅
     */
    void unsubscribeAll();

    /**
     * 同步服务器设备详情
     * @param eSight
     * @param dnList 没有设备详情的父子DN列表
     * @param openIdProvider
     */
    void syncServerDeviceDetails(ESight eSight, List<String> dnList, OpenIdProvider openIdProvider, Map<String, String> dnParentDNMap) throws SQLException;

    /**
     * 从服务器设备详情中获取eSight dn的唯一列表
     * 列表中元素是eSightId与dn拼接而成的
     * 参考{@link CommonUtils#concatESightHostIdAndDN(int, String)}
     */
    Set<String> getESightHostIdAndDNs() throws SQLException;

    /**
     * 删除所有provider
     * @return
     */
    Boolean uninstallProvider();

    /**
     * 轮询状态未改变组件
     */
    void pollingComponent();

    void putAlarmIfNotExist(String eSightIP, String dn, int retryCount);

    /**
     * 删除未同步的部件详情数据
     */
    int deleteNotSyncedDeviceDetails();
}
