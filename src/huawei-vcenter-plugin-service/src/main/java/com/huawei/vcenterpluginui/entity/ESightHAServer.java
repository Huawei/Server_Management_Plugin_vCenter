package com.huawei.vcenterpluginui.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Rays on 2018/4/8.
 */
public class ESightHAServer implements Serializable {

    private static final long serialVersionUID = 5318065550416578423L;

    public static final int STATUS_NOT_SYNC = 0; // 未同步
    public static final int STATUS_ALREADY_SYNC = 1; // 已同步
    public static final int STATUS_HA_DISABLE = 2; // 同步后，因HA服务器问题不可用了
    public static final int STATUS_ESIGHT_DISABLE = 3; // 同步后，因eSight服务器问题不可用了
    public static final int STATUS_ESIGHT_AND_HA_DISABLE = 4; // 同步后，因eSight和HA服务器问题不可用了

    private int id;
    private int eSightHostId; // eSight表编号
    private String uuid;

    // eSight服务器信息
    private String eSightServerDN;
    private String eSightServerParentDN; // 高密或刀片服务器时管理板dn
    private String eSightServerType;
    private String eSightServerStatus; // 服务器状态，“0”：正常，“-1”：离线，“-2”：未知，其他：故障

    private int status = STATUS_NOT_SYNC;
    private String providerSid; // 注册HA得到的
    private Date lastModifyTime;
    private Date createTime;

    // vCenter ha服务器信息
    private String haHostSystem;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int geteSightHostId() {
        return eSightHostId;
    }

    public void seteSightHostId(int eSightHostId) {
        this.eSightHostId = eSightHostId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String geteSightServerDN() {
        return eSightServerDN;
    }

    public void seteSightServerDN(String eSightServerDN) {
        this.eSightServerDN = eSightServerDN;
    }

    public String geteSightServerParentDN() {
        return eSightServerParentDN;
    }

    public void seteSightServerParentDN(String eSightServerParentDN) {
        this.eSightServerParentDN = eSightServerParentDN;
    }

    public String geteSightServerType() {
        return eSightServerType;
    }

    public void seteSightServerType(String eSightServerType) {
        this.eSightServerType = eSightServerType;
    }

    public String geteSightServerStatus() {
        return eSightServerStatus;
    }

    public void seteSightServerStatus(String eSightServerStatus) {
        this.eSightServerStatus = eSightServerStatus;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getProviderSid() {
        return providerSid;
    }

    public void setProviderSid(String providerSid) {
        this.providerSid = providerSid;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getHaHostSystem() {
        return haHostSystem;
    }

    public void setHaHostSystem(String haHostSystem) {
        this.haHostSystem = haHostSystem;
    }

    @Override
    public String toString() {
        return "ESightHAServer{" +
                "id=" + id +
                ", eSightHostId=" + eSightHostId +
                ", uuid='" + uuid + '\'' +
                ", eSightServerDN='" + eSightServerDN + '\'' +
                ", eSightServerParentDN='" + eSightServerParentDN + '\'' +
                ", eSightServerType='" + eSightServerType + '\'' +
                ", eSightServerStatus='" + eSightServerStatus + '\'' +
                ", status=" + status +
                ", providerSid='" + providerSid + '\'' +
                ", lastModifyTime=" + lastModifyTime +
                ", createTime=" + createTime +
                ", haHostSystem='" + haHostSystem + '\'' +
                '}';
    }
}
