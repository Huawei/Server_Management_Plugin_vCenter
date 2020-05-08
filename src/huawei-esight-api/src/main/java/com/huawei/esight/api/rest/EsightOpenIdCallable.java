package com.huawei.esight.api.rest;

import com.huawei.esight.api.provider.DefaultOpenIdProvider;
import com.huawei.esight.api.provider.OpenIdProvider;
import com.huawei.esight.bean.Esight;
import com.huawei.esight.utils.HttpRequestUtil;
import com.huawei.esight.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Created by hyuan on 2017/6/29.
 */
public abstract class EsightOpenIdCallable<T> extends EsightCallable<T> {

  private OpenIdProvider openIdProvider;
  
  public EsightOpenIdCallable(Esight esight) {
    this(esight, new DefaultOpenIdProvider(esight));
  }
  
  public EsightOpenIdCallable(Esight esight, OpenIdProvider openIdProvider) {
    super(esight);
    this.openIdProvider = openIdProvider;
  }
  
  @Override
  protected HttpHeaders header() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("openid", openIdProvider.provide());
    
    if (HttpMethod.GET != httpMethod()) {
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }
    
    return headers;
  }
  
  @Override
  public T call(String body, Map<String, String> urlParam, Class<T> returnType) {
    T t = super.call(body, urlParam, returnType);
    // open id 可能在调用功能后发现已无效，需要更新并重新调用方法
    if (!openIdProvider.isOpenIdExpired(t)) {
      openIdProvider.updateOpenId();
      t = super.call(body, urlParam, returnType);
    }
    return t;
  }
  
  /**
   * 调用API并返回指定参数类型.
   * @param bodyMap 请求包体map
   * @param returnType 返回参数类型 
   * @return 指定参数类型
   */
  public T call(Map bodyMap, Class<T> returnType) {
    Map<String, String> bodyDataMap = new HashMap<>();
    for (Object tmpKey : bodyMap.keySet()) {
      String key = tmpKey.toString();
      Object value = bodyMap.get(key);
      if (value instanceof Map) {
        bodyDataMap.put(key, JsonUtil.writeAsString((Map) value));
      } else {
        bodyDataMap.put(key, value.toString());
      }
    }
    String body = HttpRequestUtil.concatParamAndEncode(bodyDataMap);
    return call(body, null, returnType);
  }
}
