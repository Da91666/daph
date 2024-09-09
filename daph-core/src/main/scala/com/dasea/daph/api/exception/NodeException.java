package com.dasea.daph.api.exception;

public class NodeException extends DaphException {
  public NodeException(String message) {
    super(message);
  }
  
  public NodeException(Throwable cause) {
    super(cause);
  }
  
  public NodeException(String message, Throwable cause) {
    super(message, cause);
  }
}