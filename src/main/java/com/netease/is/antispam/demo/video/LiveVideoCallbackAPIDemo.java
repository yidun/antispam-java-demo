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
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务直播离线结果获取接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 * @author hzgaomin
 * @version 2016年8月1日
 */
public class LiveVideoCallbackAPIDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 业务ID，易盾根据产品业务特点分配
     */
    private final static String BUSINESSID = "your_business_id";
    /**
     * 易盾反垃圾云服务直播离线结果获取接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v4/livevideo/callback/results";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 1000, 1000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v4");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 预处理参数
        params = Utils.pretreatmentParams(params);
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
            for (JsonElement jsonElement : resultArray) {
                JsonObject antispam = jsonElement.getAsJsonObject();
                String taskId = antispam.get("taskId").getAsString();
                String callback = Utils.getStringProperty(antispam, "callback");
                Integer callbackStatus = Utils.getIntegerProperty(antispam, "callbackStatus");
                Integer riskLevel = Utils.getIntegerProperty(antispam, "riskLevel");
                Integer riskScore = Utils.getIntegerProperty(antispam, "riskScore");
                Long duration = Utils.getLongProperty(antispam, "duration");
                System.out.println(String.format("taskId:%s, 回调信息:%s, 回调状态%s, 风险等级%s, 风险评分%s, 时长 %s s",
                        taskId, callback, callbackStatus, riskLevel, riskScore, duration));
                JsonObject evidenceObjec = antispam.get("evidence").getAsJsonObject();
                JsonArray labelArray = antispam.get("labels").getAsJsonArray();
                if (null != labelArray && labelArray.size() == 0) {// 检测正常
                    System.out.println(
                            String.format("正常, taskId:%s, callback=%s, 证据信息：%s", taskId, callback, evidenceObjec));
                } else {
                    for (JsonElement labelElement : labelArray) {
                        JsonObject lObject = labelElement.getAsJsonObject();
                        Integer label = Utils.getIntegerProperty(lObject, "label");
                        Integer level = lObject.get("level").getAsInt();
                        double rate = lObject.get("rate").getAsDouble();
                        System.out.println(
                                String.format("异常, taskId:%s, callback=%s, 分类：%s, 证据信息：%s", taskId, callback, lObject,
                                        evidenceObjec));
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
