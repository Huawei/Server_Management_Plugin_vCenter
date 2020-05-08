package com.huawei.esight.api.rest.softwaresource;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class PostSoftwareUploadApi<T> extends EsightOpenIdCallable<T> {

  public PostSoftwareUploadApi(Esight esight) {
    super(esight);
  }
  
  public PostSoftwareUploadApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/softwaresource/upload";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.POST;
  }
  
  public T doCall(Map bodyDataMap, Class<T> returnType) {
    return super.call(bodyDataMap, returnType);
  }
}
