package com.huawei.vcenter.utils;

import com.huawei.vcenter.Constants;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * Created by hyuan on 2017/7/5.
 */
public class Validations {

  protected static final Log LOGGER = LogFactory.getLog(Validations.class);

  private static final String RESTURL_UNSUBSCRIBE = "https://%s/vsphere-client/vcenterpluginui/rest/services/notification/unsubscribe";
  //private static final String RESTURL_UNSUBSCRIBE = "https://%s:9443/ui/vcenterpluginui/rest/services/notification/unsubscribe";

  private static final HttpHeaders HEADERS = new HttpHeaders();

  private static final JsonParser JSON_PARSER = new JacksonJsonParser();

  static {
    HEADERS.add("Content-Type", "application/x-www-form-urlencoded");
  }

  public static Map onSubmit(String packageUrl, String vcenterUsername, String vcenterPassword,
      String vcenterIP, String vcenterPort) {
    String version = getPackageVersion();
    if ((version == null) || (version.trim().equals(""))) {
      return Collections.singletonMap("error", "E001");
    }
    return onSubmit(packageUrl, vcenterUsername, vcenterPassword, vcenterIP, vcenterPort, version);
  }

  public static Map onSubmit(String packageUrl, String vcenterUsername, String vcenterPassword,
      String vcenterIP, String vcenterPort, String version) {
    if (!packageUrl.startsWith("https://")) {
      return Collections.singletonMap("error", "E002");
    }
    if ((vcenterIP == null) || (vcenterIP.isEmpty())) {
      return Collections.singletonMap("error", "E003");
    }
    if ((vcenterPort == null) || (vcenterPort.isEmpty())) {
      return Collections.singletonMap("error", "E004");
    }
    String serverThumbprint;
    try {
      serverThumbprint = KeytookUtil.getKeystoreServerThumbprint();
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.singletonMap("error", "E005");
    }
    String pluginKey = "com.huawei.vcenterpluginui";
    String url;
    try {
      url = encodeUrlFileName(packageUrl);
    } catch (UnsupportedEncodingException e) {
      url = packageUrl;
    }
    String response = unsubscribeAlarm(vcenterIP, vcenterUsername, vcenterPassword, "install");
    if (response != null) {
      try {
        Map<String, Object> result = JSON_PARSER.parseMap(response);
        String resultCode = (String) result.get("code");
        if ("-99999".equals(resultCode)) {
          LOGGER.info(result.get("description"));
        } else if ("-1".equals(resultCode)) {
          LOGGER.info("No HA provider can be removed.");
          VcenterRegisterRunner
              .run(version, url, serverThumbprint, vcenterIP, vcenterPort, vcenterUsername,
                  vcenterPassword, pluginKey);
        } else if ("-70001".equals(resultCode)) { // DB Exceptions
          LOGGER.info("No service to uninstall provider");
          VcenterRegisterRunner
              .run(version, url, serverThumbprint, vcenterIP, vcenterPort, vcenterUsername,
                  vcenterPassword, pluginKey);
        } else {
          LOGGER.info("HA Provider has been removed.");
          VcenterRegisterRunner
              .run(version, url, serverThumbprint, vcenterIP, vcenterPort, vcenterUsername,
                  vcenterPassword, pluginKey);
        }
      } catch (Exception e) {
        LOGGER.info("Cannot uninstall provider");
        VcenterRegisterRunner
            .run(version, url, serverThumbprint, vcenterIP, vcenterPort, vcenterUsername,
                vcenterPassword, pluginKey);
      }
    } else {
      LOGGER.info("No service to uninstall provider");
      VcenterRegisterRunner
          .run(version, url, serverThumbprint, vcenterIP, vcenterPort, vcenterUsername,
              vcenterPassword, pluginKey);
    }
    return Collections.singletonMap("info", "check log");
  }

  private static String encodeUrlFileName(String url) throws UnsupportedEncodingException {
    String path = url.substring(0, url.indexOf("/package/") + "/package/".length());
    String file = URLEncoder.encode(url.substring(path.length()), "UTF-8").replaceAll("\\+", "%20");
    return path + file;
  }

  public static Map unRegister(String packageUrl, String vcenterUsername, String vcenterPassword,
      String vcenterIP, String vcenterPort) {
    if ((vcenterIP == null) || (vcenterIP.isEmpty())) {
      return Collections.singletonMap("error", "E003");
    }
    if ((vcenterPort == null) || (vcenterPort.isEmpty())) {
      return Collections.singletonMap("error", "E004");
    }
    String pluginKey = "com.huawei.vcenterpluginui";
    LOGGER.info("Removing vCenter plugin data, please wait patiently...");
    String response = unsubscribeAlarm(vcenterIP, vcenterUsername, vcenterPassword, "uninstall");
    if (response != null) {
      try {
        Map<String, Object> result = JSON_PARSER.parseMap(response);
        String resultCode = (String) result.get("code");
        if ("-99999".equals(resultCode)) {
          LOGGER.info(result.get("description"));
        } else if ("-1".equals(resultCode)) {
          LOGGER.info("No HA provider can be removed.");
          VcenterRegisterRunner.unRegister(vcenterIP, vcenterPort, vcenterUsername,
              vcenterPassword, pluginKey);
        } else if ("-70001".equals(resultCode)) { // DB Exceptions
          LOGGER.info("No service to uninstall provider");
          VcenterRegisterRunner.unRegister(vcenterIP, vcenterPort, vcenterUsername,
              vcenterPassword, pluginKey);
        } else {
          LOGGER.info("HA Provider has been removed.");
          VcenterRegisterRunner.unRegister(vcenterIP, vcenterPort, vcenterUsername,
              vcenterPassword, pluginKey);
        }
      } catch (Exception e) {
        LOGGER.info("Cannot uninstall provider");
        VcenterRegisterRunner.unRegister(vcenterIP, vcenterPort, vcenterUsername,
            vcenterPassword, pluginKey);
      }
    } else {
      LOGGER.info("No service to uninstall provider");
      VcenterRegisterRunner.unRegister(vcenterIP, vcenterPort, vcenterUsername,
          vcenterPassword, pluginKey);
    }
    return Collections.singletonMap("info", "check log");
  }

  public static String unsubscribeAlarm(String vcenterIP, String vcenterUsername,
      String vcenterPassword, String action) {
    String result = null;
    try {
      Map<String, String> bodyParamMap = new HashMap<String, String>();
      bodyParamMap.put("vcenterUsername", vcenterUsername);
      bodyParamMap.put("vcenterPassword", vcenterPassword);
      bodyParamMap.put("action", action);
      String body = HttpRequestUtil.concatParamAndEncode(bodyParamMap);

      result = HttpRequestUtil
          .requestWithBody(String.format(RESTURL_UNSUBSCRIBE, vcenterIP), HttpMethod.POST, HEADERS,
              body, String.class).getBody();
      LOGGER.debug("unsubscribe: " + result);
    } catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);
    }
    return result;
  }

  public static Map onloadChecker(HttpServletRequest request) {
    Map<String, Object> returnMap = new HashMap<>();

    File keyFile = new File(Constants.KEYSTORE_FILE);
    if (!keyFile.exists()) {
      return Collections.singletonMap("error", "E006");
    }

    List<String> packageNameList = new ArrayList<>();
    List<String> versionList = new ArrayList<>();

    // check file
    File rootFile = new File("./");
    File[] fileList = rootFile.listFiles();
    if (fileList != null) {
      for (File file : fileList) {
        if (file.getName().lastIndexOf(".zip") >= 0) {
          // check version
          String version = getPackageVersion(file.getName());
          if ((version == null) || (version.trim().equals(""))) {
            continue;
          }
          packageNameList.add(file.getName());
          versionList.add(version);
        }
      }
    }

    if (packageNameList.isEmpty()) {
      return Collections.singletonMap("error", "E007");
    }

    returnMap.put("packageNameList", packageNameList);
    returnMap.put("versionList", versionList);
    returnMap.put("key", keyFile.getAbsolutePath());
    returnMap.put("path",
        "https://" + request.getServerName() + ":" + request.getServerPort() + "/package/");
    return returnMap;
  }

  private static String getPackageVersion() {
    String version = null;
    try {
      version = ZipUtils.getVersionFromPackage(Constants.UPDATE_FILE);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return version;
  }

  private static String getPackageVersion(String file) {
    String version = null;
    try {
      version = ZipUtils.getVersionFromPackage(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return version;
  }
}
