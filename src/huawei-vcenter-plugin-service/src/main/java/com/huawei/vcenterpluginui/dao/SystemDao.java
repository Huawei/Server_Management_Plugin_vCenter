package com.huawei.vcenterpluginui.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SystemDao extends H2DataBaseDao {

  /**
   * create table in h2
   */
  public void createTable(String sqlFile) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = getConnection();
      ps = con.prepareStatement(sqlFile);
      ps.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error("Failed to create table: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, ps, null);
    }
  }

  public boolean checkTable(String sqlFile) throws SQLException {
    Connection con = null;
    ResultSet ResultSet = null;
    boolean tableExist;
    try {
      con = getConnection();
      ResultSet = con.getMetaData().getTables(null, null, sqlFile, null);
      tableExist = ResultSet.next();
    } catch (SQLException e) {
      LOGGER.error("Failed to check table: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, null, ResultSet);
    }

    return tableExist;
  }

  /**
   * 判断表是否存在，不存在则创建表
   *
   * @param tableName 表名
   * @param createTableSQL 创建表的SQL
   */
  public void checkExistAndCreateTable(String tableName, String createTableSQL) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = getConnection();
      rs = con.getMetaData().getTables(null, null, tableName, null);
      if (!rs.next()) {
        ps = con.prepareStatement(createTableSQL);
        ps.executeUpdate();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to check exist and create table: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, ps, rs);
    }
  }

  /**
   * get h2 DB file path from URL
   */
  public static String getDBFileFromURL(String url) {
    return url.replaceAll("jdbc:h2:", "");
  }

  /**
   * load file content from resources/db folder
   */
  public static String getDBScript(String sqlFile) throws IOException {
    InputStream inputStream = null;
    try {
      inputStream = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("db/" + sqlFile);
      byte[] buff = new byte[inputStream.available()];
      if (inputStream.read(buff) != -1) {
        return new String(buff, "utf-8");
      }
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
    return null;
  }

  public void checkExistTableColumnAnd(String tableName, String columnName,
      String alterSql) throws Exception {
    Connection con = null;
    PreparedStatement ps1 = null;
    PreparedStatement ps2 = null;
    ResultSet rs = null;
    try {
      con = getConnection();
      String sql = "SELECT * FROM " + tableName + " LIMIT 1";
      ps1 = con.prepareStatement(sql);
      rs = ps1.executeQuery();
      ResultSetMetaData resultSetMetaData = rs.getMetaData();
      for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
        if (resultSetMetaData.getColumnName(i + 1).equals(columnName)) {
          return;
        }
      }
      ps2 = con.prepareStatement(alterSql);
      ps2.executeUpdate();

    } catch (Exception e) {
      LOGGER.error("Failed to check exist table column: " + e.getMessage());
      throw e;
    } finally {
      closeConnection(con, rs, ps1, ps2);
    }
  }

//  public void alterTableColumn(String tableName, String columnName, String alterSql) throws Exception {
//    Connection con = null;
//    PreparedStatement ps1 = null;
//    PreparedStatement ps2 = null;
//    ResultSet rs = null;
//    try {
//      con = getConnection();
//      String sql = "SELECT * FROM " + tableName + " LIMIT 1";
//      ps1 = con.prepareStatement(sql);
//      rs = ps1.executeQuery();
//      ResultSetMetaData resultSetMetaData = rs.getMetaData();
//      for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
//        if (resultSetMetaData.getColumnName(i + 1).equals(columnName)) {
//          ps2 = con.prepareStatement(alterSql);
//          ps2.executeUpdate();
//          break;
//        }
//      }
//    } catch (Exception e) {
//      LOGGER.error(e.getMessage());
//      throw e;
//    } finally {
//      closeConnection(con, rs, ps1, ps2);
//    }
//  }

  public boolean isColumnExists(String tableName, String columnName) throws SQLException {
    Connection con = null;
    PreparedStatement ps1 = null;
    ResultSet rs = null;
    try {
      con = getConnection();
      String sql = "SELECT * FROM " + tableName + " LIMIT 1";
      ps1 = con.prepareStatement(sql);
      rs = ps1.executeQuery();
      ResultSetMetaData resultSetMetaData = rs.getMetaData();
      for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
        if (resultSetMetaData.getColumnName(i + 1).equalsIgnoreCase(columnName)) {
          return true;
        }
      }
      return false;
    } finally {
      closeConnection(con, rs, ps1);
    }
  }

}
