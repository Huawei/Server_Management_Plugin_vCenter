package com.huawei.vcenterpluginui.entity;

import java.util.Date;

public class HAComponent {

  private int id;
  private int esightHostId;
  private String dn;
  private int sn;
  private String component;
  private Date createTime;

  public HAComponent() {
  }

  public HAComponent(int id, int esightHostId, String dn, int sn, String component,
      Date createTime) {
    this.id = id;
    this.esightHostId = esightHostId;
    this.dn = dn;
    this.sn = sn;
    this.component = component;
    this.createTime = createTime;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public int getSn() {
    return sn;
  }

  public void setSn(int sn) {
    this.sn = sn;
  }

  public int getEsightHostId() {
    return esightHostId;
  }

  public void setEsightHostId(int esightHostId) {
    this.esightHostId = esightHostId;
  }

  public String getDn() {
    return dn;
  }

  public void setDn(String dn) {
    this.dn = dn;
  }

  @Override
  public String toString() {
    return "HAComponent{" +
        "id=" + id +
        ", esightHostId=" + esightHostId +
        ", dn='" + dn + '\'' +
        ", sn=" + sn +
        ", component='" + component + '\'' +
        ", createTime=" + createTime +
        '}';
  }
}
