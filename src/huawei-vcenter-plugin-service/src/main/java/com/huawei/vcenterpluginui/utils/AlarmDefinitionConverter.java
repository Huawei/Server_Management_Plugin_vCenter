package com.huawei.vcenterpluginui.utils;

import com.huawei.vcenterpluginui.entity.AlarmDefinition;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AlarmDefinitionConverter {

  public final static Log LOGGER = LogFactory.getLog(AlarmDefinitionConverter.class);

  private static final Integer EVENT_TYPE_ID_LEN = 8;

  private Document document = null;

  public AlarmDefinitionConverter() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      document = db.parse(this.getClass().getResourceAsStream("/alarm_definitions.xml"));
    } catch (Exception e) {
      LOGGER.error("Failed to parse xml: alarm_definitions.xml", e);
    }
  }

  public List<AlarmDefinition> parseAlarmDefinitionList() {
    List<AlarmDefinition> result = new ArrayList<>();
    NodeList eventIdNodeList = document.getElementsByTagName("EventID");
    for (int i = 0; i < eventIdNodeList.getLength(); i++) {
      AlarmDefinition alarmDefinition = new AlarmDefinition();
      result.add(alarmDefinition);

      Node eventIdNode = eventIdNodeList.item(i);
      NamedNodeMap eventIdMap = eventIdNode.getAttributes();

      alarmDefinition.setSeverity(eventIdMap.getNamedItem("severity").getNodeValue());
      alarmDefinition.setEventName(eventIdMap.getNamedItem("name").getNodeValue());

      NodeList eventIdChildNodeList = eventIdNode.getChildNodes();
      for (int k = 0; k < eventIdChildNodeList.getLength(); k++) {
        if ("EventType".equalsIgnoreCase(eventIdChildNodeList.item(k).getNodeName())) {
          NodeList eventTypeChildNodeList = eventIdChildNodeList.item(k).getChildNodes();
          for (int j = 0; j < eventTypeChildNodeList.getLength(); j++) {
            Node childNode = eventTypeChildNodeList.item(j);
            switch (childNode.getNodeName()) {
              case "eventTypeID":
                alarmDefinition.setEventTypeID(childNode.getTextContent());
                continue;
              case "eventType":
                alarmDefinition.setEventType(childNode.getTextContent());
                continue;
              case "description":
                alarmDefinition.setDescription(childNode.getTextContent());
                continue;
            }
          }
        }
      }
    }
    return result;
  }

  public AlarmDefinition findAlarmDefinition(int alarmId) {
    NodeList eventIdNodeList = document.getElementsByTagName("EventID");
    for (int i = 0; i < eventIdNodeList.getLength(); i++) {
      Node eventIdNode = eventIdNodeList.item(i);
      NamedNodeMap eventIdMap = eventIdNode.getAttributes();

      String severity = eventIdMap.getNamedItem("severity").getNodeValue();
      String name = eventIdMap.getNamedItem("name").getNodeValue();
      String eventTypeID = null;
      String eventType = null;
      String description = null;

      NodeList eventIdChildNodeList = eventIdNode.getChildNodes();
      AlarmDefinition alarmDefinition = null;
      for (int k = 0; k < eventIdChildNodeList.getLength(); k++) {
        if ("EventType".equalsIgnoreCase(eventIdChildNodeList.item(k).getNodeName())) {
          NodeList eventTypeChildNodeList = eventIdChildNodeList.item(k).getChildNodes();
          for (int j = 0; j < eventTypeChildNodeList.getLength(); j++) {
            Node childNode = eventTypeChildNodeList.item(j);
            switch (childNode.getNodeName()) {
              case "eventTypeID":
                eventTypeID = childNode.getTextContent();
                String hexString = Integer.toHexString(alarmId);
                while (hexString.length() < EVENT_TYPE_ID_LEN) {
                  hexString = "0" + hexString;
                }
                if (eventTypeID.toUpperCase().contains(hexString.toUpperCase())) {
                  alarmDefinition = new AlarmDefinition();
                }
                continue;
              case "eventType":
                eventType = childNode.getTextContent();
                continue;
              case "description":
                description = childNode.getTextContent();
                continue;
            }
          }
          if (alarmDefinition != null) {
            alarmDefinition.setSeverity(severity);
            alarmDefinition.setEventName(name);
            alarmDefinition.setEventTypeID(eventTypeID);
            alarmDefinition.setEventType(eventType);
            alarmDefinition.setDescription(description);
            return alarmDefinition;
          }
        }
      }
    }
    return null;
  }

}
