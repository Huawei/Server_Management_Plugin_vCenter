package com.huawei.esight.api.rest;

import com.huawei.esight.bean.Esight;
import com.huawei.esight.utils.HttpRequestUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public abstract class EsightCallable<T> implements Callable<T> {

  protected Esight esight;
  
  /**
   * 构造函数.
   * @param esight eSight配置
   */
  public EsightCallable(Esight esight) {
    this.esight = esight;
  }
  
  /**
   * 设置请求地址URI.
   * @return 请求地址URI
   */
  protected abstract String uri();
  
  /**
   * 设置请求方法.
   * @return 请求方法
   */
  protected abstract HttpMethod httpMethod();
  
  /**
   * 设置请求头.
   * @return 请求头
   */
  protected HttpHeaders header() {
    return null;
  }
  
  /**
   * 设置请求http或https协议.
   * @return 请求协议
   */
  protected String getProtocol() {
    return "https";
  }
  
  /**
   * 设置请求URL地址.
   * @return 请求URL地址
   */
  protected String getUrl() {
    return getProtocol() + "://" + this.esight.getHostIp() + ":" 
        + this.esight.getHostPort() + uri();
  }
  
  @Override
  public T call(String body, Map<String, String> urlParam, Class<T> returnType) {
    String url = getUrl();
    if (urlParam != null && !urlParam.isEmpty()) {
      url += ("?" + HttpRequestUtil.concatParam(urlParam));
    }
    return HttpRequestUtil.requestWithBody(url, httpMethod(), header(), body, returnType).getBody();
  }
}
