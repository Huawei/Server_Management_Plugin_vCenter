package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import com.huawei.esight.exception.ParseJsonException;
import com.huawei.esight.utils.HttpRequestUtil;
import com.huawei.esight.utils.JsonUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class PostDeployTaskApi<T> extends EsightOpenIdCallable<T> {

  public PostDeployTaskApi(Esight esight) {
    super(esight);
  }
  
  public PostDeployTaskApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/task";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.POST;
  }
  
  /**
   * 创建配置任务.
   * @param jsonBody 请求包体
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String jsonBody, Class<T> returnType) {
    try {
      Map<String, Object> bodyParamMap = JsonUtil.readAsMap(jsonBody);
      if (bodyParamMap == null) {
        throw new ParseJsonException("jsonBody is null");
      }
      return doCall(bodyParamMap.get("templates").toString(), 
          bodyParamMap.get("dn").toString(), returnType);
    } catch (IOException e) {
      throw new ParseJsonException(e.getMessage());
    }
  }
  
  /**
   * 创建配置任务.
   * @param templates 模板
   * @param dn 设备名称
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String templates, String dn, Class<T> returnType) {
    Map<String, String> bodyParamMap = new HashMap<String, String>();
    bodyParamMap.put("templates", templates);
    bodyParamMap.put("dn", dn);
    String body = HttpRequestUtil.concatParamAndEncode(bodyParamMap);
    return super.call(body, null, returnType);
  }
  
  public T doCall(Map bodyDataMap, Class<T> returnType) {
    return super.call(bodyDataMap, returnType);
  }
}
