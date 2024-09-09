package com.dasea.daph.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

public class HttpUtil {

    private HttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ENCODING = "UTF-8";
    private static final int CONNECT_TIMEOUT = 6000 * 2;
    private static final int SOCKET_TIMEOUT = 6000 * 10;
    private static final int INITIAL_CAPACITY = 16;

    public static HttpResult doGet(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue());
            }
        }

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setConfig(requestConfig);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            packageHeader(headers, httpGet);
            return getHttpResult(httpClient, httpGet);
        }
    }

    public static HttpResult doPost(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        packageHeader(headers, httpPost);
        packageParam(params, httpPost);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return getHttpResult(httpClient, httpPost);
        }
    }

    public static HttpResult doPut(String url, Map<String, String> params) throws Exception {

        HttpPut httpPut = new HttpPut(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpPut.setConfig(requestConfig);
        packageParam(params, httpPut);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return getHttpResult(httpClient, httpPut);
        }
    }

    public static HttpResult doDelete(String url, Map<String, String> params) throws Exception {
        if (params == null) {
            params = new HashMap<>(INITIAL_CAPACITY);
        }

        params.put("_method", "delete");
        return doPost(url, null, params);
    }

    public static void packageHeader(Map<String, String> params, HttpRequestBase httpMethod) {
        // encapsulate request header
        if (params != null) {
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                // Set to the request header to the HttpRequestBase object
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void packageParam(Map<String, String> params, HttpEntityEnclosingRequestBase httpMethod) throws UnsupportedEncodingException {
        // Encapsulate request parameters
        if (params != null) {
            List<NameValuePair> nvps = new ArrayList<>();
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // Set to the request's http object
            httpMethod.setEntity(new UrlEncodedFormEntity(nvps, ENCODING));
        }
    }

    public static HttpResult getHttpResult(CloseableHttpClient httpClient, HttpRequestBase httpMethod) throws Exception {
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpMethod)) {
            // get return result
            if (httpResponse != null && httpResponse.getStatusLine() != null) {
                String content = "";
                if (httpResponse.getEntity() != null) {
                    content = EntityUtils.toString(httpResponse.getEntity(), ENCODING);
                }
                return new HttpResult(httpResponse.getStatusLine().getStatusCode(), content);
            }
        }
        return new HttpResult(500, "Internal Server Error");
    }

    public static void release(CloseableHttpResponse httpResponse, CloseableHttpClient httpClient) throws IOException {
        // release resources
        if (httpResponse != null) {
            httpResponse.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
