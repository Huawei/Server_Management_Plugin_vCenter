package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.entity.VCenterInfo;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Map;

public interface VCenterInfoService {

    int addVCenterInfo(VCenterInfo vCenterInfo) throws SQLException;

    int saveVCenterInfo(VCenterInfo vCenterInfo, HttpSession session) throws SQLException;

    Map<String, Object> findVCenterInfo() throws SQLException;

    VCenterInfo getVCenterInfo() throws SQLException;

    boolean disableVCenterInfo();

    void deleteHAData();

    void deleteHASyncAndDeviceData();
}
