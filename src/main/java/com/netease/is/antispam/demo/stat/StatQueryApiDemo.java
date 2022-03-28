/*
 * @(#) ImageCheckAPIDemo.java 2016年3月15日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.stat;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.client.HttpClient;

import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;

/**
 * 调用易盾反垃圾云服务图片在线检测接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 * @author hzgaomin
 * @version 2016年2月3日
 */
public class StatQueryApiDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾云服务图片在线检测接口地址
     */
    private final static String API_URL = "https://openapi.dun.163.com/openapi/v2/antispam/stat/labelDistribution/query";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> headers = new HashMap<>();

        // 组装公共参数（请求头参数）
        headers.put("X-YD-SECRETID", SECRETID);
        Long timestamp = System.currentTimeMillis();
        headers.put("X-YD-TIMESTAMP", String.valueOf(timestamp));
        String nonce = String.valueOf(new Random().nextInt());
        headers.put("X-YD-NONCE", nonce);
        // 组装私有参数
        Map<String, String> params = new HashMap<>();
        params.put("clientId", "your_client_id");
        params.put("startTime", String.valueOf(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3));
        params.put("endTime", String.valueOf(System.currentTimeMillis()));
        // 生成签名
        String signString = SignatureUtils.getCheckSum(genSignatureSring(params), SECRETKEY, nonce, timestamp + "");
        headers.put("X-YD-SIGN", signString);
        // 发送请求
        String response = HttpClient4Utils.sendGetByHeader(httpClient, API_URL, params, headers);
        System.out.println(response);
    }

    public static String genSignatureSring(Map<String, String> params) throws UnsupportedEncodingException {
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuffer paramBuffer = new StringBuffer();
        for (String key : keys) {
            paramBuffer.append(key).append(params.get(key) == null ? "" : params.get(key));
        }
        return paramBuffer.toString();
    }
}
