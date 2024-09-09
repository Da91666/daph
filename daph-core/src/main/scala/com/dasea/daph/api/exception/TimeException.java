package com.dasea.daph.api.exception;

public class TimeException extends DaphException {
  public TimeException(String message) {
    super(message);
  }

  public TimeException(Throwable cause) {
    super(cause);
  }

  public TimeException(String message, Throwable cause) {
    super(message, cause);
  }
}