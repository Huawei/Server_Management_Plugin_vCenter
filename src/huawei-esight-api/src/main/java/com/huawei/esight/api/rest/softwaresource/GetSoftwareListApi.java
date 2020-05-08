package com.huawei.esight.api.rest.softwaresource;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetSoftwareListApi<T> extends EsightOpenIdCallable<T> {

  public GetSoftwareListApi(Esight esight) {
    super(esight);
  }
  
  public GetSoftwareListApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/software/list";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }
  
  /**
   * 分页查询软件源列表.
   * @param pageNo 页数
   * @param pageSize 分页大小
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String pageNo, String pageSize, Class<T> returnType) {
    Map<String, String> urlParamMap = new HashMap<String, String>();
    urlParamMap.put("pageNo", pageNo);
    urlParamMap.put("pageSize", pageSize);
    return call(null, urlParamMap, returnType);
  }
}
