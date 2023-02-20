/*
 * @(#) LiveAudioQueryMonitorAPIDemo.java 2020-09-28
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.audio;

import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务查询直播语音增值检测结果接口
 *
 * @author maxiaofeng
 * @version 2021年01月05日
 */
public class LiveAudioQueryExtraAPIDemo {

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
     * 易盾反垃圾云服务直播语音查询直播语音增值检测结果地址
     */
    private final static String API_URL = "https://as.dun.163.com/v1/liveaudio/query/extra";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    public static void main(String[] args) throws Exception {
        // 1. 设置公共参数
        Map<String, String> params = Utils.getCommonParams(SECRETID, BUSINESSID, "v1.0", "MD5");
        // 2. 设置私有参数
        params.put("taskId", "xxx");

        // 预处理参数
        params = Utils.pretreatmentParams(params);

        // 3. 生成签名信息
        Utils.sign(params, SECRETKEY);

        // 4. 发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5. 解析接口返回值
        JsonObject resultObject = new JsonParser().parse(response).getAsJsonObject();
        int code = resultObject.get("code").getAsInt();
        String msg = resultObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject result = resultObject.getAsJsonObject("result");
            JsonArray asrs = result.get("asr").getAsJsonArray();
            JsonArray languages = result.get("language").getAsJsonArray();
            if (asrs != null && asrs.size() > 0) {
                for (int i = 0; i < asrs.size(); i++) {
                    JsonObject asr = asrs.get(i).getAsJsonObject();
                    String taskId = asr.get("taskId").getAsString();
                    String content = asr.get("content").getAsString();
                    long startTime = asr.get("startTime").getAsLong();
                    long endTime = asr.get("endTime").getAsLong();
                    String speakerId = asr.get("speakerId").getAsString();
                    System.out.println(
                            String.format("语音识别检测结果：taskId=%s, speakerId=%s content=%s, startTime=%s, endTime=%s",
                                    taskId, speakerId, content, startTime, endTime));
                }
            }
            if (languages != null && languages.size() > 0) {
                for (int i = 0; i < languages.size(); i++) {
                    JsonObject language = asrs.get(i).getAsJsonObject();
                    String taskId = language.get("taskId").getAsString();
                    String content = language.get("content").getAsString();
                    String callback = language.get("callback").getAsString();
                    String segmentId = language.get("segmentId").getAsString();
                    long startTime = language.get("startTime").getAsLong();
                    long endTime = language.get("endTime").getAsLong();
                    System.out.println(String.format(
                            "语种检测结果：taskId=%s, content=%s, callback=%s, segmentId=%s, startTime=%s, endTime=%s",
                            taskId, content, callback, segmentId, startTime, endTime));
                }
            }
        } else {
            System.out.printf("ERROR: code=%s, msg=%s%n", code, msg);
        }
    }
}
