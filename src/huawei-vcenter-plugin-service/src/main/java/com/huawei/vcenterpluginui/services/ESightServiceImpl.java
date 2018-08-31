package com.huawei.vcenterpluginui.services;

import com.huawei.esight.api.provider.DefaultOpenIdProvider;
import com.huawei.esight.utils.JsonUtil;
import com.huawei.vcenterpluginui.dao.ESightHAServerDao;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.NoEsightException;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.provider.SessionOpenIdProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the EchoService interface
 */
public class ESightServiceImpl extends ESightOpenApiService implements ESightService {

    private NotificationAlarmService notificationAlarmService;

    @Autowired
    private VCenterInfoService vCenterInfoService;

    @Autowired
    private SyncServerHostService syncServerHostService;

    @Autowired
    private ESightHAServerDao eSightHAServerDao;

    @Override
    public int saveESight(ESight eSight, HttpSession session) throws SQLException {
        ESight existEsight = eSightDao.getESightByIp(eSight.getHostIp());
        int result;
        if (existEsight == null) {
            // 更新session中的openId
            new SessionOpenIdProvider(eSight, session).updateOpenId();

            // 加密esight密码
            ESight.updateEsightWithEncryptedPassword(eSight);

            result = eSightDao.saveESight(eSight);
        } else {
        	if(eSight.getLoginAccount()==null||eSight.getLoginAccount().isEmpty()){
        	    // 未更新用户名密码
        		eSight.setLoginAccount(existEsight.getLoginAccount());
        		eSight.setLoginPwd(existEsight.getLoginPwd());
                ESight.updateEsightWithEncryptedPassword(eSight);
        	} else {
                // 更新session中的openId
                new SessionOpenIdProvider(eSight, session).updateOpenId();
                // 更新了用户名密码，加密esight密码
                ESight.updateEsightWithEncryptedPassword(eSight);
            }
            eSight.setId(existEsight.getId());
            result = eSightDao.updateESight(eSight);
        }
        // background sync and subscribe alarm
//        VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
//        if (vCenterInfo != null && vCenterInfo.isState()) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    syncServerHostService.syncServerHost();
//                }
//            }).start();
//        }
        return result;
    }

    @Override
    public List<ESight> getESightList(String ip, int pageNo, int pageSize) throws SQLException {
        List<ESight> eSightList = eSightDao.getESightList(ip, pageNo, pageSize);
        if (eSightList.isEmpty()) {
            throw new NoEsightException();
        }
        return eSightList;
    }

    @Override
    public Map connect(ESight eSight) {
        return new DefaultOpenIdProvider(eSight).call(null, null, Map.class);
    }

    @Override
    public boolean updateHAStatus(String ip, String status) {
        try {
            return eSightDao.updateHAStatus(ip, status);
        } catch (SQLException e) {
            LOGGER.error("Failed to update HA status", e);
            throw new VcenterException(e.getMessage());
        }
    }

    @Override
    public boolean updateSystemKeepAliveStatus(String ip, String status) {
        try {
            return eSightDao.updateSystemKeepAliveStatus(ip, status);
        } catch (SQLException e) {
            LOGGER.error("Failed to update system keep alive status", e);
            throw new VcenterException(e.getMessage());
        }
    }

    @Override
	public int deleteESights(String ids, HttpSession session) throws SQLException, IOException {
		Map<String, Object> idMap = JsonUtil.readAsMap(ids);
		List<Integer> id = (List<Integer>)idMap.get("ids");

		// 待取消订阅报警列表
        List<ESight> eSightList = new ArrayList<>();
        VCenterInfo vCenterInfo = null;
        vCenterInfo = vCenterInfoService.getVCenterInfo();
        if (vCenterInfo != null && (vCenterInfo.isState() || vCenterInfo.isPushEvent())) {
            for (Integer esightId : id) {
                eSightList.add(eSightDao.getESightById(esightId));
            }
        }

		int result = eSightDao.deleteESight(id);

		// 删除成功，取消订阅报警
        if (result > 0) {
            for (ESight eSight : eSightList) {
                try {
                    LOGGER.info("unsubscribe by deleting esight: " + eSight);
                    notificationAlarmService.unsubscribeAlarm(eSight, session, "vcenter_del_esight");
                    eSightHAServerDao.deleteAll(eSight.getId());
                } catch (Exception e) {
                    LOGGER.error("Failed to unsubscribe alarm: " + eSight, e);
                }
            }
        }
        return result;
	}

	@Override
	public int getESightListCount(String ip) throws SQLException {
		return eSightDao.getESightListCount(ip);
	}

    public void setNotificationAlarmService(NotificationAlarmService notificationAlarmService) {
        this.notificationAlarmService = notificationAlarmService;
    }

}
