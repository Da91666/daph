package com.dasea.daph.api.exception;

public class TaskKilledException extends TaskException {
  public TaskKilledException(String message) {
    super(message);
  }

  public TaskKilledException(Throwable cause) {
    super(cause);
  }

  public TaskKilledException(String message, Throwable cause) {
    super(message, cause);
  }
}