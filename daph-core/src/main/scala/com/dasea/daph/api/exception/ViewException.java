package com.dasea.daph.api.exception;

public class ViewException extends NodeException {
  public ViewException(String message) {
    super(message);
  }

  public ViewException(Throwable cause) {
    super(cause);
  }

  public ViewException(String message, Throwable cause) {
    super(message, cause);
  }
}