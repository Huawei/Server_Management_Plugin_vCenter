package com.huawei.esight.api.rest.softwaresource;

import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.api.rest.EsightOpenIdCallable;
import com.huawei.esight.bean.Esight;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2019/4/4.
 */
public class GetSoftwareSupportListApi<T> extends EsightOpenIdCallable<T> {

  public GetSoftwareSupportListApi(Esight esight) {
    super(esight);
  }

  public GetSoftwareSupportListApi(Esight esight, OpenIdProvider openIdProvider) {
    super(esight, openIdProvider);
  }

  protected String uri() {
    return "/rest/openapi/server/deploy/software/support/list";
  }

  protected HttpMethod httpMethod() {
    return HttpMethod.GET;
  }

  public T doCall(Class<T> returnType) {
    return call(null, null, returnType);
  }
}
