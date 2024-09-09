package com.dasea.daph.node.spark3.dataframe.batch.connector.http.util;

import java.io.Serializable;

public class HttpClientResult implements Serializable {

    private static final long serialVersionUID = 2168152194164783950L;

    /**
     * httpresult status code
     */
    private int code;

    /**
     * httpresult body
     */
    private String content;

    public HttpClientResult() {
    }

    public HttpClientResult(int code) {
        this.code = code;
    }

    public HttpClientResult(String content) {
        this.content = content;
    }

    public HttpClientResult(int code, String content) {
        this.code = code;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "HttpClientResult [code=" + code + ", content=" + content + "]";
    }

}
