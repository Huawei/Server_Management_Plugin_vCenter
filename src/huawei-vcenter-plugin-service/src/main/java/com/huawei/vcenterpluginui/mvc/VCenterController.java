package com.huawei.vcenterpluginui.mvc;

import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.VcenterException;
import com.huawei.vcenterpluginui.services.ESightService;
import com.huawei.vcenterpluginui.services.VCenterInfoService;
import com.vmware.connection.ConnectionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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
  public ResponseBodyBean saveVCenterInfo(HttpServletRequest request,
      @RequestBody VCenterInfo vCenterInfo, HttpSession session) throws SQLException {
    int result = 0;
    try {
      result = vCenterInfoService.saveVCenterInfo(vCenterInfo, session);
    } catch (ConnectionException e) {
      LOGGER.warn("can not connect to vCenter, ", e);
      throw new VcenterException("-90007", e.getMessage());
    }
    if (result < 0) {
      return failure(String.valueOf(result), String.valueOf(result));
    } else if (result > 0) {
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
      if (vCenterInfo.isState() || vCenterInfo.isPushEvent()) {
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

  @RequestMapping(value = "/cert", method = RequestMethod.POST)
  @ResponseBody
  public ResponseBodyBean importCert(HttpServletRequest request, @RequestParam String password) {
    int result = 0;
    try {
      CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
          request.getSession().getServletContext());
      if (multipartResolver.isMultipart(request)) {
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Iterator<String> it = multiRequest.getFileNames();
        while (it.hasNext()) {
          InputStream inputStream = multiRequest.getFile(it.next()).getInputStream();
          result = vCenterInfoService
              .saveJksThumbprints(inputStream, password);
          inputStream.close();
        }
        return new ResponseBodyBean(String.valueOf(result), null, null);
      }
    } catch (IOException e) {
      LOGGER.error("IO error", e);
    }
    return failure();
  }

  public static List<String> getLocalIp() throws SocketException {
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
