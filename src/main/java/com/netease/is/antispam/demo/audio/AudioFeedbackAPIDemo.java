/*
 * @(#) AudioQueryByTaskIdsDemo.java 2019-04-11
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.audio;

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
 * 调用易盾反垃圾云服务点播语音反馈接口API示例
 */
public class AudioFeedbackAPIDemo {
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
     * 易盾反垃圾云服务查询点播语音结果接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v1/audio/feedback";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
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
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        JsonArray feedbacks = new JsonArray();
        JsonObject feedback = new JsonObject();
        feedback.addProperty("taskId", "taskId");
        feedback.addProperty("level", 2);
        feedback.addProperty("label", 200);
        feedbacks.add(feedback);
        params.put("feedbacks", feedbacks.toString());
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
            JsonArray results = resultObject.getAsJsonArray("result");
            if (results == null || results.size() == 0) {
                System.out.println("反馈结果 = " + results);
            } else {
                for (JsonElement jsonElement : results) {
                    JsonObject result = jsonElement.getAsJsonObject();
                    String taskId = result.get("taskId").getAsString();
                    int resultCode = result.get("result").getAsInt();
                    System.out.printf("taskId=%s, result=%s%n", taskId, convertResultCode(resultCode));
                }
            }
        } else {
            System.out.printf("ERROR: code=%s, msg=%s%n", code, msg);
        }

    }

    private static String convertResultCode(int resultCode) {
        String desc = "";
        switch (resultCode) {
            case 0:
                desc = "成功";
                break;
            case 1:
                desc = "失败";
                break;
            case 2:
                desc = "数据不存在";
                break;
            default:
        }
        return desc;
    }

}
