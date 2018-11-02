package com.huawei.vcenter.utils;

import com.vmware.common.Main;
import com.vmware.common.ssl.TrustAll;
import com.vmware.connection.BasicConnection;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.connection.Connection;
import com.vmware.vim25.AlarmInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.EventAlarmExpression;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Horace on 2018/9/10.
 */
public class ConnectedVim extends ConnectedVimServiceBase {

  private static final Log LOGGER = LogFactory.getLog(ConnectedVim.class);

  public ConnectedVim() {
    super();
  }

  /**
   * 连接vim
   */
  private void connect(String host, String username, String password) {
    Connection basicConnection = new BasicConnection();
    URL sdkUrl = null;
    try {
      sdkUrl = new URL("https", host, "/sdk");
    } catch (MalformedURLException e) {
      throw new RuntimeException("connect vim fail.");
    }
    basicConnection.setPassword(password);
    basicConnection.setUrl(sdkUrl.toString());
    basicConnection.setUsername(username);
    this.setIgnoreCert();
    this.setHostConnection(true);
    this.setConnection(basicConnection);
    //LOGGER.info("host: " + host + " username: " + username + " password: ******");
    this.connect();
//    if (connect == null || !connect.isConnected()) {
//      throw new RuntimeException("connect vim fail.");
//    }
    //LOGGER.info("connect vim success.");
  }

  @Override
  public Connection disconnect() {
    try {
      if (connection != null) {
        return super.disconnect();
      }
    } catch (Exception e) {
      //LOGGER.warn("Failed to disconnect vcenter, " + e.getMessage());
    }
    return connection;
  }

  /**
   * Remove specific alarm definition vCenter plugin
   * @param host
   * @param username
   * @param password
   * @param eventTypeIdRegex
   */
  public void unregisterAlarmDefinitions(String host, String username, String password,
      String eventTypeIdRegex) {
    try {
      connect(host, username, password);
      List<ManagedObjectReference> alarmList = getAlarmDefinitions();
      Set<String> alarmValueToBeRemoved = new HashSet<>();
      for (ManagedObjectReference mor : alarmList) {
        try {
          PropertyFilterSpec alarmFilterSpec = createAlarmFilterSpec(mor);
          ArrayList<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
          listpfs.add(alarmFilterSpec);
          List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
          if (listobjcont == null || listobjcont.isEmpty() || !(listobjcont
              .get(0) instanceof ObjectContent)) {
            return;
          }
          ObjectContent oc = (ObjectContent) listobjcont.get(0);
          String key = null;
          boolean removeAlarm = false;
          for (DynamicProperty dynamicProperty : oc.getPropSet()) {
            if (dynamicProperty.getVal() instanceof AlarmInfo) {
              AlarmInfo alarmInfo = (AlarmInfo) dynamicProperty.getVal();
              key = alarmInfo.getKey();
            } else if (dynamicProperty.getVal() instanceof EventAlarmExpression) {
              EventAlarmExpression alarmExpression = (EventAlarmExpression) dynamicProperty
                  .getVal();
              if (alarmExpression.getEventTypeId().matches(eventTypeIdRegex)) {
                removeAlarm = true;
              }
            }
          }
          if (removeAlarm) {
            alarmValueToBeRemoved.add(key);
          }
        } catch (Exception e) {
          LOGGER.debug(e.getMessage(), e);
        }
      }
      int alarmDefSize = alarmValueToBeRemoved.size();
      LOGGER
          .info(alarmDefSize + " plugin alarm(s) left" + (alarmDefSize > 0 ? ", removing..." : ""));
      for (String alarmVal : alarmValueToBeRemoved) {
        try {
          vimPort.removeAlarm(buildAlarm(alarmVal));
        } catch (Exception e) {
          LOGGER.debug("Cannot remove: " + alarmVal);
        }
      }
    } catch (Exception e) {
      LOGGER.debug("Cannot unregister alarm definitions", e);
    } finally {
      disconnect();
    }
  }

  private ManagedObjectReference buildAlarm(String val) {
    ManagedObjectReference mor = new ManagedObjectReference();
    mor.setType("Alarm");
    mor.setValue(val);
    return mor;
  }

  private static PropertyFilterSpec createAlarmFilterSpec(
      ManagedObjectReference eventHistoryCollectorRef) {
    PropertySpec propSpec = new PropertySpec();
    propSpec.setAll(false);
    propSpec.getPathSet().add("info");
    propSpec.getPathSet().add("info.expression");
    propSpec.setType(eventHistoryCollectorRef.getType());

    ObjectSpec objSpec = new ObjectSpec();
    objSpec.setObj(eventHistoryCollectorRef);
    objSpec.setSkip(false);

    PropertyFilterSpec spec = new PropertyFilterSpec();
    spec.getPropSet().add(propSpec);
    spec.getObjectSet().add(objSpec);
    return spec;
  }

  private List<ManagedObjectReference> getAlarmDefinitions() throws RuntimeFaultFaultMsg {
    return vimPort.getAlarm(serviceContent.getAlarmManager(), serviceContent.getRootFolder());
  }

  private List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs)
      throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
    ManagedObjectReference propCollectorRef = serviceContent.getPropertyCollector();
    RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

    List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

    RetrieveResult rslts = vimPort
        .retrievePropertiesEx(propCollectorRef, listpfs, propObjectRetrieveOpts);
    if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
      listobjcontent.addAll(rslts.getObjects());
    }
    String token = null;
    if (rslts != null && rslts.getToken() != null) {
      token = rslts.getToken();
    }
    while (token != null && !token.isEmpty()) {
      rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
      token = null;
      if (rslts != null) {
        token = rslts.getToken();
        if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
          listobjcontent.addAll(rslts.getObjects());
        }
      }
    }
    return listobjcontent;
  }

  private void setIgnoreCert() {
    System.setProperty(Main.Properties.TRUST_ALL, Boolean.TRUE.toString());
    try {
      TrustAll.trust();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      //LOGGER.error(e.getMessage(), e);
    }
  }

}
