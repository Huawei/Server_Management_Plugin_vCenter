package com.huawei.vcenterpluginui.mvc;

import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.services.ESightService;
import com.huawei.vcenterpluginui.services.VCenterInfoService;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * vcenter 配置 控制层
 *
 * @author licong
 */
@RequestMapping(value = "/services/vcenter")
public class VCenterController extends BaseController {

    private VCenterInfoService vCenterInfoService;

    @Autowired
    private ESightService eSightService;

    @Autowired
    public VCenterController(@Qualifier("vCenterInfoService") VCenterInfoService vCenterInfoService) {
      this.vCenterInfoService = vCenterInfoService;
    }

    // Empty controller to avoid compiler warnings in huawei-vcenter-plugin-ui's
    // bundle-context.xml
    // where the bean is declared
    public VCenterController() {
      this.vCenterInfoService = null;
    }

    /**
     * save eSight message.
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBodyBean saveVCenterInfo(HttpServletRequest request, @RequestBody VCenterInfo vCenterInfo, HttpSession session) throws SQLException {
        boolean result = vCenterInfoService.saveVCenterInfo(vCenterInfo, session) > 0;
        if (result) {
            List<ESight> eSightList = eSightService.getESightList(null, -1, -1);
            List<ESight> subscribedList = new ArrayList<>();
            List<ESight> unsubscribedList = new ArrayList<>();
            Map<String, List<ESight>> dataMap = new HashMap<>();
            for (ESight eSight : eSightList) {
                if ("1".equals(eSight.getReservedInt1())) {
                    subscribedList.add(eSight);
                } else {
                    unsubscribedList.add(eSight);
                }
            }
            if (vCenterInfo.isState()) {
                dataMap.put("success", subscribedList);
                dataMap.put("fail", unsubscribedList);
            } else {
                dataMap.put("fail", subscribedList);
                dataMap.put("success", unsubscribedList);
            }

            return success(dataMap);
        } else {
            return failure();
        }
    }

    /**
     * get vcenter info.
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseBodyBean findVCenterInfo(HttpServletRequest request) throws SQLException {
        return success(vCenterInfoService.findVCenterInfo());
    }

    @RequestMapping(value = "/ips", method = RequestMethod.GET)
    @ResponseBody
    public ResponseBodyBean findLocalIps(HttpServletRequest request) throws SocketException {
        return success(getLocalIp());
    }

    public static final List<String> getLocalIp() throws SocketException {
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        List<String> ipList = new ArrayList();
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().equals("127.0.0.1")) {
                    ipList.add(ip.getHostAddress());
                }
            }
        }
        return ipList;
    }
}
