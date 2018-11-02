package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.ContextSupported;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Rays on 2018/4/8.
 */
public class SyncServerHostServiceTest extends ContextSupported {

    @Autowired
    private SyncServerHostService syncServerHostService;

    @Test
    public void syncServerHost() {
        syncServerHostService.syncServerHost();
    }
}