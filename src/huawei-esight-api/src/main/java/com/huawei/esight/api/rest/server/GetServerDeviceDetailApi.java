package com.huawei.esight.api.rest.server;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import java.util.Collections;
import org.springframework.http.HttpMethod;


/**
 * Created by hyuan on 2017/6/29.
 */
public class GetServerDeviceDetailApi<T> extends EsightOpenIdCallable<T> {

  public GetServerDeviceDetailApi(Esight esight) {
    super(esight);
  }
  
  public GetServerDeviceDetailApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/device/detail";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }
  
  public T doCall(String dn, Class<T> returnType) {
    return super.call(null, Collections.singletonMap("dn", dn), returnType);
  }
}
