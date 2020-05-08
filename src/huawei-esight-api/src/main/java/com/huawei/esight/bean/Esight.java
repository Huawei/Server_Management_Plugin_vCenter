package com.huawei.esight.bean;

public class Esight {

  private String hostIp;
  private int hostPort;
  private String loginAccount;
  private String loginPwd;
  
  /**
   * 构造方法.
   * @param hostIp 服务器IP
   * @param hostPort 端口
   * @param loginAccount 登录账号
   * @param loginPwd 登录密码
   */
  public Esight(String hostIp, int hostPort, String loginAccount, String loginPwd) {
    this.hostIp = hostIp;
    this.hostPort = hostPort;
    this.loginAccount = loginAccount;
    this.loginPwd = loginPwd;
  }
  
  public String getHostIp() {
    return hostIp;
  }
  
  public void setHostIp(String hostIp) {
    this.hostIp = hostIp;
  }
  
  public int getHostPort() {
    return hostPort;
  }
  
  public void setHostPort(int hostPort) {
    this.hostPort = hostPort;
  }
  
  public String getLoginAccount() {
    return loginAccount;
  }
  
  public void setLoginAccount(String loginAccount) {
    this.loginAccount = loginAccount;
  }
  
  public String getLoginPwd() {
    return loginPwd;
  }
  
  public void setLoginPwd(String loginPwd) {
    this.loginPwd = loginPwd;
  }
  
  @Override
  public String toString() {
    return "Esight{" 
        + "hostIp='" + hostIp + '\'' 
        + ", hostPort=" + hostPort 
        + ", loginAccount='" + loginAccount + '\'' 
        + ", loginPwd='******'" + '}';
  }
}
