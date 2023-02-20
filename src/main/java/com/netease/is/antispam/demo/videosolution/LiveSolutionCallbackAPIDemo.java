/*
 * @(#) LiveVideoCallbackAPIDemo.java 2016年8月1日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.videosolution;

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
 * 调用易盾反垃圾云服务直播电视墙离线结果获取接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求
 * 2.commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java 3. gson，用于做json解析
 *
 * @author yd-dev
 * @version 2020-04-22
 */
public class LiveSolutionCallbackAPIDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";

    /**
     * 易盾反垃圾云服务直播离线结果获取接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v3/livewallsolution/callback/results";
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
        params.put("version", "v3");
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
            if (null == resultArray || resultArray.size() == 0) {
                System.out.println("暂时没有结果需要获取，请稍后重试！");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject result = jsonElement.getAsJsonObject();
                    if (result.has("antispam")) {
                        JsonObject antispam = result.get("antispam").getAsJsonObject();
                        // 直播电视墙uuid
                        String taskId = antispam.get("taskId").getAsString();
                        // 数据id
                        String dataId = antispam.get("dataId").getAsString();
                        // 回调参数
                        String callback = antispam.get("callback").getAsString();
                        // 状态
                        int status = antispam.get("status").getAsInt();
                        int censorSource = antispam.get("censorSource").getAsInt();
                        int riskLevel = antispam.get("riskLevel").getAsInt();
                        int riskScore = antispam.get("riskScore").getAsInt();
                        long duration = antispam.has("duration") ? antispam.get("duration").getAsLong() : 0L;
                        System.out.println(String.format(
                                "taskId:%s, dataId:%s, 回调信息:%s, 状态:%s, 审核来源=%s, 风险等级%s, 风险评分%s, 时长 %s s",
                                taskId, dataId, callback, status, censorSource, riskLevel, riskScore,
                                duration));
                        if (antispam.has("evidences")) {
                            parseMachine(antispam.get("evidences").getAsJsonObject(), taskId);
                        } else if (antispam.has("reviewEvidences")) {
                            parseHuman(antispam.get("reviewEvidences").getAsJsonObject(), taskId);
                        } else {
                            System.out.println(String.format("Invalid Result: %s", antispam.toString()));
                        }
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
        JsonObject audio = evidences.has("audio") ? evidences.get("audio").getAsJsonObject() : null;
        JsonObject video = evidences.has("video") ? evidences.get("video").getAsJsonObject() : null;
        System.out.println(String.format("Machine Evidence audio=%s, video=", audio, video));
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
