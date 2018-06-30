package com.huawei.vcenterpluginui.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Rays on 2018/4/9.
 */
public class VCenterInfo implements Serializable {
    private static final long serialVersionUID = 3811172759222907501L;

    private int id;
    private String hostIp;
    private String userName;
    private String password;
    private Date createTime;
    private boolean state;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
