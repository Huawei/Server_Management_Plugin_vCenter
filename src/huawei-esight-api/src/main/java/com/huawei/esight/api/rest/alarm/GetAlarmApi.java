package com.huawei.esight.api.rest.alarm;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2019/2/21.
 */
public class GetAlarmApi<T> extends EsightOpenIdCallable<T> {

  public GetAlarmApi(Esight esight) {
    super(esight);
  }

  public GetAlarmApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/alarm";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }

  /**
   * 查询预定条件的告警信息列表.
   * @param severity 告警级别
   * @param clearStatus 清除状态
   * @param ackStatus 确认状态
   * @param startTime 首次发生时间的起始UTC毫秒数
   * @param endTime 首次发生时间的截止UTC毫秒数
   * @param alarmName 告警名称，不支持字符串模糊匹配
   * @param alarmSource 告警源，不支持字符串模糊匹配
   * @param location 定位信息，如A区13号楼，不支持字符串模糊匹配
   * @param neDN neDN过滤
   * @param pageNo 分页查询的第几页，支持1～999页，默认取第1页
   * @param pageSize 分页查询的每页记录数
   * @param returnType
   * @return
   */
  public T doCall(String severity, Integer clearStatus, Integer ackStatus, Long startTime, Long endTime,
      String alarmName, String alarmSource, String location, String neDN, Integer pageNo, Integer pageSize,
      Class<T> returnType) {
    Map<String, String> urlParamMap = new HashMap<String, String>();
    urlParamMap.put("severity", severity == null ? "" : severity);
    urlParamMap.put("clearStatus", clearStatus == null ? "" : String.valueOf(clearStatus));
    urlParamMap.put("ackStatus", ackStatus == null ? "" : String.valueOf(ackStatus));
    urlParamMap.put("startTime", startTime == null ? "" : String.valueOf(startTime));
    urlParamMap.put("endTime", endTime == null ? "" : String.valueOf(endTime));
    urlParamMap.put("alarmName", alarmName == null ? "" : alarmName);
    urlParamMap.put("alarmSource", alarmSource == null ? "" : alarmSource);
    urlParamMap.put("location", location == null ? "" : location);
    urlParamMap.put("neDN", neDN == null ? "" : neDN);
    urlParamMap.put("pageSize", pageSize == null ? "" : String.valueOf(pageSize));
    urlParamMap.put("pageNo", pageNo == null ? "" : String.valueOf(pageNo));
    return super.call(null, urlParamMap, returnType);
  }
}
