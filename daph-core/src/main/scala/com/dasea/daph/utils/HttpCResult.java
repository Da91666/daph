package com.dasea.daph.utils;

import org.apache.http.HttpStatus;

public class HttpCResult {

    private int statusCode;

    private String result;

    public int getStatusCode() {
        return statusCode;
    }

    public HttpCResult setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getResult() {
        return result;
    }

    public HttpCResult setResult(String result) {
        this.result = result;
        return this;
    }

    public static HttpCResult of(){
        return new HttpCResult();
    }

    public boolean isSuccess() {
        return statusCode >= HttpStatus.SC_OK && statusCode <= 299;
    }

    public boolean isClientError() {
        return statusCode >= HttpStatus.SC_BAD_REQUEST && statusCode <= 499;
    }

    public boolean isServerError() {
        return statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode <= 599;
    }
}
