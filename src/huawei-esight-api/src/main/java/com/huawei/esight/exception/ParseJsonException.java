package com.huawei.esight.exception;

/**
 * Created by hyuan on 2017/6/30.
 */
public class ParseJsonException extends EsightException {
  public ParseJsonException() {
    super();
  }
  
  public ParseJsonException(String message) {
    super("", message);
  }
}
