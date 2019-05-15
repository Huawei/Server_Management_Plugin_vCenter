package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.VCenterInfo;

import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Map;

public interface VCenterInfoService {

    int addVCenterInfo(VCenterInfo vCenterInfo) throws SQLException;

    int saveVCenterInfo(VCenterInfo vCenterInfo, HttpSession session) throws SQLException;

    /**
     * sync alarm definitions in background executor
     */
    void syncAlarmDefinitions();

    Map<String, Object> findVCenterInfo() throws SQLException;

    VCenterInfo getVCenterInfo() throws SQLException;

    boolean disableVCenterInfo();

    void deleteHAData();

    void deleteHASyncAndDeviceData();

    List<AlarmDefinition> getAlarmDefinitions();

    void addAlarmDefinitions(List<AlarmDefinition> alarmDefinitions);

    void deleteAlarmDefinitions() throws SQLException;

    int saveJksThumbprints(InputStream inputStream, String password);

    void saveThumbprints(String[] thumbprints);

    String[] getThumbprints() throws SQLException;
}
