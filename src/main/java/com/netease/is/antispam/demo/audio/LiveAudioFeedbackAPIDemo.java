/*
 * @(#) LiveAudioFeedbackAPIDemo.java 2020-10-15
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.audio;

import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务更新直播音频信息接口API示例
 *
 * @author yd-dev
 * @version 2020-10-15
 */
public class LiveAudioFeedbackAPIDemo {
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
     * 易盾反垃圾云服务直播音频信息更新接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v1/liveaudio/feedback";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    public static void main(String[] args) {
        // 1. 设置公共参数
        Map<String, String> params = Utils.getCommonParams(SECRETID, BUSINESSID, "v1.0", "MD5");

        // 2.设置私有参数
        JsonObject feedback = new JsonObject();
        feedback.addProperty("taskId", "${validTaskId}");
        feedback.addProperty("status", 100);

        JsonArray feedbackArray = new JsonArray();
        feedbackArray.add(feedback);
        params.put("feedbacks", new Gson().toJson(feedbackArray));

        // 3.生成签名信息
        Utils.sign(params, SECRETKEY);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            JsonArray resultArray = jObject.get("result").getAsJsonArray();
            for (int i = 0; i < resultArray.size(); i++) {
                JsonObject result = resultArray.get(i).getAsJsonObject();
                String taskId = result.get("taskId").getAsString();
                int status = result.get("result").getAsInt();
                if (status == 0) {
                    System.out.println("SUCCESS, taskId=" + taskId);
                } else if (status == 2) {
                    System.out.println("NOT EXISTS, taskId=" + taskId);
                } else if (status == 1) {
                    System.out.println("SERVER ERROR, taskId=" + taskId);
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

}
