package com.huawei.vcenterpluginui.constant;

public class SqlFileConstant {
	public static final String HW_ESIGHT_HOST = "HW_ESIGHT_HOST";
	public static final String HW_ESIGHT_TASK = "HW_ESIGHT_TASK";
	public static final String HW_TASK_RESOURCE = "HW_TASK_RESOURCE";
	public static final String HW_ESIGHT_HA_SERVER = "HW_ESIGHT_HA_SERVER";
	public static final String HW_VCENTER_INFO = "HW_VCENTER_INFO";
	public static final String HW_SERVER_DEVICE_DETAIL = "HW_SERVER_DEVICE_DETAIL";
	public static final String HW_ALARM_DEFINITION = "HW_ALARM_DEFINITION";
	public static final String SUFFIX = ".sql";
	
	public static final String HW_ESIGHT_HOST_SQL = "DROP TABLE IF EXISTS \"HW_ESIGHT_HOST\";\n" +
										            "CREATE TABLE \"HW_ESIGHT_HOST\" (\n" +
										            "\"ID\"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,\n" +
										            "\"HOST_IP\"  nvarchar(255),\n" +
										            "\"ALIAS_NAME\"  nvarchar(255),\n" +
										            "\"HOST_PORT\"  int,\n" +
																"\"SYSTEM_ID\" nvarchar(50),\n" +
										            "\"LOGIN_ACCOUNT\"  nvarchar(255),\n" +
										            "\"LOGIN_PWD\"  nvarchar(255),\n" +
										            "\"LATEST_STATUS\"  nvarchar(50),\n" +
										            "\"RESERVED_INT1\"  int,\n" +
										            "\"RESERVED_INT2\"  int,\n" +
										            "\"RESERVED_STR1\"  nvarchar(255),\n" +
										            "\"RESERVED_STR2\"  nvarchar(255),\n" +
										            "\"LAST_MODIFY_TIME\"  datetime,\n" +
										            "\"CREATE_TIME\"  datetime NOT NULL,\n" +
										            "\"CERT_PATH\"  nvarchar(255)\n" +
										            ");";
	
	public static final String HW_ESIGHT_TASK_SQL = "DROP TABLE IF EXISTS \"HW_ESIGHT_TASK\";\n" +
										            "CREATE TABLE \"HW_ESIGHT_TASK\" (\n" +
										            "\"ID\"  INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,\n" +
										            "\"HW_ESIGHT_HOST_ID\"  INTEGER NOT NULL,\n" +
										            "\"TASK_NAME\"  varchar(255) NOT NULL,\n" +
										            "\"SOFTWARE_SOURCE_NAME\"  varchar(255) NOT NULL,\n" +
										            "\"TEMPLATES\"  varchar(500),\n" +
										            "\"DEVICE_IP\"  varchar(1024),\n" +
										            "\"TASK_STATUS\"  varchar(255),\n" +
										            "\"TASK_PROGRESS\"  INTEGER,\n" +
										            "\"TASK_RESULT\"  varchar(255),\n" +
										            "\"TASK_CODE\"  varchar(255),\n" +
										            "\"ERROR_DETAIL\"  varchar(2000),\n" +
										            "\"SYNC_STATUS\"  varchar(255),\n" +
										            "\"TASK_TYPE\"  varchar(255) NOT NULL,\n" +
										            "\"RESERVED_INT1\" INTEGER,\n" +
										            "\"RESERVED_INT2\" INTEGER,\n" +
										            "\"RESERVED_STR1\" varchar(500),\n" +
										            "\"RESERVED_STR2\" varchar(500),\n" +
										            "\"LAST_MODIFY_TIME\"  datetime,\n" +
										            "\"CREATE_TIME\"  datetime NOT NULL\n" +
										            ");";
	
	public static final String HW_TASK_RESOURCE_SQL = "DROP TABLE IF EXISTS \"HW_TASK_RESOURCE\";\n" +
											            "CREATE TABLE \"HW_TASK_RESOURCE\" (\n" +
											            "\"ID\"  INTEGER  PRIMARY KEY AUTO_INCREMENT NOT NULL,\n" +
											            "\"HW_ESIGHT_TASK_ID\"  varchar(255) NOT NULL,\n" +
											            "\"DN\"  varchar(255) NOT NULL,\n" +
											            "\"IP_ADDRESS\"  varchar(255),\n" +
											            "\"SYNC_STATUS\"  varchar(255),\n" +
											            "\"TASK_TYPE\"  varchar(255) NOT NULL,\n" +
											            "\"DEVICE_RESULT\"  varchar(255),\n" +
											            "\"ERROR_DETAIL\"  varchar(2000),\n" +
											            "\"DEVICE_PROGRESS\"  INTEGER,\n" +
											            "\"ERROR_CODE\"  varchar(255) ,\n" +
											            "\"RESERVED_INT1\" INTEGER,\n" +
											            "\"RESERVED_INT2\" INTEGER,\n" +
											            "\"RESERVED_STR1\" varchar(500),\n" +
											            "\"RESERVED_STR2\" varchar(500),\n" +
											            "\"LAST_MODIFY_TIME\"  datetime,\n" +
											            "\"CREATE_TIME\"  datetime NOT NULL\n" +
											            ");";

    public static final String HW_ESIGHT_HA_SERVER_SQL = "DROP TABLE IF EXISTS \"HW_ESIGHT_HA_SERVER\";\n" +
			"CREATE TABLE \"HW_ESIGHT_HA_SERVER\" (\n" +
			"\"ID\"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,\n" +
			"\"ESIGHT_HOST_ID\"  int,\n" +
			"\"UUID\"  nvarchar(255) NOT NULL,\n" +
			"\"ESIGHT_SERVER_TYPE\"  nvarchar(50),\n" +
			"\"ESIGHT_SERVER_DN\"  nvarchar(255),\n" +
			"\"ESIGHT_SERVER_PARENT_DN\"  nvarchar(255),\n" +
			"\"ESIGHT_SERVER_STATUS\"  nvarchar(50),\n" +
			"\"STATUS\"  int,\n" +
			"\"PROVIDER_SID\"  nvarchar(255),\n" +
			"\"HA_HOST_SYSTEM\"  nvarchar(255),\n" +
			"\"LAST_MODIFY_TIME\"  datetime,\n" +
			"\"CREATE_TIME\"  datetime NOT NULL,\n" +
			"CONSTRAINT UNIQUE_UUID UNIQUE (UUID),\n" +
			"FOREIGN KEY (ESIGHT_HOST_ID)\n" +
			"        REFERENCES HW_ESIGHT_HOST (ID)\n" +
			"        ON DELETE CASCADE\n" +
			");";

    public static final String HW_VCENTER_INFO_SQL = "DROP TABLE IF EXISTS \"HW_VCENTER_INFO\";\n"
            + "CREATE TABLE \"HW_VCENTER_INFO\" (\n"
            + "\"ID\"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,\n"
            + "\"HOST_IP\"  nvarchar(50),\n"
            + "\"USER_NAME\"  nvarchar(255),\n"
						+ "\"PASSWORD\"  nvarchar(255),\n"
						+ "\"STATE\"  BOOLEAN,\n"
            + "\"CREATE_TIME\"  datetime NOT NULL,\n"
            + "\"PUSH_EVENT\"  BOOLEAN,\n"
            + "\"PUSH_EVENT_LEVEL\"  integer,\n"
            + "CONSTRAINT UNIQUE_HOST_IP UNIQUE (HOST_IP)\n"
            + ");";

	public static final String HW_SERVER_DEVICE_DETAIL_SQL = "DROP TABLE IF EXISTS \"HW_SERVER_DEVICE_DETAIL\";\n" +
			"CREATE TABLE \"HW_SERVER_DEVICE_DETAIL\" (\n" +
			"  \"ID\"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,\n" +
			"  \"ESIGHT_HOST_ID\"  int,\n" +
			"  \"UUID\"  nvarchar(255) NOT NULL,\n" +
			"  \"DN\"  nvarchar(255),\n" +
			"  \"COMPONENT\"  nvarchar(32),\n" +
			"  \"HEALTH_STATE\"  nvarchar(4),\n" +
			"  \"PRESENT_STATE\"  nvarchar(4),\n" +
			"  \"UPDATE_TIME\"  datetime NOT NULL,\n" +
			"  FOREIGN KEY (ESIGHT_HOST_ID)\n" +
			"        REFERENCES HW_ESIGHT_HOST (ID)\n" +
			"        ON DELETE CASCADE\n" +
			");";

	public static final String HW_ALARM_DEFINITION_SQL = "DROP TABLE IF EXISTS \"HW_ALARM_DEFINITION\";\n"
			+ "CREATE TABLE \"HW_ALARM_DEFINITION\" (\n"
			+ "  \"ID\"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,\n"
			+ "  \"MOR_VALUE\"  nvarchar(255),\n"
			+ "  \"EVENT_TYPE_ID\"  nvarchar(255),\n"
			+ "  \"EVENT_NAME\"  nvarchar(255)\n"
			+ ");";

	public static final String HW_ESIGHT_HOST_SYSTEM_ID = "SYSTEM_ID";
	public static final String HW_ESIGHT_HOST_ALTER_SQL = "ALTER TABLE HW_ESIGHT_HOST ADD SYSTEM_ID VARCHAR(50) NULL;";

	public static final String HW_VCENTER_INFO_STATE = "STATE";
	public static final String HW_VCENTER_INFO_PUSHEVENT = "PUSH_EVENT";
	public static final String HW_VCENTER_INFO_PUSHEVENTLEVEL = "PUSH_EVENT_LEVEL";
	public static final String HW_VCENTER_INFO_STATE_ALTER_SQL = "ALTER TABLE HW_VCENTER_INFO ADD STATE BOOLEAN NULL;";
	public static final String HW_VCENTER_INFO_PUSHEVENT_ALTER_SQL = "ALTER TABLE HW_VCENTER_INFO ADD PUSH_EVENT BOOLEAN NULL;";
	public static final String HW_VCENTER_INFO_PUSHEVENTLEVEL_ALTER_SQL = "ALTER TABLE HW_VCENTER_INFO ADD PUSH_EVENT_LEVEL integer NULL;";

	public static final String HW_ALARM_DEFINITION_SEVERITY = "SEVERITY";
	public static final String HW_ALARM_DEFINITION_EVENTTYPE = "EVENT_TYPE";
	public static final String HW_ALARM_DEFINITION_DESCRIPTION = "DESCRIPTION";

	public static final String HW_ALARM_DEFINITION_SEVERITY_ALTER_SQL = "ALTER TABLE HW_ALARM_DEFINITION ADD SEVERITY VARCHAR(32) NULL;";
	public static final String HW_ALARM_DEFINITION_EVENTTYPE_ALTER_SQL = "ALTER TABLE HW_ALARM_DEFINITION ADD EVENT_TYPE VARCHAR(32) NULL;";
	public static final String HW_ALARM_DEFINITION_DESCRIPTION_ALTER_SQL = "ALTER TABLE HW_ALARM_DEFINITION ADD DESCRIPTION VARCHAR(255) NULL;";


	public static final String COLUMN_ESIGHT_SERVER_PARENT_DN = "ESIGHT_SERVER_PARENT_DN";
	public static final String COLUMN_ESIGHT_SERVER_PARENT_DN_SQL = "ALTER TABLE " + HW_ESIGHT_HA_SERVER + " ADD " +
            COLUMN_ESIGHT_SERVER_PARENT_DN + " nvarchar(255);";

}
