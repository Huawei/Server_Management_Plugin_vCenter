package com.huawei.esight.api.rest.firmware;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import java.util.Collections;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetFirmwareProgressApi<T> extends EsightOpenIdCallable<T> {

  public GetFirmwareProgressApi(Esight esight) {
    super(esight);
  }
  
  public GetFirmwareProgressApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }
  
  protected String uri() {
    return "/rest/openapi/server/firmware/basepackages/progress";
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }
  
  public T doCall(String taskName, Class<T> returnType) {
    return super.call(null, Collections.singletonMap("taskName", taskName), returnType);
  }
}
