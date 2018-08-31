package com.huawei.vcenterpluginui.dao;

import com.huawei.vcenterpluginui.exception.DataBaseException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class H2DataBaseDao {

  private String url;

  public final static Log LOGGER = LogFactory.getLog(H2DataBaseDao.class);

  private static final String VMWARE_RUNTIME_DATA_DIR = "VMWARE_RUNTIME_DATA_DIR";

  private static final String VMWARE_LINUX_DIR = "/home/vsphere-client";

  private static final String OS = System.getProperty("os.name").toLowerCase(Locale.US);

  private static final String URL_PREFIX = "jdbc:h2:";

  private static final String DB_FILE = "huawei-vcenter-plugin-data";

  private static final String USER = "sa";

  private static final String KEY = "";

  private static String getVmwareRuntimeDataDir() {
    return System.getenv(VMWARE_RUNTIME_DATA_DIR);
  }

  protected String concatInValues(Collection<Integer> values) {
    if (values == null || values.isEmpty()) {
      return "";
    }
    StringBuilder buff = new StringBuilder();
    for (Integer value : values) {
      if (buff.length() > 0) {
        buff.append(",");
      }
      buff.append(value);
    }
    return buff.toString();
  }

  protected String getSQLIn(int len) {
    StringBuilder builder = new StringBuilder();
    while (len-- > 0) {
      if (builder.length() != 0) {
        builder.append(",");
      }
      builder.append("?");
    }
    return builder.toString();
  }

  protected String getTempTableName() {
    return String.format("HW_%s", UUID.randomUUID().toString().replaceAll("-", "_"));
  }

  private static Boolean isWindows() {
    return OS.indexOf("windows") >= 0;
  }

  public Connection getConnection() throws DataBaseException {
    Connection con;

    try {
      Class.forName("org.h2.Driver");
      con = DriverManager.getConnection(url, USER, KEY);
    } catch (Exception e) {
      throw new DataBaseException(e.getMessage());
    }

    return con;
  }

  public void closeConnection(Connection con, ResultSet rs, PreparedStatement... ps) {
    if (rs != null) {
      try {
        rs.close();
      } catch (Exception var2) {
        var2.printStackTrace();
      }
    }
    if (ps != null) {
      for (PreparedStatement ps1 : ps) {
        try {
          if (ps1 != null) {
            ps1.close();
          }
        } catch (Exception var2) {
          var2.printStackTrace();
        }
      }
    }
    if (con != null) {
      try {
        con.close();
      } catch (Exception var2) {
        var2.printStackTrace();
      }
    }
  }

  public void closeConnection(Connection con, Statement ps, ResultSet rs) {
    close(rs);
    close(ps);
    close(con);
  }

  public void close(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        LOGGER.error(e);
      }
    }
  }

  public void setUrl(String url) {
    String vmwareDataPath = getVmwareRuntimeDataDir();
    if (vmwareDataPath != null && !"".equals(vmwareDataPath)) {
      if (isWindows()) {
        this.url = URL_PREFIX + "//" + vmwareDataPath + File.separator + DB_FILE;
      } else {
        this.url = URL_PREFIX + VMWARE_LINUX_DIR + File.separator + DB_FILE;
      }
    } else {
      if (isWindows()) {
        this.url = url + File.separator + DB_FILE;
      } else {
        this.url = URL_PREFIX + VMWARE_LINUX_DIR + File.separator + DB_FILE;
      }
    }
    //this.url = url + File.separator + DB_FILE;
  }
}
