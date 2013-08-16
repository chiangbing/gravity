package com.hanborq.gravity;

public class WorkStatus {

  private String workId;
  private String room;
  private WorkState status;
  private double progress;
  private long errors;

  public WorkStatus(String workId,
                    String room,
                    WorkState state,
                    double progress,
                    long errors) {
    this.workId = workId;
    this.room = room;
    this.status = state;
    this.progress = progress;
    this.errors = errors;
  }

  public String getWorkId() {
    return workId;
  }

  public void setWorkId(String workId) {
    this.workId = workId;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public WorkState getState() {
    return status;
  }

  public void setState(WorkState status) {
    this.status = status;
  }

  public double getProgress() {
    return progress;
  }

  public void setProgress(double progress) {
    this.progress = progress;
  }

  public long getErrors() {
    return errors;
  }

  public void setErrors(long errors) {
    this.errors = errors;
  }
}
