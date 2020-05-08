package com.huawei.esight.exception;

/**
 * Created by hyuan on 2017/9/29.
 */
public class NoOpenIdException extends EsightException {

  public NoOpenIdException() {
    super();
  }
  
  public NoOpenIdException(String message) {
    super("", message);
  }
  
  public NoOpenIdException(String code, String message) {
    super(code, message);
  }
}
