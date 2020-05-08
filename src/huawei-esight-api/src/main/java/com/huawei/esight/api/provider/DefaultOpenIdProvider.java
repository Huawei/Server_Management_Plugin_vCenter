package com.huawei.esight.api.provider;

import com.huawei.esight.api.rest.EsightCallable;
import com.huawei.esight.bean.Esight;
import com.huawei.esight.exception.NoOpenIdException;
import com.huawei.esight.utils.JsonUtil;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Created by hyuan on 2017/6/29.
 */
public class DefaultOpenIdProvider extends EsightCallable<Map> implements OpenIdProvider {
  
  private String opendid = null;
  
  public static final String SIGNIN_URL = "/rest/openapi/sm/session";
  
  public static final int ESIGHT_OPENID_EXPIRED_CODE = 1204;
  
  public DefaultOpenIdProvider(Esight esight) {
    super(esight);
  }
  
  protected String uri() {
    return SIGNIN_URL;
  }
  
  protected HttpMethod httpMethod() {
    return HttpMethod.PUT;
  }
  
  @Override
  protected HttpHeaders header() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("userid", esight.getLoginAccount());
    headers.add("value", esight.getLoginPwd());
    return headers;
  }
  
  @Override
  public String provide() {
    
    if (this.opendid != null) {
      return this.opendid;
    }
    
    Map dataMap = call(null, null, Map.class);
    Object code = dataMap.get("code");
    if (code == null || 0 != (Integer) dataMap.get("code")) {
      throw new NoOpenIdException(String.valueOf(code), String.valueOf(dataMap.get("description")));
    }
    this.opendid = dataMap.get("data").toString();
    return this.opendid;
  }
  
  @Override
  public boolean isOpenIdExpired(Object result) {
    Map dataMap = JsonUtil.object2Map(result);
    if (dataMap == null) {
      return false;
    }
    Object code = dataMap.get("code");
    if (ESIGHT_OPENID_EXPIRED_CODE == (Integer) code) {
      //openid expired
      return false;
    }
    return true;
  }
  
  @Override
  public void updateOpenId() {
    this.opendid = null;
    provide();
  }

}
