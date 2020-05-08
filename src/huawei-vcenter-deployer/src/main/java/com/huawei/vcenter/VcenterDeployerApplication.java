package com.huawei.vcenter;

import com.huawei.vcenter.utils.KeytookUtil;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VcenterDeployerApplication {

  protected static final Logger LOGGER = LoggerFactory.getLogger(VcenterDeployerApplication.class);

  public static void main(String[] args) {
    try {
      KeytookUtil.genKey();
      LOGGER.info("Starting server...");
    } catch (IOException e) {
      e.printStackTrace();
    }

    SpringApplication.run(VcenterDeployerApplication.class, args);

    try {
      //String url = "";
      //String host = InetAddress.getLocalHost().getHostAddress();
      LOGGER.info("Server has been started.");
      List<String> hosts = getLocalIp();
      StringBuffer buffer = new StringBuffer();
      for (String host : hosts) {
        buffer.append("\r\n").append("https://").append(host).append(":8443");
      }
      LOGGER.info("Use either URL that vCenter can access to open page: " + buffer.toString());
//    } catch (UnknownHostException e) {
//      e.printStackTrace();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  public static final List<String> getLocalIp() throws SocketException {
    Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
    InetAddress ip = null;
    List<String> ipList = new ArrayList();
    while (allNetInterfaces.hasMoreElements()) {
      NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
      Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
      while (addresses.hasMoreElements()) {
        ip = addresses.nextElement();
        if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().equals("127.0.0.1")) {
          ipList.add(ip.getHostAddress());
        }
      }
    }
    return ipList;
  }

}
