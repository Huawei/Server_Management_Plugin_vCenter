package com.huawei.esight.api.provider;

import com.huawei.esight.bean.Esight;
import com.huawei.esight.exception.NoOpenIdException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Created by harbor on 8/23/2017.
 */
public class DestoryOpenIdProvider extends DefaultOpenIdProvider {

  public DestoryOpenIdProvider(Esight esight) {
    super(esight);
  }
  
  private String openid = null;
  
  @Override
  protected HttpMethod httpMethod() {
    return HttpMethod.DELETE;
  }
  
  @Override
  protected HttpHeaders header() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("openid", openid);
    
    return headers;
  }
  
  /**
   * 注销登录会话.
   * @param openid 会话ID
   */
  public void logout(String openid) {
    Map<String, String> urlParamMap = new HashMap<String, String>();
    urlParamMap.put("openid", openid);
    this.openid = openid;
    Map dataMap = call(null, urlParamMap, Map.class);
    Object code = dataMap.get("code");
    if (code == null || 0 != (Integer) dataMap.get("code")) {
      throw new NoOpenIdException(String.valueOf(code), String.valueOf(dataMap.get("description")));
    }
  }

}
