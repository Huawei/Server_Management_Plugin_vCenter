package com.huawei.vcenterpluginui.entity;

import java.util.Date;

/**
 * Created by Rays on 2018/4/24.
 */
public class ESightServerChildDN {

    private int id;
    private int esightHaServerId;
    private String dn;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEsightHaServerId() {
        return esightHaServerId;
    }

    public void setEsightHaServerId(int esightHaServerId) {
        this.esightHaServerId = esightHaServerId;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ESightServerChildDN{" +
                "id=" + id +
                ", esightHaServerId=" + esightHaServerId +
                ", dn='" + dn + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
