package com.huawei.vcenterpluginui.dao;

import com.huawei.vcenterpluginui.constant.SqlFileConstant;
import com.huawei.vcenterpluginui.entity.ESightHAServer;
import com.huawei.vcenterpluginui.exception.DataBaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ESightHAServerDao extends H2DataBaseDao {

    private static final String INSERT_SQL = "INSERT INTO " + SqlFileConstant.HW_ESIGHT_HA_SERVER + " " +
            "(ESIGHT_HOST_ID,UUID,ESIGHT_SERVER_TYPE,ESIGHT_SERVER_STATUS,STATUS,PROVIDER_SID,HA_HOST_SYSTEM," +
            "ESIGHT_SERVER_DN,ESIGHT_SERVER_PARENT_DN,LAST_MODIFY_TIME,CREATE_TIME)" +
            " VALUES (?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";

    private void setInsertParam(PreparedStatement ps, ESightHAServer eSightHAServer) throws SQLException {
        ps.setInt(1, eSightHAServer.geteSightHostId());
        ps.setString(2, eSightHAServer.getUuid());
        ps.setString(3, eSightHAServer.geteSightServerType());
        ps.setString(4, eSightHAServer.geteSightServerStatus());
        ps.setInt(5, eSightHAServer.getStatus());
        ps.setString(6, eSightHAServer.getProviderSid());
        ps.setString(7, eSightHAServer.getHaHostSystem());
        ps.setString(8, eSightHAServer.geteSightServerDN());
        ps.setString(9, eSightHAServer.geteSightServerParentDN());
    }

    public int addESightHAServer(ESightHAServer eSightHAServer) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(INSERT_SQL);
            setInsertParam(ps, eSightHAServer);
            int row = ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                eSightHAServer.setId(rs.getInt(1));
            }
            return row;
        } catch (Exception e) {
            LOGGER.error("Failed to ad eSight HA server: " + e.getMessage());
            throw e;
        } finally {
            closeConnection(con, ps, rs);
        }
    }

    public List<ESightHAServer> deleteAllAndBatchAdd(List<ESightHAServer> list) throws SQLException {
        if (list == null || list.isEmpty()) {
            return list;
        }
        List<ESightHAServer> data;
        if (list instanceof ArrayList) {
            data = list;
        } else {
            data = new ArrayList<>(list);
        }
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            ps1 = con.prepareStatement("DELETE FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER);
            int deleteRow = ps1.executeUpdate();
            LOGGER.info("deleteRow: " + deleteRow);

            ps = con.prepareStatement(INSERT_SQL);
            for (ESightHAServer eSightHAServer : data) {
                setInsertParam(ps, eSightHAServer);
                ps.addBatch();
            }
            int[] rows = ps.executeBatch();
            con.commit();
            ps.clearBatch();
            LOGGER.info("insert rows: " + Arrays.toString(rows));
            rs = ps.getGeneratedKeys();
            final int size = data.size();
            for (int i = 0; rs.next(); i++) {
                if (i >= size) {
                    break;
                }
                data.get(i).setId(rs.getInt(1));
            }
            return data;
        } catch (DataBaseException | SQLException e) {
            LOGGER.error("Failed to delete and add eSight HA servers: " + e.getMessage());
            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
            throw new SQLException(e);
        } finally {
            close(ps1);
            closeConnection(con, ps, rs);
        }
    }

    /**
     * 查询指定的eSightHAServer
     * @param eSightId eSight编号
     * @param dn 设备唯一标识
     * @return null or ESightHAServer
     */
    public ESightHAServer getESightHAServer(int eSightId, String dn) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER
                    + " WHERE ESIGHT_HOST_ID=? AND ESIGHT_SERVER_DN=? AND STATUS=1 LIMIT 1");
            ps.setInt(1, eSightId);
            ps.setString(2, dn);
            rs = ps.executeQuery();
            if (rs.next()) {
                return buildESightHAServer(rs);
            }
        } catch (DataBaseException | SQLException e) {
            LOGGER.error("Failed to get eSight HA server: " + e.getMessage());
        } finally {
            closeConnection(con, ps, rs);
        }
        return null;
    }

    /**
     * 获取所有esight HA服务器
     */
    public List<ESightHAServer> getAllESightHAServers() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER);
            rs = ps.executeQuery();
            List<ESightHAServer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(buildESightHAServer(rs));
            }
            return result;
        } catch (DataBaseException | SQLException e) {
            LOGGER.error("Failed to get eSight HA servers: " + e.getMessage());
            throw new SQLException(e);
        } finally {
            closeConnection(con, ps, rs);
        }
    }

    public int deleteAll(Integer eSightId) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            StringBuilder sb = new StringBuilder("DELETE FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER);
            if (eSightId != null) {
                sb.append(" WHERE ESIGHT_HOST_ID=?");
            }
            ps = con.prepareStatement(sb.toString());
            if (eSightId != null) {
                ps.setInt(1, eSightId);
            }
            return ps.executeUpdate();
        } catch (DataBaseException | SQLException e) {
            LOGGER.error("Failed to delete eSight HA servers: " + e.getMessage());
            throw new SQLException(e);
        } finally {
            closeConnection(con, ps, null);
        }
    }

    public ESightHAServer getEsightHAServerByHost(String host) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER
                + " WHERE HA_HOST_SYSTEM=? LIMIT 1");
            ps.setString(1, host);
            rs = ps.executeQuery();
            if (rs.next()) {
                return buildESightHAServer(rs);
            }
        } catch (DataBaseException | SQLException e) {
            LOGGER.error("Failed to get eSight HA server by host: " + e.getMessage());
        } finally {
            closeConnection(con, ps, rs);
        }
        return null;
    }

    private ESightHAServer buildESightHAServer(ResultSet rs) throws SQLException {
        ESightHAServer eSightHAServer = new ESightHAServer();
        eSightHAServer.setId(rs.getInt("ID"));
        eSightHAServer.seteSightHostId(rs.getInt("ESIGHT_HOST_ID"));
        eSightHAServer.setStatus(rs.getInt("STATUS"));
        eSightHAServer.seteSightServerStatus(rs.getString("ESIGHT_SERVER_STATUS"));
        eSightHAServer.setProviderSid(rs.getString("PROVIDER_SID"));
        eSightHAServer.setHaHostSystem(rs.getString("HA_HOST_SYSTEM"));
        eSightHAServer.setUuid(rs.getString("UUID"));
        eSightHAServer.seteSightServerType(rs.getString("ESIGHT_SERVER_TYPE"));
        eSightHAServer.seteSightServerDN(rs.getString("ESIGHT_SERVER_DN"));
        eSightHAServer.seteSightServerParentDN(rs.getString(SqlFileConstant.COLUMN_ESIGHT_SERVER_PARENT_DN));
        eSightHAServer.setCreateTime(rs.getTimestamp("CREATE_TIME"));
        eSightHAServer.setLastModifyTime(rs.getTimestamp("LAST_MODIFY_TIME"));
        return eSightHAServer;
    }

    public ESightHAServer getESightHAServerByDN(int eSightId, String dn) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ESightHAServer eSightHAServer = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER +
                    " WHERE ESIGHT_HOST_ID=? AND ESIGHT_SERVER_DN=?");
            ps.setInt(1, eSightId);
            ps.setString(2, dn);
            rs = ps.executeQuery();
            if (rs.next()) {
                eSightHAServer = buildESightHAServer(rs);
            }
            return eSightHAServer;
        } catch (DataBaseException | SQLException e) {
            LOGGER.error("Failed to get ESight HA Server: " + e.getMessage());
            throw new SQLException(e);
        } finally {
            closeConnection(con, ps, rs);
        }
    }

    public List<ESightHAServer> getESightHAServersByDN(int eSightId, String dn) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER +
                    " WHERE ESIGHT_HOST_ID=? AND STATUS=1 AND (ESIGHT_SERVER_DN=? OR " +
                    SqlFileConstant.COLUMN_ESIGHT_SERVER_PARENT_DN + "=?)");
            ps.setInt(1, eSightId);
            ps.setString(2, dn);
            ps.setString(3, dn);
            rs = ps.executeQuery();
            List<ESightHAServer> result = new LinkedList<>();
            while (rs.next()) {
                result.add(buildESightHAServer(rs));
            }
            return result;
        } catch (DataBaseException | SQLException e) {
            LOGGER.error("Failed to get eSight HA server by DN: " + e.getMessage());
            throw new SQLException(e);
        } finally {
            closeConnection(con, ps, rs);
        }
    }
}
