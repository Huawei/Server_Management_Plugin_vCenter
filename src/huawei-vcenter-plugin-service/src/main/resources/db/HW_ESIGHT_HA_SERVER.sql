

-- ----------------------------
-- Table structure for HWESightHosts
-- ----------------------------
DROP TABLE IF EXISTS "HW_ESIGHT_HA_SERVER";
CREATE TABLE "HW_ESIGHT_HA_SERVER" (
"ID"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,
"ESIGHT_HOST_ID"  int,
"UUID"  nvarchar(255) NOT NULL,
"ESIGHT_SERVER_TYPE"  nvarchar(50),
"ESIGHT_SERVER_DN"  nvarchar(255),
"ESIGHT_SERVER_PARENT_DN"  nvarchar(255),
"ESIGHT_SERVER_STATUS"  nvarchar(50),
"STATUS"  int,
"PROVIDER_SID"  nvarchar(255),
"HA_HOST_SYSTEM"  nvarchar(255),
"LAST_MODIFY_TIME"  datetime,
"CREATE_TIME"  datetime NOT NULL,
CONSTRAINT UNIQUE_UUID UNIQUE (UUID),
FOREIGN KEY (ESIGHT_HOST_ID)
        REFERENCES HW_ESIGHT_HOST (ID)
        ON DELETE CASCADE
);
