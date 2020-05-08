package com.huawei.esight.api.rest.alarm;

import com.huawei.esight.api.EsightHelper;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hyuan on 2019/2/21.
 */
public class GetAlarmApiTest {

  @Test
  public void testCall() {
    Map dataMap = new GetAlarmApi<Map>(EsightHelper.getEsight())
        .doCall("1,2,3", null, null, null, null, null, null, null, "NE=34603108", 1, 10, Map.class);
    System.out.println(dataMap);
    Assert.assertEquals(0, dataMap.get("code"));
  }
}
