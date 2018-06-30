

-- ----------------------------
-- Table structure for HWESightHosts
-- ----------------------------
DROP TABLE IF EXISTS "HW_SERVER_DEVICE_DETAIL";
CREATE TABLE "HW_SERVER_DEVICE_DETAIL" (
  "ID"  integer PRIMARY KEY AUTO_INCREMENT NOT NULL,
  "ESIGHT_HOST_ID"  int,
  "UUID"  nvarchar(255) NOT NULL,
  "DN"  nvarchar(255),
  "COMPONENT"  nvarchar(32),
  "HEALTH_STATE"  nvarchar(4),
  "PRESENT_STATE"  nvarchar(4),
  "UPDATE_TIME"  datetime NOT NULL,
  FOREIGN KEY (ESIGHT_HOST_ID)
        REFERENCES HW_ESIGHT_HOST (ID)
        ON DELETE CASCADE
);
