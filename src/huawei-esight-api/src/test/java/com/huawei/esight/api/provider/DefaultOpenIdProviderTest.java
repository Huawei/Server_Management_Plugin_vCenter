package com.huawei.esight.api.provider;

import com.huawei.esight.api.EsightHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hyuan on 2017/6/29.
 */
public class DefaultOpenIdProviderTest {
    @Test
    public void testGetOpenId() {
        Assert.assertNotNull(new DefaultOpenIdProvider(EsightHelper.getEsight()).provide());
    }
}
