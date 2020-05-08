package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import com.huawei.esight.utils.HttpRequestUtil;
import java.util.Collections;
import org.springframework.http.HttpMethod;


/**
 * Created by hyuan on 2017/6/29.
 */
public class DeleteTemplateApi<T> extends EsightOpenIdCallable<T> {

  public DeleteTemplateApi(Esight esight) {
    super(esight);
  }
  
  public DeleteTemplateApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/template/del";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.POST;
  }
  
  /**
   * 删除配置模板.
   * @param templateName 模板名称
   * @param returnType 返回参数类型
   * @return 指定返回参数类型
   */
  public T doCall(String templateName, Class<T> returnType) {
    String body = HttpRequestUtil.concatParamAndEncode(
        Collections.singletonMap("templateName", templateName));
    return super.call(body, null, returnType);
  }
}
