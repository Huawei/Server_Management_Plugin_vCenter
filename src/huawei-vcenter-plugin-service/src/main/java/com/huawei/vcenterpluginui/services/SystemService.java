package com.huawei.vcenterpluginui.services;

/**
 * Created by hyuan on 2017/5/10.
 */
public interface SystemService {
    /**
     * init table structures and data
     */
    void initDB();

    /**
     * check if table exists in DB
     * @param tableName
     * @return
     */
    boolean isTableExists(String tableName);

    /**
     * check if column exists in the DB table
     * @param tableName
     * @param columnName
     * @return
     */
    boolean isColumnExists(String tableName, String columnName);

}
