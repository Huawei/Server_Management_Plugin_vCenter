/* Copyright 2012 VMware, Inc. All rights reserved. -- VMware Confidential */

package com.huawei.vcenterpluginui.model;

/**
 * A data model of VM properties to retrieve.
 */
public class VmData {
   /** The Datacenter name this VM resides on. */
   public String datacenterName;

   /** Number of virtual CPUs present in this VM. */
   public String numberOfVirtualCpus;

   /** Capacity in KB for this VM's VirtualDisk. */
   public String capacityInKb;

   /** Virtual Machine name. */
   public String vmName;
}
