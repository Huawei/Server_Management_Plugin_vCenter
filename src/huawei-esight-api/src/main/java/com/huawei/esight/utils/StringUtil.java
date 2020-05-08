package com.huawei.esight.utils;

import java.util.Locale;

/**
 * Created by hyuan on 2017/7/4.
 */
public class StringUtil {

  /**
   * 判断是否敏感键.
   * @param key 关键词
   * @return 判断结果(true/false)
   */
  public static boolean isSensitiveKey(String key) {
    if (key != null) {
      String lowerKey = key.toLowerCase(Locale.US);
      if (lowerKey.indexOf("pwd") != -1) {
        return true;
      } else if (lowerKey.indexOf("password") != -1) {
        return true;
      } else if (lowerKey.indexOf("pswd") != -1) {
        return true;
      } else if (lowerKey.indexOf("openid") != -1) {
        return true;
      } else if (lowerKey.indexOf("value") != -1) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * 将值替换为相同长度*.
   * @param value 替换字符串
   * @return 替换结果
   */
  public static String maskValue(String value) {
    return value == null ? null : value.replaceAll(".", "*");
  }

}
