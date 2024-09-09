package com.dasea.daph.api.exception;

public class TaskRunException extends TaskException {
  public TaskRunException(String message) {
    super(message);
  }
  
  public TaskRunException(Throwable cause) {
    super(cause);
  }
  
  public TaskRunException(String message, Throwable cause) {
    super(message, cause);
  }
}