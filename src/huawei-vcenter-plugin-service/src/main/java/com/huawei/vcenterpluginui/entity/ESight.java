package com.huawei.vcenterpluginui.entity;

import java.io.Serializable;

import com.huawei.esight.bean.Esight;
import com.huawei.vcenterpluginui.utils.CipherUtils;

public class ESight extends Esight implements Serializable {

	public ESight(String hostIp, int hostPort, String loginAccount, String loginPwd) {
		super(hostIp, hostPort, loginAccount, loginPwd);
	}

	public ESight() {
		super(null, 0, null, null);
	}

	private static final long serialVersionUID = -9063117479397179007L;

	private int id;
	private String aliasName;
	private String latestStatus;
	private String reservedInt1; // -- HA状态：0/null-未同步 1-已同步 2-未同步(取消订阅)
	private String reservedInt2; // -- 保活状态: 0/null-未订阅 1-已订阅 2-未订阅(取消订阅)
	private String reservedStr1;
	private String reservedStr2;
	private String lastModify;
	private String createTime;
	private String systemId;

	private int haProvider; // -- HA Provider状态：0/null-未创建 1-已创建 2-创建失败
	private int alarmDefinition; // -- 告警订阅创建状态：0/null-未创建 1-已创建 2-创建有失败

	public int getHaProvider() {
		return haProvider;
	}

	public void setHaProvider(int haProvider) {
		this.haProvider = haProvider;
	}

	public int getAlarmDefinition() {
		return alarmDefinition;
	}

	public void setAlarmDefinition(int alarmDefinition) {
		this.alarmDefinition = alarmDefinition;
	}

	public String getLatestStatus() {
		return latestStatus;
	}

	public void setLatestStatus(String latestStatus) {
		this.latestStatus = latestStatus;
	}

	public String getReservedInt1() {
		return reservedInt1;
	}

	public void setReservedInt1(String reservedInt1) {
		this.reservedInt1 = reservedInt1;
	}

	public String getReservedInt2() {
		return reservedInt2;
	}

	public void setReservedInt2(String reservedInt2) {
		this.reservedInt2 = reservedInt2;
	}

	public String getReservedStr1() {
		return reservedStr1;
	}

	public void setReservedStr1(String reservedStr1) {
		this.reservedStr1 = reservedStr1;
	}

	public String getReservedStr2() {
		return reservedStr2;
	}

	public void setReservedStr2(String reservedStr2) {
		this.reservedStr2 = reservedStr2;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public String getLastModify() {
		return lastModify;
	}

	public void setLastModify(String lastModify) {
		this.lastModify = lastModify;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getSystemId() {
		return systemId;
	}

	@Override
    public String toString() {
        return "ESight [id=" + id + ", hostIp=" + getHostIp() + ", hostPort=" + getHostPort() + ", loginAccount=" + getLoginAccount()
                + ", loginPwd=******" + ", latestStatus=" + latestStatus + ", reservedInt1=" + reservedInt1
                + ", reservedInt2=" + reservedInt2 + ", reservedStr1=" + reservedStr1 + ", reservedStr2=" + reservedStr2
                + ", lastModify=" + lastModify + ", createTime=" + createTime + "]";
    }

	/**
	 * 加密eSight对象的登录密码
	 * @param esight
	 */
	public static void updateEsightWithEncryptedPassword(ESight esight) {
		if (esight != null ) {
			esight.setLoginPwd(CipherUtils.aesEncode(esight.getLoginPwd()));
		}
	}

	/**
	 * 解密eSight对象的登录密码
	 * @param esight
	 */
	public static void decryptedPassword(ESight esight) {
		if (esight != null) {
			esight.setLoginPwd(CipherUtils.aesDncode(esight.getLoginPwd()));
		}
	}

}
