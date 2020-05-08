package com.huawei.esight.exception;

/**
 * Created by hyuan on 2017/6/29.
 */
public class EsightException extends RuntimeException {
  private String code;
  private String message;
  
  public EsightException() {
    super();
  }
  
  public EsightException(String message) {
    super(message);
    this.message = message;
  }
  
  /**
   * 构造方法.
   * @param code 错误编码
   * @param message 错误信息
   */
  public EsightException(String code, String message) {
    super(message);
    this.code = code;
    this.message = message;
  }
  
  public String getCode() {
    return code;
  }
  
  public void setCode(String code) {
    this.code = code;
  }
  
  @Override
  public String getMessage() {
    return message;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
}
