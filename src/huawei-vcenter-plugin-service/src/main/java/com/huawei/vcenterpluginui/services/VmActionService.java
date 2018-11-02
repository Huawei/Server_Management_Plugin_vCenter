package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.model.VmInfo;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;

import java.util.Collection;

/**
 * Service handling the plugin VM actions
 */
public interface VmActionService {
   /**
    * Sample action1 called on the server.
    *
    * @param vmReference
    *    Internal reference to the VM object for that action.
    * @param vmInfo
    *    User-provided data to perform that action
    *
    * @return true if the action succeeds, false otherwise
    */
   public boolean backendAction1(Object vmReference, VmInfo vmInfo);

   /**
    * Sample action2 called on the server.
    * Note that no additional parameters are used since this is for a headless action
    *
    * @param vmReference
    *    Internal reference to the VM object for that action.
    *
    * @return true if the action succeeds, false otherwise
    */
   public boolean backendAction2(Object vmReference);

   /**
    * Service to retrieve service content
    * @return
    */
   public ServiceContent getServiceContent();

   void setSupportedVersionUrl(String supportedVersionUrl);

   Collection<String> getSupportedVersions();

   String getVersion();

}
