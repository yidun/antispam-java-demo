/*
 * @(#) HttpClientUtils.java 2016年2月3日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * HttpClient工具类
 * 
 * @author hzgaomin
 * @version 2016年2月3日
 */
public class HttpClient4Utils {
    /**
     * 实例化HttpClient
     * 
     * @param maxTotal
     * @param maxPerRoute
     * @param socketTimeout
     * @param connectTimeout
     * @param connectionRequestTimeout
     * @return
     */
    public static HttpClient createHttpClient(int maxTotal, int maxPerRoute, int socketTimeout, int connectTimeout,
            int connectionRequestTimeout) {
        RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(defaultRequestConfig).build();
        return httpClient;
    }

    /**
     * 发送post请求
     * 
     * @param httpClient
     * @param url 请求地址
     * @param params 请求参数
     * @param encoding 编码
     * @return
     */
    public static String sendPost(HttpClient httpClient, String url, Map<String, String> params, Charset encoding) {
        String resp = "";
        HttpPost httpPost = new HttpPost(url);
        if (params != null && params.size() > 0) {
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(formParams, encoding);
            httpPost.setEntity(postEntity);
        }
        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) httpClient.execute(httpPost);
            resp = EntityUtils.toString(response.getEntity(), encoding);
        } catch (Exception e) {
            // log
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // log
                    e.printStackTrace();
                }
            }
        }
        return resp;
    }

    public static String sendGetByHeader(HttpClient httpClient, String url, Map<String, String> params,
            Map<String, String> headers) throws Exception {
        HttpGet httpGet = null;
        if (MapUtils.isNotEmpty(params)) {
            final List<NameValuePair> qparams = new LinkedList<NameValuePair>();
            for (Map.Entry<String, String> param : params.entrySet()) {
                qparams.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
            httpGet = new HttpGet(url + "?" + URLEncodedUtils.format(qparams, "UTF-8"));
        } else {
            httpGet = new HttpGet(url);
        }
        if (headers != null) {
            for (String key : headers.keySet()) {
                httpGet.addHeader(key, headers.get(key));
            }
        }
        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) httpClient.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                // 返回状态错误，直接抛异常，记录时精简一下log输出
                throw new IllegalStateException(String.format("错误的接口返回状态值, status=%d, response=%s", status,
                        StringUtils.abbreviate(EntityUtils.toString(response.getEntity()), 1024)));
            }
            // EntityUtils is not recommended
            return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8")); // 该方法会释放连接。。。
        } catch (Exception e) {
            String msg = String.format("http接口调用出错url=%s, params=%s", url, params);
            throw new Exception(msg, e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * 关闭response
     *
     * @param response
     */
    public static void closeQuietly(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
