package com.huawei.vcenterpluginui.entity;

import java.util.Date;

public class AlarmRecord {

  private int id;
  private int esightHostId;
  private String eventId;
  private int sn;
  private Date createTime;
  private String dn;

  public AlarmRecord() {
  }

  public AlarmRecord(int id, int esightHostId, String eventId, String dn, int sn, Date createTime) {
    this.id = id;
    this.esightHostId = esightHostId;
    this.eventId = eventId;
    this.sn = sn;
    this.createTime = createTime;
    this.dn = dn;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getEsightHostId() {
    return esightHostId;
  }

  public void setEsightHostId(int esightHostId) {
    this.esightHostId = esightHostId;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public int getSn() {
    return sn;
  }

  public void setSn(int sn) {
    this.sn = sn;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getDn() {
    return dn;
  }

  public void setDn(String dn) {
    this.dn = dn;
  }

  @Override
  public String toString() {
    return "AlarmRecord{" +
        "id=" + id +
        ", esightHostId=" + esightHostId +
        ", eventId='" + eventId + '\'' +
        ", sn=" + sn +
        ", createTime=" + createTime +
        ", dn='" + dn + '\'' +
        '}';
  }
}
