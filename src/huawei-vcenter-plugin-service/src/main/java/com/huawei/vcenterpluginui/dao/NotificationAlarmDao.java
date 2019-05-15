package com.huawei.vcenterpluginui.dao;

import com.huawei.vcenterpluginui.constant.SqlFileConstant;
import com.huawei.vcenterpluginui.entity.AlarmRecord;
import com.huawei.vcenterpluginui.entity.HAComponent;
import com.huawei.vcenterpluginui.entity.Pair;
import com.huawei.vcenterpluginui.entity.ServerDeviceDetail;
import com.huawei.vcenterpluginui.exception.DataBaseException;
import com.huawei.vcenterpluginui.utils.CommonUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationAlarmDao extends H2DataBaseDao {

  private static final String TABLE_NAME = SqlFileConstant.HW_SERVER_DEVICE_DETAIL;

  public Collection<String> getNotPresentStateComponents() throws SQLException {
    String sql = String.format("SELECT DISTINCT component FROM %s t1 WHERE component NOT IN" +
            "(SELECT DISTINCT component FROM %s t2 WHERE t2.health_state <> '-1')", TABLE_NAME,
        TABLE_NAME);
    Collection<String> notPresentStateComponents = new HashSet<>();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      rs = ps.executeQuery();
      while (rs.next()) {
        notPresentStateComponents.add(rs.getString("component"));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      closeConnection(conn, ps, rs);
    }
    return notPresentStateComponents;
  }

  public int getServerDeviceDetailCount(int esightId, Collection<String> dns) throws SQLException {
    StringBuilder sql = new StringBuilder(
        "SELECT COUNT(*) FROM HW_SERVER_DEVICE_DETAIL WHERE ESIGHT_HOST_ID=? AND DN IN(");
    sql.append(getSQLIn(dns.size()));
    sql.append(")");

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql.toString());
      int idx = 1;
      ps.setInt(idx++, esightId);
      for (String dn : dns) {
        ps.setString(idx++, dn);
      }
      rs = ps.executeQuery();
      rs.next();
      return rs.getInt(1);
    } catch (SQLException e) {
      throw e;
    } finally {
      closeConnection(conn, ps, rs);
    }
  }

  public Pair<Collection<String>, List<ServerDeviceDetail>> getServerDeviceDetailDiff(
      List<ServerDeviceDetail> list)
      throws SQLException {
    Collection<String> turnGreenComponents = new HashSet<>();
    List<ServerDeviceDetail> differentDevices = new ArrayList<>();
    if (list == null) {
      return new Pair<>(turnGreenComponents, differentDevices);
    }

    // insert temp table
    String tmpTableName = getTempTableName();
    // green components
    String sqlTurnGreenComponents = "SELECT DISTINCT component FROM %s t1 " +
        "WHERE t1.health_state IN('0', '2', '3', '5') AND t1.component NOT IN " +
        "      (SELECT DISTINCT component FROM %s WHERE health_state NOT IN('0', '2', '3', '5', '-1', '-2'))"
        + "  AND t1.component IN " +
        "      (SELECT DISTINCT t2.component FROM HW_SERVER_DEVICE_DETAIL t2 WHERE t1.esight_host_id=t2.esight_host_id AND t1.dn=t2.dn AND t2.health_state NOT IN('0', '2', '3', '5', '-1')"
        +
        "      UNION SELECT DISTINCT component FROM HW_SERVER_DEVICE_DETAIL t3 WHERE t1.esight_host_id=t3.esight_host_id AND t1.dn=t3.dn AND t3.component NOT IN"
        +
        "      (SELECT component FROM HW_SERVER_DEVICE_DETAIL WHERE health_state <> '-1'))";
    // non-green components
    String sqlPattern = "SELECT t3.* " +
        "FROM (SELECT t1.* " +
        "      FROM %s t1 " +
        "      WHERE NOT EXISTS( " +
        "          SELECT 1 " +
        "          FROM HW_SERVER_DEVICE_DETAIL t2 " +
        "          WHERE " +
        "            t2.ESIGHT_HOST_ID = t1.ESIGHT_HOST_ID " +
        "            AND t2.DN = t1.DN " +
        "            AND t2.COMPONENT = t1.COMPONENT " +
        "            AND t2.UUID = t1.UUID " +
        "            AND t2.HEALTH_STATE = t1.HEALTH_STATE " +
        "            AND t2.PRESENT_STATE = t1.PRESENT_STATE)) t3 " +
        "WHERE t3.HEALTH_STATE NOT IN ('0', '2', '3', '5', '-1', '-2')";
    String sql1 = String.format(
        "CREATE TABLE %s AS SELECT ESIGHT_HOST_ID,DN,COMPONENT,UUID,HEALTH_STATE,PRESENT_STATE,UPDATE_TIME FROM %s WHERE 1=0",
        tmpTableName, TABLE_NAME);
    String sql2 = String.format(sqlTurnGreenComponents, tmpTableName, tmpTableName);
    String sql3 = String.format(sqlPattern, tmpTableName);
    String sql4 = "DROP TABLE " + tmpTableName;
    Connection conn = null;
    PreparedStatement ps1 = null;
    PreparedStatement ps2 = null;
    PreparedStatement ps3 = null;
    PreparedStatement ps4 = null;
    ResultSet rs = null;
    try {
      // create temp table
      conn = getConnection();
      conn.setAutoCommit(false);
      ps1 = conn.prepareStatement(sql1);
      ps1.execute();

      // add data into temp table
      addServerDeivceDetails(conn, tmpTableName, list);

      // get green components
      ps4 = conn.prepareStatement(sql2);
      rs = ps4.executeQuery();
      while (rs.next()) {
        turnGreenComponents.add(rs.getString("component"));
      }
      rs.close();

      // compare and get differences
      ps2 = conn.prepareStatement(sql3);
      rs = ps2.executeQuery();
      while (rs.next()) {
        differentDevices.add(new ServerDeviceDetail(
            rs.getInt("ESIGHT_HOST_ID"),
            rs.getString("DN"),
            rs.getString("COMPONENT"),
            rs.getString("UUID"),
            rs.getString("HEALTH_STATE"),
            rs.getString("PRESENT_STATE")
        ));
      }

      // drop temp table
      ps3 = conn.prepareStatement(sql4);
      ps3.execute();
      conn.commit();
    } catch (SQLException e) {
      LOGGER.error("Failed to get server device detail differences: " + e.getMessage());
      if (conn != null) {
        conn.rollback();
      }
      throw e;
    } finally {
      closeConnection(conn, rs, ps1, ps2, ps3, ps4);
    }
    return new Pair<>(turnGreenComponents, differentDevices);
  }

  private void addServerDeivceDetails(Connection conn, List<ServerDeviceDetail> list)
      throws SQLException {
    addServerDeivceDetails(conn, TABLE_NAME, list);
  }

  private void addServerDeivceDetails(Connection conn, String table, List<ServerDeviceDetail> list)
      throws SQLException {
    if (list == null || list.isEmpty()) {
      return;
    }
    String sql = "INSERT INTO " + table
        + "(ESIGHT_HOST_ID, DN, COMPONENT, UUID, HEALTH_STATE, PRESENT_STATE, UPDATE_TIME) VALUES" +
        "(?,?,?,?,?,?,CURRENT_TIMESTAMP)";
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql);
      for (ServerDeviceDetail detail : list) {
        ps.setInt(1, detail.getEsightId());
        ps.setString(2, detail.getDn());
        ps.setString(3, detail.getComponent());
        ps.setString(4, detail.getUuid());
        ps.setString(5, detail.getHealthState());
        ps.setString(6, detail.getPresentStatus());
        ps.addBatch();
      }
      ps.executeBatch();
    } finally {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException e) {
          LOGGER.error("Failed to close resources " + e.getMessage());
        }
      }
    }
  }

  public void updateDeviceDetail(int esightId, Collection<String> dnList,
      List<ServerDeviceDetail> list) throws SQLException {
    StringBuilder sql = new StringBuilder(
        "DELETE FROM HW_SERVER_DEVICE_DETAIL WHERE ESIGHT_HOST_ID=? AND DN IN (");
    sql.append(getSQLIn(dnList.size())).append(")");

    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      int index = 1;
      ps = conn.prepareStatement(sql.toString());
      ps.setInt(index++, esightId);
      for (String dn : dnList) {
        ps.setString(index++, dn);
      }
      ps.executeUpdate();

      addServerDeivceDetails(conn, list);

      conn.commit();
    } catch (SQLException e) {
      LOGGER.error("Failed to update device detail: " + e.getMessage());
      if (conn != null) {
        conn.rollback();
      }
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  /**
   * update specified device/component details
   */
  public void updateDeviceDetail(int esightId,
      Collection<String> dnList,
      List<ServerDeviceDetail> list,
      Collection<String> components,
      Collection<String> notReadyUUIDs) throws SQLException {
    if (components == null || components.isEmpty()) {
      return;
    }
    StringBuilder sql = new StringBuilder(
        "DELETE FROM HW_SERVER_DEVICE_DETAIL WHERE ESIGHT_HOST_ID=? AND DN IN (");
    sql.append(getSQLIn(dnList.size()));
    sql.append(") AND COMPONENT IN(");
    sql.append(getSQLIn(components.size()));
    sql.append(")");
    // don't update not ready uuid
    if (!notReadyUUIDs.isEmpty()) {
      sql.append(" AND UUID NOT IN(");
      sql.append(getSQLIn(notReadyUUIDs.size()));
      sql.append(")");
    }

    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      int index = 1;
      ps = conn.prepareStatement(sql.toString());
      ps.setInt(index++, esightId);
      for (String dn : dnList) {
        ps.setString(index++, dn);
      }
      for (String component : components) {
        ps.setString(index++, component);
      }
      if (!notReadyUUIDs.isEmpty()) {
        for (String uuid : notReadyUUIDs) {
          ps.setString(index++, uuid);
        }
      }
      LOGGER.info(
          "Parameters: " + esightId + ", " + dnList + ", " + components + ", " + notReadyUUIDs);
      ps.executeUpdate();

      // DO NOT update server devices which are not ready
      if (!notReadyUUIDs.isEmpty()) {
        List<ServerDeviceDetail> newList = new ArrayList<>();
        for (ServerDeviceDetail serverDeviceDetail : list) {
          if (!notReadyUUIDs.contains(serverDeviceDetail.getUuid())) {
            newList.add(serverDeviceDetail);
          }
        }
        addServerDeivceDetails(conn, newList);
      } else {
        addServerDeivceDetails(conn, list);
      }

      conn.commit();
    } catch (SQLException e) {
      LOGGER.error("Failed to update device detail: " + e.getMessage());
      if (conn != null) {
        conn.rollback();
      }
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public void updateDeviceDetail(int esightId, String dn, List<ServerDeviceDetail> list)
      throws SQLException {
    String sql = "DELETE FROM HW_SERVER_DEVICE_DETAIL WHERE ESIGHT_HOST_ID=? AND DN=?";
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      ps = conn.prepareStatement(sql);
      ps.setInt(1, esightId);
      ps.setString(2, dn);
      ps.executeUpdate();

      addServerDeivceDetails(conn, list);

      conn.commit();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      if (conn != null) {
        conn.rollback();
      }
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public Set<String> getESightHostIdAndDNs() throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = getConnection();
      ps = conn.prepareStatement("SELECT DISTINCT ESIGHT_HOST_ID,DN FROM " + TABLE_NAME);
      rs = ps.executeQuery();

      Set<String> result = new HashSet<>();
      while (rs.next()) {
        int esightHostId = rs.getInt("ESIGHT_HOST_ID");
        String dn = rs.getString("DN");
        result.add(CommonUtils.concatESightHostIdAndDN(esightHostId, dn));
      }
      return result;
    } catch (DataBaseException | SQLException e) {
      LOGGER.error("Failed to get eSight host id and dns: " + e.getMessage());
      throw new SQLException(e);
    } finally {
      closeConnection(conn, ps, rs);
    }
  }

  public int deleteNotSyncedDeviceDetails() throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    String sql = "DELETE FROM HW_SERVER_DEVICE_DETAIL t1 " +
        "WHERE NOT EXISTS ( " +
        "    SELECT 1 FROM HW_ESIGHT_HA_SERVER t2 " +
        "    WHERE t1.ESIGHT_HOST_ID=t2.ESIGHT_HOST_ID " +
        "    AND (t1.DN=t2.ESIGHT_SERVER_DN OR t1.DN=t2.ESIGHT_SERVER_PARENT_DN) " +
        "    AND t2.STATUS=1)";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to delete not synced device details: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public AlarmRecord getAlarmRecord(int esightHostId, int sn, String dn) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = "SELECT * FROM HW_ALARM_RECORD WHERE ESIGHT_HOST_ID=? AND SN=? AND DN=?";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, esightHostId);
      ps.setInt(2, sn);
      ps.setString(3, dn);
      rs = ps.executeQuery();
      if (rs.next()) {
        return new AlarmRecord(rs.getInt("ID"), rs.getInt("ESIGHT_HOST_ID"),
            rs.getString("EVENT_ID"), rs.getString("DN"), rs.getInt("SN"),
            rs.getTimestamp("CREATE_TIME"));
      }
      return null;
    } catch (SQLException e) {
      LOGGER.error("Failed to get alarm records: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, rs);
    }
  }

  public int addAlarmRecord(AlarmRecord alarmRecord) throws SQLException {
    if (alarmRecord == null) {
      return 0;
    }
    Connection conn = null;
    PreparedStatement ps = null;
    String sql = "INSERT INTO HW_ALARM_RECORD(ESIGHT_HOST_ID,EVENT_ID,DN,SN,CREATE_TIME) VALUES(?,?,?,?,CURRENT_TIMESTAMP)";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, alarmRecord.getEsightHostId());
      ps.setString(2, alarmRecord.getEventId());
      ps.setString(3, alarmRecord.getDn());
      ps.setInt(4, alarmRecord.getSn());
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to add alarm record: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public int deleteAlarmRecord(int esightHostId, String dn) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    String sql = "DELETE FROM HW_ALARM_RECORD WHERE ESIGHT_HOST_ID=? AND DN=?";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, esightHostId);
      ps.setString(2, dn);
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to delete alarm record: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public int deleteAlarmRecord(AlarmRecord alarmRecord) throws SQLException {
    if (alarmRecord == null) {
      return 0;
    }
    Connection conn = null;
    PreparedStatement ps = null;
    String sql = "DELETE FROM HW_ALARM_RECORD WHERE ESIGHT_HOST_ID=? AND DN=? AND SN=?";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, alarmRecord.getEsightHostId());
      ps.setString(2, alarmRecord.getDn());
      ps.setInt(3, alarmRecord.getSn());
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to delete alarm record: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public int getAlarmRecordEventIdCount(AlarmRecord alarmRecord) throws SQLException {
    if (alarmRecord == null) {
      return 0;
    }
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = "SELECT COUNT(ID) FROM HW_ALARM_RECORD WHERE ESIGHT_HOST_ID=? AND EVENT_ID=? AND DN=?";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, alarmRecord.getEsightHostId());
      ps.setString(2, alarmRecord.getEventId());
      ps.setString(3, alarmRecord.getDn());
      rs = ps.executeQuery();
      rs.next();
      return rs.getInt(1);
    } catch (SQLException e) {
      LOGGER.error("Failed to get alarm record event id count: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, rs);
    }
  }

  public List<HAComponent> getHAComponents(HAComponent haComponent) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = "SELECT * FROM HW_HA_COMPONENT WHERE ESIGHT_HOST_ID=? AND SN=? AND DN=?";
    List<HAComponent> haComponents = new ArrayList<>();
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, haComponent.getEsightHostId());
      ps.setInt(2, haComponent.getSn());
      ps.setString(3, haComponent.getDn());
      rs = ps.executeQuery();
      while (rs.next()) {
        haComponents.add(
            new HAComponent(rs.getInt("ID"), rs.getInt("ESIGHT_HOST_ID"), rs.getString("COMPONENT"),
                rs.getInt("SN"), rs.getString("COMPONENT"), rs.getTimestamp("CREATE_TIME")));
      }
      return haComponents;
    } catch (SQLException e) {
      LOGGER.error("Failed to get HA components: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, rs);
    }
  }

  public int getComponentTypeCount(HAComponent haComponent) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = "SELECT COUNT(ID) FROM HW_HA_COMPONENT WHERE ESIGHT_HOST_ID=? AND DN=? AND COMPONENT=?";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, haComponent.getEsightHostId());
      ps.setString(2, haComponent.getDn());
      ps.setString(3, haComponent.getComponent());
      rs = ps.executeQuery();
      rs.next();
      return rs.getInt(1);
    } catch (SQLException e) {
      LOGGER.error("Failed to get component type count: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, rs);
    }
  }

  public int addHAComponents(HAComponent haComponent) throws SQLException {
    if (haComponent == null) {
      return 0;
    }
    Connection conn = null;
    PreparedStatement ps = null;
    String sql =
        "INSERT INTO HW_HA_COMPONENT(ESIGHT_HOST_ID,COMPONENT,SN,DN,CREATE_TIME) "
            + "VALUES(?,?,?,?,CURRENT_TIMESTAMP)";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, haComponent.getEsightHostId());
      ps.setString(2, haComponent.getComponent());
      ps.setInt(3, haComponent.getSn());
      ps.setString(4, haComponent.getDn());
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to add HA component: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public int deleteHAComponent(int esightHostId, String dn) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    String sql = "DELETE FROM HW_HA_COMPONENT WHERE ESIGHT_HOST_ID=? AND DN=?";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, esightHostId);
      ps.setString(2, dn);
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to delete HA components: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public int deleteHAComponent(HAComponent haComponent) throws SQLException {
    if (haComponent == null) {
      return 0;
    }
    Connection conn = null;
    PreparedStatement ps = null;
    String sql = "DELETE FROM HW_HA_COMPONENT WHERE ESIGHT_HOST_ID=? AND SN=? AND DN=?";
    try {
      conn = getConnection();
      ps = conn.prepareStatement(sql);
      ps.setInt(1, haComponent.getEsightHostId());
      ps.setInt(2, haComponent.getSn());
      ps.setString(3, haComponent.getDn());
      return ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to delete HA component: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(conn, ps, null);
    }
  }

  public void cleanAllData() {
    Connection con = null;
    PreparedStatement ps1 = null;
    try {
      con = getConnection();
      for (String table : SqlFileConstant.ALL_TABLES) {
        try {
          ps1 = con.prepareStatement("DELETE FROM " + table);
          ps1.execute();
          ps1.close();
          ps1 = null;
        } catch (SQLException e) {
          LOGGER.error("Cannot delete data from " + table);
        }
      }
    } finally {
      closeConnection(con, ps1, null);
    }
  }
}
