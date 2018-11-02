package com.huawei.vcenterpluginui.entity;

import java.util.Map;

public class ServerDeviceDetail {

    private int esightId;
    private String dn;
    private String component;
    private String uuid;
    private String healthState;
    private String presentState;

    public ServerDeviceDetail(int esightId, String dn, String component, Map<String, Object> componentInfoMap) {
        this.esightId = esightId;
        this.dn = dn;
        this.component = component;
        this.uuid = String.valueOf(componentInfoMap.get("uuid"));
        this.healthState = String.valueOf(componentInfoMap.get("healthState")).split("\\.")[0];
        this.presentState = String.valueOf(componentInfoMap.get("presentState"));
    }

    public ServerDeviceDetail(int esightId, String dn, String component, String uuid, String healthState, String presentState) {
        this.esightId = esightId;
        this.dn = dn;
        this.component = component;
        this.uuid = uuid;
        this.healthState = healthState;
        this.presentState = presentState;
    }

    /**
     * present state 0
     * @param esightId
     * @param dn
     * @param component
     */
    public ServerDeviceDetail(int esightId, String dn, String component) {
        this(esightId, dn, component, null, "-1", "0");
    }

    public int getEsightId() {
        return esightId;
    }

    public String getDn() {
        return dn == null ? "" : dn;
    }

    public String getComponent() {
        return component == null ? "" : component;
    }

    public String getUuid() {
        return uuid == null ? "" : uuid;
    }

    public String getHealthState() {
        return healthState == null ? "" : healthState;
    }

    public String getPresentStatus() {
        return presentState == null ? "" : presentState;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public void setHealthState(String healthState) {
        this.healthState = healthState;
    }

    public void setPresentState(String presentState) {
        this.presentState = presentState;
    }

    @Override
    public String toString() {
        return "ServerDeviceDetail{" +
                "esightId=" + esightId +
                ", dn='" + dn + '\'' +
                ", component='" + component + '\'' +
                ", uuid='" + uuid + '\'' +
                ", healthState='" + healthState + '\'' +
                ", presentState='" + presentState + '\'' +
                '}';
    }
}
