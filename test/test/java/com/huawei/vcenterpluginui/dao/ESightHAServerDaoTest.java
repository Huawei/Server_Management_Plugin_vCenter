package com.huawei.vcenterpluginui.dao;

import com.google.gson.Gson;
import com.huawei.vcenterpluginui.ContextSupported;
import com.huawei.vcenterpluginui.entity.ESight;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rays on 2018/4/9.
 */
public class ESightHAServerDaoTest extends ContextSupported {

    @Autowired
    private ESightHAServerDao eSightHAServerDao;

    @Autowired
    private ESightDao eSightDao;

    @Test
    public void getAllESightHAServers() throws SQLException {
        List<ESightHAServer> allESightHAServers = eSightHAServerDao.getAllESightHAServers();
        System.out.println(new Gson().toJson(allESightHAServers));
    }

    @Test
    public void getESightHAServerByDN() throws SQLException {
        ESightHAServer eSightHAServer = eSightHAServerDao.getESightHAServerByDN(11, "NE=34603009");
        System.out.println(eSightHAServer);
    }

    @Test
    public void addESightHAServer() throws Exception {
        List<ESight> allESights = eSightDao.getAllESights();
        if (allESights.isEmpty()) {
            return;
        }
        ESightHAServer eSightHAServer = buildESightHAServer();
        eSightHAServer.seteSightHostId(allESights.get(0).getId());
        int row = eSightHAServerDao.addESightHAServer(eSightHAServer);
        System.out.println("row: " + row + " id: " + eSightHAServer.getId());
    }

    private ESightHAServer buildESightHAServer() {
        ESightHAServer eSightHAServer = new ESightHAServer();
        eSightHAServer.seteSightHostId(1);
        eSightHAServer.setUuid("s" + System.currentTimeMillis());
        eSightHAServer.setProviderSid("2ca1ca0a-1dd2-11b2-9d5b-0018e1c5d866");
        eSightHAServer.seteSightServerStatus("0");
        eSightHAServer.seteSightServerType("rack");
        eSightHAServer.seteSightServerDN("dn=test");
        eSightHAServer.setStatus(ESightHAServer.STATUS_NOT_SYNC);
        return eSightHAServer;
    }

    @Test
    public void deleteAllAndBatchAdd() throws Exception {
        LinkedList<ESightHAServer> list = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            list.add(buildESightHAServer());
            Thread.sleep(1L);
        }
        List<ESightHAServer> eSightHAServers = eSightHAServerDao.deleteAllAndBatchAdd(list);
        System.out.println(new Gson().toJson(eSightHAServers));
    }

    @Test
    public void deleteAll() throws Exception {
        int row = eSightHAServerDao.deleteAll(1);
        System.out.println("row: " + row);
    }

    @Test
    public void getESightHAServers() {
        ESightHAServer eSightHAServer = eSightHAServerDao.getESightHAServer(1, "test");
        System.out.println(eSightHAServer);
    }

    @Test
    public void getESightHAServersByDN() throws SQLException {
        List<ESightHAServer> eSightHAServers = eSightHAServerDao.getESightHAServersByDN(1, "dn=test");
        System.out.println(eSightHAServers);
    }
}