package com.huawei.vcenterpluginui.entity;

public class HAEventDef {

  private String eventId;
  private String severity;
  private String eventComponent;

  public HAEventDef(String eventId, String severity, String eventComponent) {
    this.eventId = eventId;
    this.severity = severity;
    this.eventComponent = eventComponent;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getEventComponent() {
    return eventComponent;
  }

  public void setEventComponent(String eventComponent) {
    this.eventComponent = eventComponent;
  }

  @Override
  public String toString() {
    return "HAEventDef{" +
        "eventId='" + eventId + '\'' +
        ", severity='" + severity + '\'' +
        ", eventComponent='" + eventComponent + '\'' +
        '}';
  }
}
