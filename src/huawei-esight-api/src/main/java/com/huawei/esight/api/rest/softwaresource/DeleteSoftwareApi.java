package com.huawei.esight.api.rest.softwaresource;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;

import java.util.Collections;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class DeleteSoftwareApi<T> extends EsightOpenIdCallable<T> {

  public DeleteSoftwareApi(Esight esight) {
    super(esight);
  }
  
  public DeleteSoftwareApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/deploy/softwaresource/delete";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.POST;
  }
  
  public T doCall(String softwareName, Class<T> returnType) {
    return call(null, Collections.singletonMap("softwareName", softwareName), returnType);
  }
}
