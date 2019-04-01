package com.lix.cloud.sdk.http;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class HttpRequester {

    private static final Logger logger = LogManager.getLogger();

    private static final int MAX_CONN_TIMEOUT = 1000;
    private static final int MAX_READ_TIMEOUT = 5000;
    public static final String MAX_TOTAL = "maxTotal";
    public static final String MAX_PER_ROUTE = "maxPerRoute";
    public static final String MAX_IDLE_TIME = "maxIdleTime";
    public static final String SO_TIME_OUT = "soTimeout";
    public static final String SO_KEEP_ALIVE = "soKeepAlive";
    public static final String TCP_NO_DELAY = "tcpNoDelay";
    public static final String RETRY_TIMES = "retryTimes";
    public static final String UTF8 = "utf-8";

    private HttpClient httpClient;

    /**
     * 使用缺省的HttpClient
     */
    public HttpRequester() {
        httpClient = HttpClients.createDefault();
    }

    /**
     * 自定义参数初始化HttpClient
     */
    public HttpRequester(Map<String, Integer> config) {
        if (config == null) {
            httpClient = HttpClients.createDefault();
        } else {
            httpClient = createHttpClient(config);
        }
    }

    /**
     * post请求 获取内容
     * 请求参数以json字符串方式提供
     * 
     * @param url
     * @param body
     * @return
     */
    public String post(String url, String body) {
        return post(url, body, "application/json");
    }

    /**
     * post请求 获取内容
     * 自定义Content-Type
     * 
     * @param url
     * @param body
     * @param contentType
     * @return
     */
    public String post(String url, String body, String contentType) {
        HttpPost post = new HttpPost(url);
        post.setConfig(getRequestConfig());
        post.setHeader("Content-Type", contentType);
        post.setEntity(new StringEntity(body, UTF8));
        return doExecute(post);
    }

    /**
     * post请求 获取内容
     * 请求参数以键值对方式提供
     *
     * @param url
     * @return
     */
    public String post(String url, Map<String, Object> params) {
        HttpPost post = new HttpPost(url);
        post.setConfig(getRequestConfig());
        post.setEntity(getParamsEntity(params));
        return doExecute(post);
    }

    /**
     * GET请求 获取内容
     *
     * @param url
     * @return
     */
    public String get(String url) {
        HttpGet get = new HttpGet(url);
        get.setConfig(getRequestConfig());
        return doExecute(get);
    }

    private String doExecute(HttpUriRequest request) {
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, UTF8);
            EntityUtils.consume(entity);
            return result;
        } catch (IOException e) {
            logger.error("HttpClient.execute failed with error", e);
            return null;
        }
    }

    /**
     * 配置请求的超时设置
     * @return
     */
    private RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(MAX_READ_TIMEOUT)
                .setConnectTimeout(MAX_CONN_TIMEOUT)
                .setSocketTimeout(MAX_CONN_TIMEOUT).build();
    }

    /**
     * 将参数Map转换成HttpEntity
     * @param params
     * @return
     */
    private HttpEntity getParamsEntity(Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
        try {
            return new UrlEncodedFormEntity(nvps, UTF8);
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException when setPostParams", e);
            return null;
        }
    }

    /**
     * 创建HttpClient对象
     * @param config 配置信息
     * @return
     */
    private CloseableHttpClient createHttpClient(Map<String, Integer> config) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf).register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        if (config.containsKey(MAX_TOTAL)) {
            // 将最大连接数增加
            cm.setMaxTotal(config.get(MAX_TOTAL));
        }
        if (config.containsKey(MAX_PER_ROUTE)) {
            // 将每个路由基础的连接增加
            cm.setDefaultMaxPerRoute(config.get(MAX_PER_ROUTE));
        }
        HttpClientBuilder builder = HttpClients.custom();
        builder.setConnectionManager(cm);
        builder.setRetryHandler(new InnerHttpRequestRetryHandler(config.get(RETRY_TIMES)));
        builder.setDefaultSocketConfig(getSocketConfig(config));
        if (config.containsKey(MAX_IDLE_TIME)) {
            builder.evictIdleConnections(config.get(MAX_IDLE_TIME), TimeUnit.SECONDS);
        }
        return builder.build();
    }

    private SocketConfig getSocketConfig(Map<String, Integer> config) {
        SocketConfig.Builder socBuilder = SocketConfig.custom();
        if (config.containsKey(SO_TIME_OUT)) {
            socBuilder.setSoTimeout(config.get(SO_TIME_OUT) * 1000);
        }
        if (config.containsKey(SO_KEEP_ALIVE)) {
            socBuilder.setSoKeepAlive(config.get(SO_KEEP_ALIVE).equals(1));
        }
        if (config.containsKey(TCP_NO_DELAY)) {
            socBuilder.setTcpNoDelay(config.get(TCP_NO_DELAY).equals(1));
        }
        return socBuilder.build();
    }

    /**
     * 重试处理handler
     * 
     * @author wangkui-lhq
     *
     */
    private class InnerHttpRequestRetryHandler implements HttpRequestRetryHandler {

        private int retryTimes = 5;

        private InnerHttpRequestRetryHandler(Integer retryTimes) {
            if (retryTimes != null) {
                this.retryTimes = retryTimes;
            }
        }

        @Override
        public boolean retryRequest(IOException exception, int exeCount, HttpContext context) {
            // 如果已经重试了n次，就放弃
            if (exeCount >= retryTimes) {
                return false;
            }
            // 如果服务器丢掉了连接，那么就重试
            if (exception instanceof NoHttpResponseException) {
                return true;
            }
            // 不要重试SSL握手异常
            if (exception instanceof SSLHandshakeException) {
                return false;
            }
            // 超时
            if (exception instanceof InterruptedIOException) {
                return false;
            }
            // 目标服务器不可达
            if (exception instanceof UnknownHostException) {
                return false;
            }
            // 连接被拒绝
            if (exception instanceof ConnectTimeoutException) {
                return false;
            }
            // SSL握手异常
            if (exception instanceof SSLException) {
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的/请求内容相同，就再次尝试
            return !(request instanceof HttpEntityEnclosingRequest);
        }
    }
}
