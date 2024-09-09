package com.dasea.daph.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public enum HttpCUtil {

    INSTANCE;

    private Logger logger = LoggerFactory.getLogger(HttpCUtil.class);

    private CloseableHttpClient httpClient;

    {
        httpClient = getHttpClientBuilder().build();
    }

    public HttpCResult get(String url) {
        return this.get(url, null);
    }

    public HttpCResult get(String url, Header[] headers) {

        HttpCResult httpResult;

        HttpGet httpGet = new HttpGet();
        httpGet.setURI(URI.create(url));
        // 设置请求头
        if (Objects.nonNull(headers) && headers.length > 0) {
            httpGet.setHeaders(headers);
        }
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            // 返回状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity httpEntity = httpResponse.getEntity();
            // 设置返回结果
            httpResult = HttpCResult.of().setStatusCode(statusCode).setResult(EntityUtils.toString(httpEntity));
            // 消费流实体
            EntityUtils.consumeQuietly(httpEntity);
        } catch (IOException e) {
            httpResult = HttpCResult.of().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } finally {
//            closeStream(httpClient);
        }
        return httpResult;
    }

    public HttpCResult post(String url, String json, String token) {

        HttpCResult httpResult;

        HttpPost httpPost = new HttpPost(url);
        // 设置请求参数
        StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
        entity.setContentType("application/json;charset=UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Authorization", token);

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            // 返回状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity httpEntity = httpResponse.getEntity();
            // 设置返回结果
            httpResult = HttpCResult.of().setStatusCode(statusCode).setResult(EntityUtils.toString(httpEntity));
            // 消费流实体
            EntityUtils.consumeQuietly(httpEntity);
        } catch (Exception e) {
            httpResult = HttpCResult.of().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } finally {
//            closeStream(httpClient);
        }
        return httpResult;
    }

    public HttpCResult post(String url, String json) {

        HttpCResult httpResult;

        HttpPost httpPost = new HttpPost(url);
        // 设置请求参数
        StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
        entity.setContentType("application/json;charset=UTF-8");
        httpPost.setEntity(entity);

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            // 返回状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity httpEntity = httpResponse.getEntity();
            // 设置返回结果
            httpResult = HttpCResult.of().setStatusCode(statusCode).setResult(EntityUtils.toString(httpEntity));
            // 消费流实体
            EntityUtils.consumeQuietly(httpEntity);
        } catch (Exception e) {
            httpResult = HttpCResult.of().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } finally {
//            closeStream(httpClient);
        }
        return httpResult;
    }

    private void closeStream(CloseableHttpClient httpClient) {
        if (Objects.nonNull(httpClient)) {
            try {
                httpClient.close();
            } catch (IOException e) {
            }
        }
    }

    private HttpClientBuilder getHttpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(getHttpClientConnectionManager())
            .setDefaultRequestConfig(getRequestConfig())
            .setRetryHandler(DefaultHttpRequestRetryHandler.INSTANCE);
        return httpClientBuilder;
    }

    private PoolingHttpClientConnectionManager getHttpClientConnectionManager() {
        SSLContext sslContext = null;
        try {
            sslContext = createIgnoreVerifySSL();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("=======Https Client======");
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslContext))
                .build();
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        httpClientConnectionManager.setMaxTotal(5);
        // 并发数
        httpClientConnectionManager.setDefaultMaxPerRoute(5);
        return httpClientConnectionManager;
    }

    private RequestConfig.Builder getBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom();
        return builder.setConnectTimeout((int)TimeUnit.SECONDS.toMillis(120))
            .setConnectionRequestTimeout((int)TimeUnit.SECONDS.toMillis(120))
            .setSocketTimeout((int)TimeUnit.SECONDS.toMillis(120));
    }

    private RequestConfig getRequestConfig() {
        RequestConfig requestConfig = getBuilder().build();
        return requestConfig;
    }



    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        logger.info("绕过Https的SSLContext");
        SSLContext sc = SSLContext.getInstance("SSLv3");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[] { trustManager }, null);
        return sc;
    }

}
