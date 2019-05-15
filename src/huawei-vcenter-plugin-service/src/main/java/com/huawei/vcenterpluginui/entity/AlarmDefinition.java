package com.huawei.vcenterpluginui.entity;

public class AlarmDefinition {

  private String eventName;
  private String severity;
  private String eventTypeID;
  private String eventType;
  private String description;

  private String morValue;
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getVcEventId() {
    return eventName;
  }

  public String getMorValue() {
    return morValue;
  }

  public void setMorValue(String morValue) {
    this.morValue = morValue;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getEventTypeID() {
    return eventTypeID;
  }

  public String getVcResumeEventId() {
    return getVcEventId() + "-resume";
  }

  public void setEventTypeID(String eventTypeID) {
    this.eventTypeID = eventTypeID;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "AlarmDefinition{" +
        "eventName='" + eventName + '\'' +
        ", severity='" + severity + '\'' +
        ", eventTypeID='" + eventTypeID + '\'' +
        ", eventType='" + eventType + '\'' +
        ", description='" + description + '\'' +
        ", morValue='" + morValue + '\'' +
        ", resumeEventTypeId='" + getVcResumeEventId() + '\'' +
        ", id=" + id +
        '}';
  }
}
