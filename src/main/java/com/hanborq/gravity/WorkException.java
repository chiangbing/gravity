package com.hanborq.gravity;

public class WorkException extends Exception {

  public WorkException(String message) {
    super(message);
  }

  public WorkException(String message, Throwable cause) {
    super(message, cause);
  }
}
