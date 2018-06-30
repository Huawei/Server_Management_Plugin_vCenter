package com.huawei.vcenterpluginui.dao;

import com.huawei.vcenterpluginui.constant.SqlFileConstant;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.DataBaseException;

import java.sql.*;

/**
 * Created by Rays on 2018/4/9.
 */
public class VCenterInfoDao extends H2DataBaseDao {

    public int addVCenterInfo(VCenterInfo vCenterInfo) throws SQLException {
        checkVCenterInfo(vCenterInfo);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("INSERT INTO " + SqlFileConstant.HW_VCENTER_INFO + " (HOST_IP,USER_NAME," +
                    "PASSWORD,STATE,CREATE_TIME) VALUES (?,?,?,?,CURRENT_TIMESTAMP)");
            ps.setString(1, vCenterInfo.getHostIp());
            ps.setString(2, vCenterInfo.getUserName());
            ps.setString(3, vCenterInfo.getPassword());
            ps.setBoolean(4, vCenterInfo.isState());
            int row = ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                vCenterInfo.setId(rs.getInt(1));
            }
            return row;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            closeConnection(con, ps, rs);
        }
    }

    public int updateVCenterInfo(VCenterInfo vCenterInfo) throws SQLException {
        checkVCenterInfo(vCenterInfo);
        checkID(vCenterInfo.getId());
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("UPDATE " + SqlFileConstant.HW_VCENTER_INFO + " SET HOST_IP=?,USER_NAME=?,PASSWORD=?,STATE=? WHERE ID=?");
            ps.setString(1, vCenterInfo.getHostIp());
            ps.setString(2, vCenterInfo.getUserName());
            ps.setString(3, vCenterInfo.getPassword());
            ps.setBoolean(4, vCenterInfo.isState());
            ps.setInt(5, vCenterInfo.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            closeConnection(con, ps, rs);
        }
    }

    public VCenterInfo getVCenterInfo() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_VCENTER_INFO + " ORDER BY CREATE_TIME DESC LIMIT 1");
            rs = ps.executeQuery();
            if (rs.next()) {
                VCenterInfo vCenterInfo = new VCenterInfo();
                vCenterInfo.setId(rs.getInt("ID"));
                vCenterInfo.setHostIp(rs.getString("HOST_IP"));
                vCenterInfo.setUserName(rs.getString("USER_NAME"));
                vCenterInfo.setPassword(rs.getString("PASSWORD"));
                vCenterInfo.setCreateTime(rs.getTimestamp("CREATE_TIME"));
                vCenterInfo.setState(rs.getBoolean("STATE"));
                return vCenterInfo;
            }
        } catch (DataBaseException | SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new SQLException(e);
        } finally {
            closeConnection(con, ps, rs);
        }
        return null;
    }

    public boolean disableVCenterInfo() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("UPDATE " + SqlFileConstant.HW_VCENTER_INFO + " SET state=FALSE ");
            return ps.executeUpdate() > 0;
        } catch (DataBaseException | SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new SQLException(e);
        } finally {
            closeConnection(con, ps, null);
        }
    }

    public void deleteHAData() throws SQLException {
        Connection con = null;
        Statement ps = null;
        String sql1 = "DELETE FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER;
        String sql2 = "DELETE FROM " + SqlFileConstant.HW_SERVER_DEVICE_DETAIL;
        String sql3 = "DELETE FROM " + SqlFileConstant.HW_VCENTER_INFO;
        try {
            con = getConnection();
            ps = con.createStatement();
            ps.addBatch(sql1);
            ps.addBatch(sql2);
            ps.addBatch(sql3);
            ps.executeBatch();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            closeConnection(con, ps, null);
        }
    }

    public void deleteHASyncAndDeviceData() {
        Connection con = null;
        Statement ps = null;
        String sql1 = "DELETE FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER;
        String sql2 = "DELETE FROM " + SqlFileConstant.HW_SERVER_DEVICE_DETAIL;
        try {
            con = getConnection();
            ps = con.createStatement();
            ps.addBatch(sql1);
            ps.addBatch(sql2);
            ps.executeBatch();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeConnection(con, ps, null);
        }
    }

    private void checkIp(String ip) throws SQLException {
        if (ip == null || ip.length() > 255) {
            throw new SQLException("parameter ip is not correct");
        }
    }

    private void checkUserName(String userName) throws SQLException {
        if (userName == null || userName.length() > 255) {
            throw new SQLException("parameter userName is not correct");
        }
    }

    private void checkPassword(String password) throws SQLException {
        if (password == null || password.length() > 255) {
            throw new SQLException("parameter password is not correct");
        }
    }

    private void checkID(int id) throws SQLException {
        if(id < 1){
            throw new SQLException("parameter is is not correct");
        }

    }

    private void checkVCenterInfo(VCenterInfo vCenterInfo) throws SQLException {
        checkIp(vCenterInfo.getHostIp());
        checkUserName(vCenterInfo.getUserName());
        checkPassword(vCenterInfo.getPassword());
    }
}
