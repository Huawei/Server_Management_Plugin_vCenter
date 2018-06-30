package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.dao.ESightHAServerDao;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Rays on 2018/4/26.
 */
public class ESightHAServerServiceImpl implements ESightHAServerService {

    @Autowired
    private ESightHAServerDao eSightHAServerDao;

    @Override
    public List<ESightHAServer> getESightHAServersByDN(int eSightId, String dn) throws SQLException {
        return eSightHAServerDao.getESightHAServersByDN(eSightId, dn);
    }

    @Override
    public ESightHAServer getESightHAServerByDN(int eSightId, String dn) throws SQLException {
        return eSightHAServerDao.getESightHAServerByDN(eSightId, dn);
    }

    @Override
    public Set<String> getDNs(int eSightId, String dn) throws SQLException {
        List<ESightHAServer> eSightHAServersByDN = getESightHAServersByDN(eSightId, dn);
        Set<String> dnSet = new HashSet<>(eSightHAServersByDN.size());
        for (ESightHAServer eSightHAServer : eSightHAServersByDN) {
            dnSet.add(eSightHAServer.geteSightServerDN());
        }
        return dnSet;
    }

    @Override
    public List<ESightHAServer> getAllESightHAServers() throws SQLException {
        return eSightHAServerDao.getAllESightHAServers();
    }

    @Override
    public List<ESightHAServer> deleteAllAndBatchAdd(List<ESightHAServer> result) throws SQLException {
        return eSightHAServerDao.deleteAllAndBatchAdd(result);
    }

    @Override
    public int deleteAll(Integer eSightId) throws SQLException {
        return eSightHAServerDao.deleteAll(eSightId);
    }

}
