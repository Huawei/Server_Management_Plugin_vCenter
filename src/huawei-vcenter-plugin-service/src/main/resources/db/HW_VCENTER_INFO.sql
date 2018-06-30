

-- ----------------------------
-- Table structure for HWESightHosts
-- ----------------------------
DROP TABLE IF EXISTS "HW_VCENTER_INFO";
CREATE TABLE "HW_VCENTER_INFO" (
"ID"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,
"HOST_IP"  nvarchar(50),
"USER_NAME"  nvarchar(255),
"PASSWORD"  nvarchar(255),
"CREATE_TIME"  datetime NOT NULL,
CONSTRAINT UNIQUE_HOST_IP UNIQUE (HOST_IP)
);
