package com.huawei.vcenterpluginui.utils;

import com.huawei.esight.utils.JsonUtil;
import com.huawei.vcenterpluginui.entity.HAEventDef;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAEventHelper {

  public final static Logger LOGGER = LoggerFactory.getLogger(HAEventHelper.class);

  private static final Map<Long, HAEventDef> EVENT_DEF_MAP = new HashMap<>();

  private static final HAEventHelper INSTANCE = new HAEventHelper();

  private HAEventHelper() {
    InputStream resourceAsStream = null;
    try {
      resourceAsStream = this.getClass().getResourceAsStream("/eventsForHA.json");
      byte[] buff = new byte[resourceAsStream.available()];
      resourceAsStream.read(buff);
      Collection<Map<String, Object>> events = (Collection<Map<String, Object>>) JsonUtil
          .readAsMap(new String(buff, "UTF-8")).get("events");
      for (Map<String, Object> eventMap : events) {
        String eventId = eventMap.get("eventId").toString();
        String severity = eventMap.get("severity").toString();
        String eventComponent = eventMap.get("eventComponent").toString();
        EVENT_DEF_MAP.put(toLong(eventId), new HAEventDef(eventId, severity, eventComponent));
      }
    } catch (IOException e) {
      LOGGER.info("Cannot parse eventsForHA.json", e);
    } finally {
      if (resourceAsStream != null) {
        try {
          resourceAsStream.close();
        } catch (IOException e) {
          LOGGER.info("Cannot close resource");
        }
      }
    }
  }

  private static long toLong(String eventId) {
    return Long.parseLong(eventId, 16);
  }

  public static HAEventHelper getInstance() {
    return INSTANCE;
  }

  public HAEventDef getHaEventDef(String eventId) {
    if (eventId == null) {
      return null;
    }
    if (eventId.startsWith("0x")) {
      eventId = eventId.replace("0x", "");
    }
    return EVENT_DEF_MAP.get(toLong(eventId));
  }

  public HAEventDef getHaEventDef(long alarmId) {
    return EVENT_DEF_MAP.get(alarmId);
  }

}
