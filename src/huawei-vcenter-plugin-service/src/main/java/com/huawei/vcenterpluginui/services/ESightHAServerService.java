package com.huawei.vcenterpluginui.services;

import com.huawei.vcenterpluginui.entity.ESightHAServer;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Created by Rays on 2018/4/26.
 */
public interface ESightHAServerService {

    /**
     * 根据告警dn（可能是子服务器的dn），得到已同步的服务器信息
     */
    List<ESightHAServer> getESightHAServersByDN(int eSightId, String dn) throws SQLException;

    /**
     * 根据eSightId和DN取服务器信息
     * @param eSightId
     * @param dn
     * @return 已同步或未同步的服务器
     * @throws SQLException
     */
    ESightHAServer getESightHAServerByDN(int eSightId, String dn) throws SQLException;

    /**
     * 根据告警dn（可能是子服务器的dn），得到已同步的服务器dn
     */
    Set<String> getDNs(int eSightId, String dn) throws SQLException;

    /**
     * 获取本地数据库全部服务器信息
     */
    List<ESightHAServer> getAllESightHAServers() throws SQLException;

    /**
     * 删除所有服务器信息并添加新的服务器信息
     */
    List<ESightHAServer> deleteAllAndBatchAdd(List<ESightHAServer> result) throws SQLException;

    /**
     * 根据eSightId删除所关联的服务器信息，当eSightId为null时，清空表数据
     * @param eSightId 可为null
     */
    int deleteAll(Integer eSightId) throws SQLException;
}
