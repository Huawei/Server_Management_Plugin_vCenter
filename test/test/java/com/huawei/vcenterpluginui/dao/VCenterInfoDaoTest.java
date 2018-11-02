package com.huawei.vcenterpluginui.dao;

import com.google.gson.Gson;
import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.Pair;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.utils.AlarmDefinitionConverter;
import com.huawei.vcenterpluginui.utils.CipherUtils;
import java.sql.SQLException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Rays on 2018/4/9.
 */
public class VCenterInfoDaoTest extends ContextSupported {

  @Autowired
  private VCenterInfoDao vCenterInfoDao;

  @Test
  public void addVCenterInfo() throws Exception {
    VCenterInfo vCenterInfo = new VCenterInfo();
    vCenterInfo.setHostIp("192.168.11.32");
    vCenterInfo.setUserName("administrator@huaweitest.com");
    vCenterInfo.setPassword(CipherUtils.aesEncode("Huawei12#$"));
    int row = vCenterInfoDao.addVCenterInfo(vCenterInfo);
    System.out.println("row: " + row + " id: " + vCenterInfo.getId());
  }

  @Test
  public void getVCenterInfo() throws SQLException {
    VCenterInfo vCenterInfo = vCenterInfoDao.getVCenterInfo();
    if (vCenterInfo == null) {
      System.out.println("vCenterInfo is null");
    } else {
      System.out.println(new Gson().toJson(vCenterInfo));
    }
  }

  @Test
  public void updateVCenterInfo() throws SQLException {
    VCenterInfo vCenterInfo = vCenterInfoDao.getVCenterInfo();
    if (vCenterInfo != null) {
      vCenterInfo.setState(true);
      int row = vCenterInfoDao.updateVCenterInfo(vCenterInfo);
      Assert.assertEquals(1, row);
    }
  }

  @Test
  public void getAlarmDefinitionDiff() {
    List<AlarmDefinition> alarmDefinitionList = new AlarmDefinitionConverter()
        .parseAlarmDefinitionList();
    Pair pair = vCenterInfoDao.getAlarmDefinitionDiff(alarmDefinitionList);
    System.out.println(pair.getKey());
    System.out.println(pair.getValue());
  }
}