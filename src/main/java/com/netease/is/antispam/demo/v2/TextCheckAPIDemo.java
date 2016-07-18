/*
 * @(#) TextCheckAPIDemo.java 2016年2月3日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;

/**
 * 调用易盾反垃圾云服务文本在线检测接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author hzgaomin
 * @version 2016年2月3日
 */
public class TextCheckAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务文本在线检测接口地址 */
    private final static String API_URL = "https://api.aq.163.com/v2/text/check";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 1000, 1000, 1000);

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.设置私有参数
        params.put("dataId", "ebfcad1c-dba1-490c-b4de-e784c2691768");
        params.put("content", "易盾测试内容！");
        params.put("dataOpType", "1");
        params.put("ip", "123.115.77.137");

        params.put("dataType", "1");
        params.put("parentDataId", "334d42e1-112f-4fc7-8fb7-c60542fc2018");
        params.put("title", "易盾测试标题");
        params.put("url", "http://www.xx.com/xxx.html");
        params.put("account", "java@163.com");
        params.put("nickname", "没事瞎评论java");
        params.put("deviceType", "4");
        params.put("deviceId", "92B1E5AA-4C3D-4565-A8C2-86E297055088");
        params.put("callback", "ebfcad1c-dba1-490c-b4de-e784c2691768");
        params.put("publishTime", String.valueOf(System.currentTimeMillis()));

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject resultObject = jObject.getAsJsonObject("result");
            int action = resultObject.get("action").getAsInt();
            if (action == 1) {
                System.out.println("正常内容，通过");
            } else if (action == 2) {
                System.out.println("垃圾内容，删除");
            } else if (action == 3) {
                System.out.println("嫌疑内容");
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }

    }
}
