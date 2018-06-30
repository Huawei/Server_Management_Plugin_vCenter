package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.dao.ESightDao;
import com.huawei.vcenterpluginui.dao.ESightHAServerDao;
import com.huawei.vcenterpluginui.dao.VCenterInfoDao;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.utils.CipherUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

public class VCenterInfoServiceImpl extends ESightOpenApiService implements VCenterInfoService {

    @Autowired
    private VCenterInfoDao vCenterInfoDao;

    @Autowired
    private SyncServerHostService syncServerHostService;

    @Autowired
    private NotificationAlarmService notificationAlarmService;

    @Autowired
    private ESightDao eSightDao;

    @Autowired
    private ESightHAServerDao eSightHAServerDao;

    @Autowired
    private VCenterHAService vCenterHAService;

    @Override
    public int addVCenterInfo(VCenterInfo vCenterInfo) throws SQLException {
        return vCenterInfoDao.addVCenterInfo(vCenterInfo);
    }

    private void encode(VCenterInfo vCenterInfo) {
        vCenterInfo.setPassword(CipherUtils.aesEncode(vCenterInfo.getPassword()));
    }

    @Override
    public int saveVCenterInfo(final VCenterInfo vCenterInfo,final HttpSession session) throws SQLException {
        VCenterInfo vCenterInfo1 = vCenterInfoDao.getVCenterInfo();
        int returnValue = 0;
        if (vCenterInfo1 != null) {
            vCenterInfo1.setUserName(vCenterInfo.getUserName());
            vCenterInfo1.setState(vCenterInfo.isState());
            vCenterInfo1.setHostIp(vCenterInfo.getHostIp());
            if (vCenterInfo.getPassword() != null && !"".equals(vCenterInfo.getPassword())) {
                vCenterInfo1.setPassword(vCenterInfo.getPassword());
                encode(vCenterInfo1);
            }
            if (vCenterInfo1.isState()) {
                vCenterHAService.createProvider(vCenterInfo1, false);
            }
            returnValue = vCenterInfoDao.updateVCenterInfo(vCenterInfo1);
        } else {
            encode(vCenterInfo);
            if (vCenterInfo.isState()) {
                vCenterHAService.createProvider(vCenterInfo, false);
            }
            returnValue = addVCenterInfo(vCenterInfo);
        }

        if (vCenterInfo.isState()) {
            syncServerHostService.syncServerHost(true);
        } else {
            try {
                List<ESight> eSightList = eSightDao.getAllESights();
                for (ESight eSight : eSightList) {
                    try {
                        LOGGER.info("unsubscribe by client: " + eSight);
                        if ("1".equals(eSight.getReservedInt1())) {
                            notificationAlarmService.unsubscribeAlarm(eSight, session, "unsubscribe_by_client");
                        }
                        eSightHAServerDao.deleteAll(eSight.getId());
                    } catch (Exception e) {
                        LOGGER.error("Failed to unsubscribe alarm: " + eSight, e);
                    }
                }
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return returnValue;
    }

    @Override
    public Map<String, Object> findVCenterInfo() throws SQLException {
        Map<String, Object> returnMap = new HashMap<>();
        VCenterInfo vCenterInfo = vCenterInfoDao.getVCenterInfo();
        if(vCenterInfo != null){
            returnMap.put("USER_NAME", vCenterInfo.getUserName());
            returnMap.put("STATE",vCenterInfo.isState());
            returnMap.put("HOST_IP",vCenterInfo.getHostIp());
        }
        return returnMap;
    }

    @Override
    public VCenterInfo getVCenterInfo() throws SQLException {
        return vCenterInfoDao.getVCenterInfo();
    }

    @Override
    public boolean disableVCenterInfo() {
        try {
            return vCenterInfoDao.disableVCenterInfo();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void deleteHAData() {
        try {
            vCenterInfoDao.deleteHAData();
        } catch (Exception e) {
            throw new VcenterException(e.getMessage());
        }
    }

    @Override
    public void deleteHASyncAndDeviceData() {
        vCenterInfoDao.deleteHASyncAndDeviceData();
    }


}
