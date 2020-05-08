package com.huawei.esight.api.rest.template;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class PostDeployTaskDetailApi<T> extends EsightOpenIdCallable<T> {

  public PostDeployTaskDetailApi(Esight esight) {
    super(esight);
  }
  
  public PostDeployTaskDetailApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/task/detail";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }
  
  public T doCall(String taskName, Class<T> returnType) {
    return super.call(null, Collections.singletonMap("taskName", taskName), returnType);
  }
  
  public T doCall(Map bodyDataMap, Class<T> returnType) {
    return super.call(bodyDataMap, returnType);
  }
}
