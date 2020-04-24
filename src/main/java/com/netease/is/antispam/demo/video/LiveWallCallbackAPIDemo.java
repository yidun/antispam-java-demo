/*
 * @(#) LiveVideoCallbackAPIDemo.java 2016年8月1日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.video;

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
 * 调用易盾反垃圾云服务直播电视墙离线结果获取接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2.commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author yd-dev
 * @version 2020-04-22
 */
public class LiveWallCallbackAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务直播离线结果获取接口地址 */
    private final static String API_URL = "https://as.dun.163yun.com/v3/livewall/callback/results";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 1000, 1000);

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
        params.put("version", "v3");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 3.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 4.解析接口返回值
        JsonObject resultObject = new JsonParser().parse(response).getAsJsonObject();
        int code = resultObject.get("code").getAsInt();
        String msg = resultObject.get("msg").getAsString();
        if (code == 200) {
            JsonArray resultArray = resultObject.getAsJsonArray("result");
            if (null == resultArray || resultArray.size() == 0) {
                System.out.println("暂时没有结果需要获取，请稍后重试！");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    // 直播电视墙uuid
                    String taskId = jObject.get("taskId").getAsString();
                    // 数据id
                    String dataId = jObject.get("dataId").getAsString();
                    // 回调参数
                    String callback = jObject.get("callback").getAsString();
                    // 状态
                    int status = jObject.get("status").getAsInt();
                    System.out.println(String.format("taskId:%s, dataId:%s, callback:%s, status:%s", taskId, dataId, callback, status));
                    if (jObject.has("evidences")) {
                        parseMachine(jObject.get("evidences").getAsJsonObject(), taskId);
                    } else if (jObject.has("reviewEvidences")) {
                        parseHuman(jObject.get("reviewEvidences").getAsJsonObject(), taskId);
                    } else {
                        System.out.println(String.format("Invalid Result: %s", jObject.toString()));
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

    /**
     * 机审信息
     *
     * @param evidences
     * @param taskId
     */
    private static void parseMachine(JsonObject evidences, String taskId) {
        System.out.println("=== 机审信息 ===");
        JsonObject evidence = evidences.get("evidence").getAsJsonObject();
        JsonArray labels = evidences.get("labels").getAsJsonArray();

        int type = evidence.get("type").getAsInt();
        String url = evidence.get("url").getAsString();
        long beginTime = evidence.get("beginTime").getAsLong();
        long endTime = evidence.get("endTime").getAsLong();

        for (JsonElement jsonElement : labels) {
            JsonObject callbackImageLabel = jsonElement.getAsJsonObject();
            int label = callbackImageLabel.get("label").getAsInt();
            int level = callbackImageLabel.get("level").getAsInt();
            float rate = callbackImageLabel.get("rate").getAsFloat();
            JsonArray subLabels = callbackImageLabel.get("subLabels").getAsJsonArray();
        }

        System.out.println(String.format("Machine Evidence: %s", evidence.toString()));
        System.out.println(String.format("Machine Labels: %s", labels.toString()));
        System.out.println("================");
    }

    /**
     * 人审信息
     *
     * @param reviewEvidences
     * @param taskId
     */
    private static void parseHuman(JsonObject reviewEvidences, String taskId) {
        System.out.println("=== 人审信息 ===");
        // 操作
        int action = reviewEvidences.get("action").getAsInt();
        long actionTime = reviewEvidences.get("actionTime").getAsLong();
        int label = reviewEvidences.get("label").getAsInt();
        String detail = reviewEvidences.get("detail").getAsString();
        int warnCount = reviewEvidences.get("warnCount").getAsInt();
        JsonArray evidence = reviewEvidences.get("evidence").getAsJsonArray();
        if (action == 2) {
            // 警告
            System.out.println(
                    String.format("警告,taskId:%s, 总警告次数:%s, 证据信息：%s", taskId, warnCount, evidence.toString()));
        } else if (action == 3) {
            // 断流
            System.out.println(
                    String.format("断流, taskId:%s, 总警告次数:%s, 证据信息：%s", taskId, warnCount, evidence.toString()));
        } else {
            System.out.println(String.format("人审信息: %s", reviewEvidences.toString()));
        }
        System.out.println("================");
    }
}
