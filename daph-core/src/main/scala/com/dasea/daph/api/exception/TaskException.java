package com.dasea.daph.api.exception;

public class TaskException extends DaphException {
  public TaskException(String message) {
    super(message);
  }
  
  public TaskException(Throwable cause) {
    super(cause);
  }
  
  public TaskException(String message, Throwable cause) {
    super(message, cause);
  }
}