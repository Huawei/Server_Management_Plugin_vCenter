package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetTemplateListApi<T> extends EsightOpenIdCallable<T> {

  public GetTemplateListApi(Esight esight) {
    super(esight);
  }
  
  public GetTemplateListApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/template/list";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }
  
  /**
   * 查询配置模板列表.
   * @param templateType 模板类型
   * @param pageNo 页码
   * @param pageSize 页大小
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String templateType, String pageNo, String pageSize, Class<T> returnType) {
    Map<String, String> urlParamMap = new HashMap<String, String>();
    if (pageNo != null && !pageNo.isEmpty()) {
      urlParamMap.put("pageNo", pageNo);
    }
    if (pageSize != null && !pageSize.isEmpty()) {
      urlParamMap.put("pageSize", pageSize);
    }
    if (templateType != null && !templateType.isEmpty()) {
      urlParamMap.put("templateType", templateType);
    }
    
    return call(null, urlParamMap, returnType);
  }
}
