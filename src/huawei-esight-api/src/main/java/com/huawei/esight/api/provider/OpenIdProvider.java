package com.huawei.esight.api.provider;

/**
 * Created by hyuan on 2017/6/29.
 */
public interface OpenIdProvider {

  /**
   * 提供open id字符串.
   * @return 会话ID
   */
  String provide();
  
  /**
   * open id可能在调用后才发现失效.<br/>
   * 对于vCenter这种将open id保存进缓存的客户端来说，该方法会检查返回结果.<br/>
   * 如果失效，会尝试调用{@link #updateOpenId}重试
   * @param result API返回结果
   * @return 验证结果(true/false)
   */
  boolean isOpenIdExpired(Object result);
  
  /**
   * 更新open id.
   */
  void updateOpenId();
}
