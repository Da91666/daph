package com.dasea.daph.node.spark3.dataframe.batch.connector.http;

import com.dasea.daph.api.config.NodeConfig;

public class HttpInputConfig extends NodeConfig {
    private String url;
    private String method;
    private String header;
    private String requestParams;
    private String syncPath;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getSyncPath() {
        return syncPath;
    }

    public void setSyncPath(String syncPath) {
        this.syncPath = syncPath;
    }
}
