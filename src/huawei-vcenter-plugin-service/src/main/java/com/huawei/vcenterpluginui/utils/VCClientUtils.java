package com.huawei.vcenterpluginui.utils;

public class VCClientUtils {

  private static final String FLASH_CLIENT = "vsphere-client";

  private static final String HTML5_CLIENT = "vsphere-ui";

  private static final String WEB_CLIENT = System.getProperty("user.name");

  public static String getFlashClientValue() {
    return FLASH_CLIENT;
  }

  public static String getHtml5ClientValue() {
    return HTML5_CLIENT;
  }

  public static boolean isFlashClient() {
    return FLASH_CLIENT.equalsIgnoreCase(getWebClient());
  }

  public static boolean isHtml5Client() {
    return HTML5_CLIENT.equalsIgnoreCase(getWebClient());
  }

  public static String getWebClient() {
    return WEB_CLIENT;
  }
}
