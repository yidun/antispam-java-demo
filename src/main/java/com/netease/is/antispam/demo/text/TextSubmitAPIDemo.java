/*
 * @(#) TextCallbackAPIDemo.java 2016年12月28日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;

/**
 * 调用易盾反垃圾云服务文本批量提交接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author hzgaomin
 * @version 2016年2月3日
 */
public class TextSubmitAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务文本离线检测结果获取接口地址 */
    private final static String API_URL = "http://as.dun.163.com/v1/text/submit";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

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
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.设置私有参数
        JsonArray textArray = new JsonArray();
        //dataId结构产品自行设计，用于唯一定位该文本数据
        JsonObject text1 = new JsonObject();
        text1.addProperty("dataId", "ebfcad1c-dba1-490c-b4de-e784c2691768");
        text1.addProperty("content", "易盾测试内容！v1接口!");
        text1.addProperty("action","0");
        textArray.add(text1);

        JsonObject text2 = new JsonObject();
        text2.addProperty("dataId", "ebfcad1c-dba1-490c-b4de-e784c2691767");
        text2.addProperty("content", "批量提交内容！v1接口!");
        text2.addProperty("action","1");
        textArray.add(text2);

        params.put("texts", textArray.toString());

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject resultObject = new JsonParser().parse(response).getAsJsonObject();
        int code = resultObject.get("code").getAsInt();
        String msg = resultObject.get("msg").getAsString();
        if (code == 200) {
            JsonArray resultArray = resultObject.getAsJsonArray("result");
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String dataId = jObject.get("dataId").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                System.out.println(String.format("dataId=%s，文本提交返回taskId:%s", dataId, taskId));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
