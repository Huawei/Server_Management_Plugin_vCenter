package com.huawei.vcenterpluginui.mvc;

import com.huawei.vcenterpluginui.constant.ErrorPrefix;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.services.ServerApiService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 服务器列表 控制层
 *
 * @author licong
 */
@RequestMapping(value = "/services/server")
public class ServerController extends BaseController {

  private ServerApiService serverApiService;

//  @Autowired
//  private ESightHAServerService eSightHAServerService;

  @Autowired
  public ServerController(@Qualifier("serverApiService") ServerApiService serverApiService) {
    this.serverApiService = serverApiService;
  }

  // Empty controller to avoid compiler warnings in huawei-vcenter-plugin-ui's
  // bundle-context.xml
  // where the bean is declared
  public ServerController() {
    serverApiService = null;
  }

  /**
   * get server list.
   *
   * @param ip esightIp
   */
  @RequestMapping(value = "/list", method = RequestMethod.GET)
  @ResponseBody
  public ResponseBodyBean getServerList(HttpServletRequest request,
      @RequestParam String servertype,
      @RequestParam String ip,
      @RequestParam(required = false) int pageNo,
      @RequestParam(required = false) int pageSize,
      HttpSession session) throws IOException, SQLException {

    return getResultByData(serverApiService.queryServer(ip, session, servertype, pageNo, pageSize));
  }


  @RequestMapping(value = "/device/detail", method = RequestMethod.GET)
  @ResponseBody
  public ResponseBodyBean getDeviceDetail(HttpServletRequest request,
      @RequestParam String dn,
      @RequestParam String ip,
      HttpSession session) throws IOException, SQLException {

    return getResultByData(serverApiService.queryDeviceDetail(ip, dn, session),
        ErrorPrefix.SERVER_ERROR_PREFIX);
  }

  @RequestMapping(value = "/device/detail/host", method = RequestMethod.GET)
  @ResponseBody
  public ResponseBodyBean getDeviceDetailByHost(HttpServletRequest request,
      @RequestParam String objectId,
      HttpSession session) throws IOException, SQLException {
    // urn:vmomi:HostSystem:host-1473:5f0c44d4-fee6-4a3c-a50d-884c58258b34
    String host = objectId.split(":")[3];
    Map<String, String> ipDNMap = serverApiService.getIpAndDN(host);
    if (ipDNMap.isEmpty()) {
      return success(Collections.EMPTY_MAP);
    } else {
      Entry<String, String> entry = ipDNMap.entrySet().iterator().next();
      ResponseBodyBean responseBodyBean = getResultByData(
          serverApiService.queryDeviceDetail(entry.getKey(), entry.getValue(), session),
          ErrorPrefix.SERVER_ERROR_PREFIX);

      // set (CMC) after highdensity server IP
//      try {
//        if (ESightServerType.HIGH_DENSITY.value().equalsIgnoreCase(
//            eSightHAServerService.getEsightHAServerByHost(host).geteSightServerType())) {
//          Collection<Map<String, Object>> dataList = ((Collection<Map<String, Object>>) ((Map<String, Object>) responseBodyBean
//              .getData()).get("data"));
//          if (dataList != null) {
//            for (Map<String, Object> dataMap : dataList) {
//              Object ipAddress = dataMap.get("ipAddress");
//              if (ipAddress != null) {
//                dataMap.put("ipAddress", ipAddress.toString() + " (CMC)");
//              }
//            }
//          }
//        }
//      } catch (Exception e) {
//        LOGGER.error("cannot set high density CMC info");
//      }

      responseBodyBean.setDescription(entry.getKey());
      return responseBodyBean;
    }
  }

}
