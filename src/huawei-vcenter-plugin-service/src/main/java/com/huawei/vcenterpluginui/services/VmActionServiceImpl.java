package com.huawei.vcenterpluginui.services;

import com.huawei.esight.utils.HttpRequestUtil;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.model.VmInfo;
import com.huawei.vcenterpluginui.utils.AlarmDefinitionConverter;
import com.huawei.vcenterpluginui.utils.ConnectedVim;
import com.vmware.common.ssl.TrustAllTrustManager;
import com.vmware.vim25.*;
import com.vmware.vise.vim.data.VimObjectReferenceService;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.transform.dom.DOMSource;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Implementation of the VmActionService interface
 */
public class VmActionServiceImpl implements VmActionService {

   private static final Log _logger =
         LogFactory.getLog(VmActionServiceImpl.class);

   private final VimObjectReferenceService _vimObjectReferenceService;

   private static VimPortType _vimPort = initializeVimPort();

   private static final String SERVICE_INSTANCE = "ServiceInstance";

   public void setSupportedVersionUrl(String supportedVersionUrl) {
      SUPPORTED_VERSION_URL = supportedVersionUrl;
   }

   //private static String SUPPORTED_VERSION_URL = "https://192.168.11.32/sdk/vimServiceVersions.xml";
   private static String SUPPORTED_VERSION_URL = "https://127.0.0.1/sdk/vimServiceVersions.xml";

   private static final Collection<String> SUPPORTED_VERSIONS = new HashSet<>();

   private static VimPortType initializeVimPort() {
      // Static initialization is preferred because it takes a few seconds.
      VimService vimService = new VimService();
      return vimService.getVimPort();
   }

   /**
    * Static Initialization block, which will make this client trust all
    * certificates. USE THIS ONLY FOR TESTING.
    */
   static {
      HostnameVerifier hostNameVerifier = new HostnameVerifier() {
         @Override
         public boolean verify(String urlHostName, SSLSession session) {
            return true;
         }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(hostNameVerifier);

      javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
      javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
      trustAllCerts[0] = tm;
      javax.net.ssl.SSLContext sc = null;

      try {
         sc = javax.net.ssl.SSLContext.getInstance("SSL");
      } catch (NoSuchAlgorithmException e) {
         _logger.info(e);
      }

      if (sc != null) {
         javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
         sslsc.setSessionTimeout(0);
         try {
            sc.init(null, trustAllCerts, null);
         } catch (KeyManagementException e) {
            _logger.info(e);
         }
         javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(
                 sc.getSocketFactory());
      }
   }

   /**
    * Constructor used to inject the utility services (see the declaration
    * in main/resources/spring/bundle-context-osgi.xml)
    *
    * @param vimObjectReferenceService Service to access vSphere object references information.
    */
   public VmActionServiceImpl(
         VimObjectReferenceService vimObjectReferenceService) {
      _vimObjectReferenceService = vimObjectReferenceService;
   }

   /* (non-Javadoc)
    * @see com.vmware.samples.actions.VmActionService#backendAction1(java.lang.Object)
    */
   public boolean backendAction1(Object vmReference, VmInfo vmInfo) {
      // All vCenter objects sent from the UI are serialized into an internal type.
      // Use VimObjectReferenceService to get more information.
      // See DataProviderImpl.java for an example using the vSphere Web Services SDK
      // to talk to vCenter.
      String type = _vimObjectReferenceService.getResourceObjectType(vmReference);
      String value = _vimObjectReferenceService.getValue(vmReference);
      _logger.info("backendAction1 called with object type = " + type + ", value = " + value
         + ", param1 = " + vmInfo.param1 + ", param2 = " + vmInfo.param2);

      // action processing should take place on the back-end Server,
      // nothing heavy should run in this service on the vSphere Client server.
      // If back-end processing takes time it's better to return right away here
      // and let the UI deals with updates later.

      return true;
   }

   /* (non-Javadoc)
    * @see com.vmware.samples.actions.VmActionService#backendAction2(java.lang.Object)
    */
   public boolean backendAction2(Object vmReference) {
      String type = _vimObjectReferenceService.getResourceObjectType(vmReference);
      String value = _vimObjectReferenceService.getValue(vmReference);
      _logger.info("backendAction2 called with object type = " + type +
            ", value = " + value);

      // action processing should take place on the back-end Server.

      // Returning false is as an example to trigger an error message in the UI.
      return false;
   }

   public ServiceContent getServiceContent() {
      ManagedObjectReference serviceInstance = new ManagedObjectReference();
      serviceInstance.setType(SERVICE_INSTANCE);
      serviceInstance.setValue(SERVICE_INSTANCE);
      try {
         return _vimPort.retrieveServiceContent(serviceInstance);
      } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
         _logger.info("failed to get service content", runtimeFaultFaultMsg);
         return null;
      }
   }

   private ServiceContent getServiceContent(VimPortType vimPort) {
      ManagedObjectReference serviceInstance = new ManagedObjectReference();
      serviceInstance.setType(SERVICE_INSTANCE);
      serviceInstance.setValue(SERVICE_INSTANCE);
      try {
         return vimPort.retrieveServiceContent(serviceInstance);
      } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
         _logger.info("failed to get service content", runtimeFaultFaultMsg);
         return null;
      }
   }

   public Collection<String> getSupportedVersions() {
      synchronized (SUPPORTED_VERSIONS) {
         if (SUPPORTED_VERSIONS.isEmpty()) {
            DOMSource domSource = HttpRequestUtil
                .requestWithBody(SUPPORTED_VERSION_URL, HttpMethod.GET, null, "", DOMSource.class)
                .getBody();
            loopVersion(domSource.getNode());
         }
         return SUPPORTED_VERSIONS;
      }
   }

   private void loopVersion(Node node) {
      if ("version".equals(node.getNodeName())) {
         SUPPORTED_VERSIONS.add(node.getTextContent());
      } else if(node.hasChildNodes()) {
         NodeList nodeList = node.getChildNodes();
         for (int i = 0; i < nodeList.getLength(); i++) {
            loopVersion(nodeList.item(i));
         }
      }
   }

}