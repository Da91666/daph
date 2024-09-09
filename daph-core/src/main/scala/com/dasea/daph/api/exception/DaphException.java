package com.dasea.daph.api.exception;

public class DaphException extends Exception {
  private static final long serialVersionUID = -5010825613163203385L;
  
  public DaphException(String message) {
    super(message);
  }
  
  public DaphException(Throwable cause) {
    super(cause);
  }
  
  public DaphException(String message, Throwable cause) {
    super(message, cause);
  }
}