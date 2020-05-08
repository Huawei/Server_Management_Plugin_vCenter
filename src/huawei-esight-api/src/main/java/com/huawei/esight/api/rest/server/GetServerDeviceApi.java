package com.huawei.esight.api.rest.server;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetServerDeviceApi<T> extends EsightOpenIdCallable<T> {

  public GetServerDeviceApi(Esight esight) {
    super(esight);
  }
  
  public GetServerDeviceApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/device";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }
  
  /**
   * 分页查询服务器设备列表.
   * @param servertype 服务器类型
   * @param pageNo 页数
   * @param pageSize 分页大小
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String servertype, String pageNo, String pageSize, Class<T> returnType) {
    Map<String, String> urlParamMap = new HashMap<String, String>();
    urlParamMap.put("servertype", servertype);
    urlParamMap.put("start", pageNo);
    urlParamMap.put("size", pageSize);
    return super.call(null, urlParamMap, returnType);
  }
}
