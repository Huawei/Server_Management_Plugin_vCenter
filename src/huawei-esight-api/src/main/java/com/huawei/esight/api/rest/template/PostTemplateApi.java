package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import com.huawei.esight.exception.ParseJsonException;
import com.huawei.esight.utils.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class PostTemplateApi<T> extends EsightOpenIdCallable<T> {

  public PostTemplateApi(Esight esight) {
    super(esight);
  }
  
  public PostTemplateApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/template";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.POST;
  }
  
  /**
   * 创建网络配置模板.
   * @param jsonBody 请求包体
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String jsonBody, Class<T> returnType) {
    try {
      Map bodyDataMap = JsonUtil.readAsMap(jsonBody);
      if(bodyDataMap == null){
        throw new ParseJsonException("jsonBody is null");
      }
      return doCall(bodyDataMap, returnType);
    } catch (Exception e) {
      throw new ParseJsonException(e.getMessage());
    }
  }
  
  /**
   * 创建网络配置模板.
   * @param templateName 模板名称
   * @param templateType 模板类型
   * @param templateDesc 模板描述
   * @param templateProp 模板属性
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String templateName, String templateType, 
      String templateDesc, String templateProp, Class<T> returnType) {
    Map<String, String> bodyMap = new HashMap<String, String>();
    bodyMap.put("templateName", templateName);
    bodyMap.put("templateType", templateType);
    bodyMap.put("templateDesc", templateDesc);
    bodyMap.put("templateProp", templateProp);
    return doCall(bodyMap, returnType);
  }
  
  public T doCall(Map bodyDataMap, Class<T> returnType) {
    return super.call(bodyDataMap, returnType);
  }
}
