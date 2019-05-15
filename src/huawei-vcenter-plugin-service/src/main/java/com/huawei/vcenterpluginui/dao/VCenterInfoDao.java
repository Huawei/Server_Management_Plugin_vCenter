package com.huawei.vcenterpluginui.dao;

import com.huawei.vcenterpluginui.constant.SqlFileConstant;
import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import com.huawei.vcenterpluginui.entity.Pair;
import com.huawei.vcenterpluginui.entity.VCenterInfo;
import com.huawei.vcenterpluginui.exception.DataBaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
      ps = con.prepareStatement(
          "INSERT INTO " + SqlFileConstant.HW_VCENTER_INFO + " (HOST_IP,USER_NAME," +
              "PASSWORD,STATE,CREATE_TIME,PUSH_EVENT,PUSH_EVENT_LEVEL,HOST_PORT) VALUES (?,?,?,?,CURRENT_TIMESTAMP,?,?,?)");
      ps.setString(1, vCenterInfo.getHostIp());
      ps.setString(2, vCenterInfo.getUserName());
      ps.setString(3, vCenterInfo.getPassword());
      ps.setBoolean(4, vCenterInfo.isState());
      ps.setBoolean(5, vCenterInfo.isPushEvent());
      ps.setInt(6, vCenterInfo.getPushEventLevel());
      ps.setInt(7, vCenterInfo.getHostPort());
      int row = ps.executeUpdate();
      rs = ps.getGeneratedKeys();
      if (rs.next()) {
        vCenterInfo.setId(rs.getInt(1));
      }
      return row;
    } catch (SQLException e) {
      LOGGER.error("Failed to add vCenter info: " + e.getMessage());
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
      ps = con.prepareStatement("UPDATE " + SqlFileConstant.HW_VCENTER_INFO
          + " SET HOST_IP=?,USER_NAME=?,PASSWORD=?,STATE=?,PUSH_EVENT=?,PUSH_EVENT_LEVEL=?,HOST_PORT=? WHERE ID=?");
      ps.setString(1, vCenterInfo.getHostIp());
      ps.setString(2, vCenterInfo.getUserName());
      ps.setString(3, vCenterInfo.getPassword());
      ps.setBoolean(4, vCenterInfo.isState());
      ps.setBoolean(5, vCenterInfo.isPushEvent());
      ps.setInt(6, vCenterInfo.getPushEventLevel());
      ps.setInt(7, vCenterInfo.getHostPort());
      ps.setInt(8, vCenterInfo.getId());
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to update vCenter info: " + e.getMessage());
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
      ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_VCENTER_INFO
          + " ORDER BY CREATE_TIME DESC LIMIT 1");
      rs = ps.executeQuery();
      if (rs.next()) {
        VCenterInfo vCenterInfo = new VCenterInfo();
        vCenterInfo.setId(rs.getInt("ID"));
        vCenterInfo.setHostIp(rs.getString("HOST_IP"));
        vCenterInfo.setHostPort(rs.getInt("HOST_PORT"));
        vCenterInfo.setUserName(rs.getString("USER_NAME"));
        vCenterInfo.setPassword(rs.getString("PASSWORD"));
        vCenterInfo.setCreateTime(rs.getTimestamp("CREATE_TIME"));
        vCenterInfo.setState(rs.getBoolean("STATE"));
        vCenterInfo.setPushEvent(rs.getBoolean("PUSH_EVENT"));
        vCenterInfo.setPushEventLevel(rs.getInt("PUSH_EVENT_LEVEL"));
        return vCenterInfo;
      }
    } catch (DataBaseException | SQLException e) {
      LOGGER.error("Failed to get vCenter info: " + e.getMessage());
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
      LOGGER.error("Failed to disable vCenter info: " + e.getMessage());
      throw new SQLException(e);
    } finally {
      closeConnection(con, ps, null);
    }
  }

  public boolean disablePushEvent() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = getConnection();
      ps = con
          .prepareStatement("UPDATE " + SqlFileConstant.HW_VCENTER_INFO + " SET PUSH_EVENT=FALSE ");
      return ps.executeUpdate() > 0;
    } catch (DataBaseException | SQLException e) {
      LOGGER.error("Failed to disable push event: " + e.getMessage());
      throw new SQLException(e);
    } finally {
      closeConnection(con, ps, null);
    }
  }

  public void deleteAlarmDefinitions() throws SQLException {
    Connection con = null;
    Statement ps = null;
    String sql = "DELETE FROM " + SqlFileConstant.HW_ALARM_DEFINITION;
    try {
      con = getConnection();
      ps = con.createStatement();
      ps.execute(sql);
    } catch (SQLException e) {
      LOGGER.error("Failed to delete alarm definitions: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, ps, null);
    }
  }

  public void deleteAlarmDefinitions(List<Integer> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    Connection con = null;
    Statement ps = null;
    String sql =
        "DELETE FROM " + SqlFileConstant.HW_ALARM_DEFINITION + " WHERE ID IN( "
            + concatInValues(ids) + ")";
    try {
      con = getConnection();
      ps = con.createStatement();
      ps.execute(sql);
    } catch (SQLException e) {
      LOGGER.error("Failed to delete alarm definitions: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, ps, null);
    }
  }

  public List<AlarmDefinition> getAlarmDefinitions() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = getConnection();
      ps = con.prepareStatement("SELECT * FROM " + SqlFileConstant.HW_ALARM_DEFINITION);
      rs = ps.executeQuery();
      List<AlarmDefinition> alarmDefinitionList = new ArrayList<>();
      while (rs.next()) {
        alarmDefinitionList.add(buildAlarmDefinition(rs));
      }
      return alarmDefinitionList;
    } catch (SQLException e) {
      LOGGER.error("Failed to get alarm definitions: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, ps, rs);
    }
  }

  public void addAlarmDefinitionss(List<AlarmDefinition> list) throws SQLException {
    if (list == null || list.isEmpty()) {
      return;
    }
    String sql = "INSERT INTO " + SqlFileConstant.HW_ALARM_DEFINITION
        + "(MOR_VALUE, EVENT_TYPE_ID, EVENT_NAME, SEVERITY, EVENT_TYPE, DESCRIPTION) VALUES" +
        "(?,?,?,?,?,?)";
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = getConnection();
      ps = con.prepareStatement(sql);
      for (AlarmDefinition ad : list) {
        ps.setString(1, ad.getMorValue());
        ps.setString(2, ad.getEventTypeID());
        ps.setString(3, ad.getEventName());
        ps.setString(4, ad.getSeverity());
        ps.setString(5, ad.getEventType());
        ps.setString(6, ad.getDescription());
        ps.addBatch();
      }
      ps.executeBatch();
    } finally {
      closeConnection(con, ps, null);
    }
  }

  private static final String TABLE_NAME = SqlFileConstant.HW_ALARM_DEFINITION;

  public Pair<List<AlarmDefinition>, List<AlarmDefinition>> getAlarmDefinitionDiff(
      List<AlarmDefinition> alarmDefinitions) {
    List<AlarmDefinition> staleAlarmDefinitions = new ArrayList<>();
    List<AlarmDefinition> newAlarmDefinitions = new ArrayList<>();

    // create temp table
    String tmpTableName = getTempTableName();
    String sql1 =
        "CREATE TABLE " + tmpTableName + " AS SELECT * FROM " + TABLE_NAME + " WHERE 1=0";

    // insert temp table
    String sql2 = "INSERT INTO " + tmpTableName
        + "(EVENT_TYPE_ID,EVENT_NAME,SEVERITY,EVENT_TYPE,DESCRIPTION) VALUES(?,?,?,?,?)";

    // stale data
    String sql3 = "SELECT * FROM " + TABLE_NAME + " old WHERE NOT EXISTS (SELECT 1 FROM "
        + tmpTableName
        + " new WHERE old.EVENT_TYPE_ID=new.EVENT_TYPE_ID "
        + "AND old.EVENT_NAME=new.EVENT_NAME "
        + "AND old.SEVERITY=new.SEVERITY "
        + "AND old.EVENT_TYPE=new.EVENT_TYPE "
        + "AND old.DESCRIPTION=new.DESCRIPTION)";

    // new data
    String sql4 = "SELECT * FROM " + tmpTableName + " new WHERE NOT EXISTS (SELECT 1 FROM "
        + TABLE_NAME
        + " old WHERE old.EVENT_TYPE_ID=new.EVENT_TYPE_ID "
        + "AND old.EVENT_NAME=new.EVENT_NAME "
        + "AND old.SEVERITY=new.SEVERITY "
        + "AND old.EVENT_TYPE=new.EVENT_TYPE "
        + "AND old.DESCRIPTION=new.DESCRIPTION)";

    // drop temp table
    String sql5 = "DROP TABLE " + tmpTableName;

    Connection conn = null;
    PreparedStatement ps1 = null;
    PreparedStatement ps2 = null;
    PreparedStatement ps3 = null;
    PreparedStatement ps4 = null;
    PreparedStatement ps5 = null;
    ResultSet rs = null;
    try {
      conn = getConnection();
      ps1 = conn.prepareStatement(sql1);
      ps1.execute();

      if (alarmDefinitions != null && !alarmDefinitions.isEmpty()) {
        ps2 = conn.prepareStatement(sql2);
        for (AlarmDefinition alarmDefinition : alarmDefinitions) {
          ps2.setString(1, alarmDefinition.getEventTypeID());
          ps2.setString(2, alarmDefinition.getEventName());
          ps2.setString(3, alarmDefinition.getSeverity());
          ps2.setString(4, alarmDefinition.getEventType());
          ps2.setString(5, alarmDefinition.getDescription());
          ps2.addBatch();
        }
        ps2.executeBatch();
      }

      ps3 = conn.prepareStatement(sql3);
      rs = ps3.executeQuery();
      while (rs.next()) {
        AlarmDefinition ad = new AlarmDefinition();
        staleAlarmDefinitions.add(buildAlarmDefinition(rs));
      }
      rs.close();

      ps4 = conn.prepareStatement(sql4);
      rs = ps4.executeQuery();
      while (rs.next()) {
        newAlarmDefinitions.add(buildAlarmDefinition(rs));
      }

      ps5 = conn.prepareStatement(sql5);
      ps5.execute();
    } catch (Exception e) {
      LOGGER.error("Failed to getAlarmDefinitionDiff: " + e.getMessage());
      return null;
    } finally {
      closeConnection(conn, rs, ps1, ps2, ps3, ps4, ps5);
    }

    return new Pair<>(staleAlarmDefinitions, newAlarmDefinitions);
  }

  private AlarmDefinition buildAlarmDefinition(ResultSet rs) throws SQLException {
    AlarmDefinition ad = new AlarmDefinition();
    ad.setId(rs.getInt("ID"));
    ad.setMorValue(rs.getString("MOR_VALUE"));
    ad.setEventTypeID(rs.getString("EVENT_TYPE_ID"));
    ad.setEventName(rs.getString("EVENT_NAME"));
    ad.setSeverity(rs.getString("SEVERITY"));
    ad.setEventType(rs.getString("EVENT_TYPE"));
    ad.setDescription(rs.getString("DESCRIPTION"));
    return ad;
  }

  public void deleteHAData() throws SQLException {
    Connection con = null;
    Statement ps = null;
    String sql1 = "DELETE FROM " + SqlFileConstant.HW_ESIGHT_HA_SERVER;
    String sql2 = "DELETE FROM " + SqlFileConstant.HW_SERVER_DEVICE_DETAIL;
    String sql3 = "DELETE FROM " + SqlFileConstant.HW_VCENTER_INFO;
    String sql4 = "DELETE FROM " + SqlFileConstant.HW_HA_COMPONENT;
    String sql5 = "DELETE FROM " + SqlFileConstant.HW_ALARM_RECORD;
    try {
      con = getConnection();
      ps = con.createStatement();
      ps.addBatch(sql1);
      ps.addBatch(sql2);
      ps.addBatch(sql3);
      ps.addBatch(sql4);
      ps.addBatch(sql5);
      ps.executeBatch();
    } catch (SQLException e) {
      LOGGER.error("Failed to delete HA data: " + e.getMessage());
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
    String sql3 = "DELETE FROM " + SqlFileConstant.HW_HA_COMPONENT;
    String sql4 = "DELETE FROM " + SqlFileConstant.HW_ALARM_RECORD;
    try {
      con = getConnection();
      ps = con.createStatement();
      ps.addBatch(sql1);
      ps.addBatch(sql2);
      ps.addBatch(sql3);
      ps.addBatch(sql4);
      ps.executeBatch();
    } catch (SQLException e) {
      LOGGER.error("Failed to delete HA sync and device data: " + e.getMessage());
    } finally {
      closeConnection(con, ps, null);
    }
  }

  public String[] mergeSaveAndLoadAllThumbprints(String[] thumbprints) throws SQLException {
    if (thumbprints != null && thumbprints.length > 0) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String sql1 = "DELETE FROM HW_ESIGHT_THUMBPRINT";
      String sql2 = "INSERT INTO HW_ESIGHT_THUMBPRINT(THUMBPRINT) VALUES(?)";
      Set<String> thumbprintSet = new HashSet<>();
      try {
        con = getConnection();
        con.setAutoCommit(false);
        thumbprintSet.addAll(Arrays.asList(loadThumbprints()));

        // sql1
        ps = con.prepareStatement(sql1);
        ps.executeUpdate();
        ps.close(); // manually close

        // sql2
        thumbprintSet.addAll(Arrays.asList(thumbprints));
        ps = con.prepareStatement(sql2);
        for (String thumbprint : thumbprintSet) {
          ps.setString(1, thumbprint);
          ps.addBatch();
        }
        ps.executeBatch(); // close in finally

        con.commit();
        return thumbprintSet.toArray(new String[thumbprintSet.size()]);
      } catch (SQLException e) {
        if(con != null) {
          con.rollback();
        }
        // LOGGER.error(e.getMessage(), e);
        LOGGER.error("Failed to mergeSaveAndLoadAllThumbprints" + e.getMessage());
        throw e;
      } finally {
        closeConnection(con, ps, rs);
      }
    }
    return new String[0];
  }

  public String[] loadThumbprints() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = "SELECT THUMBPRINT FROM HW_ESIGHT_THUMBPRINT";
    Set<String> thumbprintSet = new HashSet<>();
    try {
      con = getConnection();
      ps = con.prepareStatement(sql);
      rs = ps.executeQuery();
      while (rs.next()) {
        thumbprintSet.add(rs.getString("THUMBPRINT"));
      }
      return thumbprintSet.toArray(new String[thumbprintSet.size()]);
    } catch (SQLException e) {
      if(con != null) {
        con.rollback();
      }
      LOGGER.error("Failed to load thumbprints: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, ps, rs);
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
    if (id < 1) {
      throw new SQLException("parameter is is not correct");
    }

  }

  private void checkVCenterInfo(VCenterInfo vCenterInfo) throws SQLException {
    checkIp(vCenterInfo.getHostIp());
    checkUserName(vCenterInfo.getUserName());
    checkPassword(vCenterInfo.getPassword());
  }
}
