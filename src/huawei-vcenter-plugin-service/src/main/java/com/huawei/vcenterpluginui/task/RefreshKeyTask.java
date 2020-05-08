package com.huawei.vcenterpluginui.task;

import com.huawei.vcenterpluginui.dao.ESightDao;
import com.huawei.vcenterpluginui.dao.VCenterInfoDao;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.utils.CipherUtils;
import com.huawei.vcenterpluginui.utils.FileUtils;
import com.huawei.vcenterpluginui.utils.VCClientUtils;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("RefreshKeyJob")
public class RefreshKeyTask {

  @Autowired
  private ESightDao eSightDao;

  @Autowired
  private VCenterInfoDao vCenterInfoDao;

  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshKeyTask.class);

  @Scheduled(cron = "0 0 0 1 * ?")
//	@Scheduled(cron = "0 0/5 * * * ?")
  public void job1() {
    if (VCClientUtils.isHtml5Client()) {
      LOGGER.info("Do not refresh keys on H5 version");
      return;
    }
    LOGGER.info("Refresh the key...");
    try {
      //获取用户密码等信息
      List<ESight> eSightList = eSightDao.getESightListWithPwd(null, -1, -1);

      VCenterInfo vCenterInfo = vCenterInfoDao.getVCenterInfo();
      if (vCenterInfo != null) {
        vCenterInfo.setPassword(CipherUtils.aesDncode(vCenterInfo.getPassword()));
      }

      //更新密钥，重新加密
//			String fileStringKey = CipherUtils.getSafeRandomToString(CipherUtils.KEY_SIZE);
//			FileUtils.saveKey(fileStringKey,FileUtils.BASE_FILE_NAME);

      String randomKey = CipherUtils.getSafeRandomToString(CipherUtils.KEY_SIZE);
      String workKey = CipherUtils.aesEncode(randomKey, CipherUtils.getBaseKey());
      FileUtils.saveKey(workKey, FileUtils.WORK_FILE_NAME);

      if (vCenterInfo != null) {
        vCenterInfo.setPassword(CipherUtils.aesEncode(vCenterInfo.getPassword()));
        vCenterInfoDao.updateVCenterInfo(vCenterInfo);
      }

      for (ESight eSight : eSightList) {
        ESight.updateEsightWithEncryptedPassword(eSight);
        eSightDao.updateESight(eSight);
      }

    } catch (InvalidKeySpecException | UnsupportedEncodingException | SQLException | NoSuchAlgorithmException e) {
      LOGGER.error("Failed to refresh key: " + e.getMessage());
    }
  }

  public ESightDao geteSightDao() {
    return eSightDao;
  }

  public void seteSightDao(ESightDao eSightDao) {
    this.eSightDao = eSightDao;
  }

}
