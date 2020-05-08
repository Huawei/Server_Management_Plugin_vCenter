package com.huawei.esight.api.rest;

import java.util.Map;

/**
 * Created by hyuan on 2017/6/29.
 */
public interface Callable<T> {
  /**
   * 发送请求.
   * @param body 请求消息体
   * @param urlParam URL参数map
   * @param returnType 数据返回类型
   * @return 指定参数类型
   */
  T call(String body, Map<String, String> urlParam, Class<T> returnType);
}
