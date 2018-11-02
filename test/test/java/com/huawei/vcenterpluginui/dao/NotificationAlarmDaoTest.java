package com.huawei.vcenterpluginui.dao;

import com.google.gson.Gson;
import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by Rays on 2018/4/24.
 */
public class NotificationAlarmDaoTest extends ContextSupported {

    @Autowired
    private NotificationAlarmDao notificationAlarmDao;

    @Test
    public void getESightHostIdAndDNs() throws SQLException {
        Set<String> eSightHostIdAndDNs = notificationAlarmDao.getESightHostIdAndDNs();
        System.out.println("eSightHostIdAndDNs: " + new Gson().toJson(eSightHostIdAndDNs));
    }

    @Test
    public void getServerDeviceDetailCount() throws SQLException {
        int count = notificationAlarmDao.getServerDeviceDetailCount(11, Arrays.asList("NE=NONE"));
        Assert.assertEquals(0, count);
    }

    @Test
    public void updateDeviceDetail() throws SQLException {
        //int esightId, String dn, String component, String uuid, String healthState, String presentState
        ServerDeviceDetail s1 = new ServerDeviceDetail(5, "NE=34603009", "PSU", "RackServer2CA1CA0A1DD211powerPS1chassis", "-2", "1");
        ServerDeviceDetail s2 = new ServerDeviceDetail(5, "NE=34603009", "PSU", "RackServer2CA1CA0A1DD211powerPSU-2", "1", "1");
        List<ServerDeviceDetail> serverDeviceDetailList = Arrays.asList(s1, s2);
        notificationAlarmDao.updateDeviceDetail(5, Arrays.asList("NE=34603009"),
                serverDeviceDetailList, Arrays.asList("PSU"), Arrays.asList("RackServer2CA1CA0A1DD211powerPS1chassis"));

    }
}