package com.huawei.esight.api.rest.softwaresource;

import com.huawei.esight.api.EsightHelper;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hyuan on 2017/6/29.
 */
public class GetSoftwareSupportListApiTest {

  @Test
  public void testCall() {
    Map dataMap = new GetSoftwareSupportListApi<Map>(EsightHelper.getEsight()).doCall(Map.class);
    Assert.assertEquals(0, dataMap.get("code"));
  }
}
