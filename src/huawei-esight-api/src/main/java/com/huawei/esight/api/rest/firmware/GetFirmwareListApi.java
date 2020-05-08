package com.huawei.esight.api.rest.firmware;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetFirmwareListApi<T> extends EsightOpenIdCallable<T> {

  public GetFirmwareListApi(Esight esight) {
    super(esight);
  }
  
  public GetFirmwareListApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/firmware/basepackages/list";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }
  
  /**
   * 分页查询数据.
   * @param pageNo 页数
   * @param pageSize 页大小
   * @param returnType 返回参数类型
   * @return 返回指定参数类型
   */
  public T doCall(String pageNo, String pageSize, Class<T> returnType) {
    Map<String, String> urlParamMap = new HashMap<String, String>();
    urlParamMap.put("pageNo", pageNo);
    urlParamMap.put("pageSize", pageSize);
    return super.call(null, urlParamMap, returnType);
  }
}
