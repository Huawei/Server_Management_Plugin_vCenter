package com.huawei.vcenterpluginui.mvc;

import com.huawei.esight.exception.NoOpenIdException;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ResponseBodyBean;
import com.huawei.vcenterpluginui.entity.ResponseListBean;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.services.ESightService;
import com.huawei.vcenterpluginui.services.VCenterInfoService;
import com.huawei.vcenterpluginui.utils.VersionUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * esight 配置 控制层
 *
 * @author licong
 */
@RequestMapping(value = "/services/esight")
public class ESightController extends BaseController {

  private final ESightService _eSightService;

  @Autowired
  private VCenterInfoService vCenterInfoService;

  @Autowired
  public ESightController(@Qualifier("eSightService") ESightService eSightService) {
    _eSightService = eSightService;
  }

  // Empty controller to avoid compiler warnings in huawei-vcenter-plugin-ui's
  // bundle-context.xml
  // where the bean is declared
  public ESightController() {
    _eSightService = null;
  }

  /**
   * save eSight message.
   */
  @RequestMapping(value = "", method = RequestMethod.POST)
  @ResponseBody
  public ResponseBodyBean saveESight(HttpServletRequest request, @RequestBody ESight eSight,
      HttpSession session) throws SQLException {
    return _eSightService.saveESight(eSight, session) > 0 ? success() : failure();
  }

  /**
   * update eSight message.
   */
  @RequestMapping(value = "", method = RequestMethod.PUT)
  @ResponseBody
  public ResponseBodyBean updateESight(HttpServletRequest request, @RequestBody ESight eSight,
      HttpSession session) throws SQLException {
    return _eSightService.saveESight(eSight, session) > 0 ? success() : failure();
  }

  /**
   * delete eSight message.
   */
  @RequestMapping(value = "/rm", method = RequestMethod.POST)
  @ResponseBody
  public ResponseBodyBean deleteESight(HttpServletRequest request, @RequestBody String ids,
      HttpSession session) throws SQLException, IOException {
    return _eSightService.deleteESights(ids, session) > 0 ? success() : failure();
  }

  /**
   * get eSight List.
   */
  @RequestMapping(value = "", method = RequestMethod.GET)
  @ResponseBody
  public ResponseListBean getESightList(HttpServletRequest request,
      @RequestParam(required = false) String ip, @RequestParam(required = false) Integer pageNo,
      @RequestParam(required = false) Integer pageSize) throws SQLException {
    List<ESight> eSightList = _eSightService.getESightList(ip, pageNo, pageSize);
    VCenterInfo vCenterInfo = vCenterInfoService.getVCenterInfo();
    for (ESight eSight : eSightList) {
      // alarm definition
      int alarmDefinition = eSight.getAlarmDefinition();
      if (alarmDefinition != 2) {
        if (vCenterInfo != null && vCenterInfo.isPushEvent() && "1".equals(eSight.getReservedInt1())
            && "1".equals(eSight.getReservedInt2()) && eSight.getAlarmDefinition() == 1) {
          eSight.setAlarmDefinition(1);
        } else {
          eSight.setAlarmDefinition(0);
        }
      }
      // provider
      int haProvider = eSight.getHaProvider();
      if (haProvider != 0) {
        if (vCenterInfo != null && vCenterInfo.isState() && "1".equals(eSight.getReservedInt1())
            && "1".equals(eSight.getReservedInt2()) && eSight.getHaProvider() == 1) {
          eSight.setHaProvider(1);
        } else {
          eSight.setHaProvider(0);
        }
      }
    }
    return new ResponseListBean(success(eSightList),
        _eSightService.getESightListCount(ip));
  }

  /**
   * test eSight.
   */
  @RequestMapping(value = "/test", method = RequestMethod.PUT)
  @ResponseBody
  public ResponseBodyBean testESight(HttpServletRequest request, @RequestBody ESight eSight)
      throws SQLException {
    Map dataMap = _eSightService.connect(eSight);
    Object code = dataMap.get("code");
    if (code == null || 0 != (Integer) dataMap.get("code")) {
      throw new NoOpenIdException(String.valueOf(code), String.valueOf(dataMap.get("description")));
    }
    return success();
  }

  /**
   * get VCenter version
   */
  @RequestMapping(value = "/version", method = RequestMethod.GET)
  @ResponseBody
  public ResponseBodyBean getVersion(HttpServletRequest request, HttpServletResponse response)
      throws UnsupportedEncodingException {
    String rqHd = request.getHeader("Accept-Language");
    if (rqHd != null) {
      String rqHeader = URLEncoder.encode(rqHd, StandardCharsets.UTF_8.displayName());
      response.addHeader("Accept-Language", rqHeader);
    }
    return success(VersionUtils.getVersion());
  }
}
