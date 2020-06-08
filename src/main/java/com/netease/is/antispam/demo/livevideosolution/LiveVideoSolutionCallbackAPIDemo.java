/*
 * @(#) LiveVideoSolutionCallbackAPIDemo.java 2019-11-28
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.livevideosolution;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 调用易盾反垃圾云服务获取直播音视频解决方案离线结果接口API示例
 *
 * @author yd-dev
 * @version 2020-04-22
 */
public class LiveVideoSolutionCallbackAPIDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾云服务点播音视频解决方案离线结果获取接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v2/livewallsolution/callback/results";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
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
        params.put("version", "v2");
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
                    String taskId = jObject.get("taskId").getAsString();
                    String callback = jObject.get("callback").getAsString();
                    String dataId = jObject.get("dataId").getAsString();
                    int status = jObject.get("status").getAsInt();
                    System.out.println(String.format("taskId:%s, callback:%s, dataId:%s, status:%s", taskId, callback, dataId, status));
                    if (jObject.has("evidences")) {
                        JsonObject evidences = jObject.get("evidences").getAsJsonObject();
                        if (evidences.has("audio")) {
                            parseAudioEvidence(evidences.get("audio").getAsJsonObject(), taskId);
                        } else if (evidences.has("video")) {
                            parseVideoEvidence(evidences.get("video").getAsJsonObject(), taskId);
                        } else {
                            System.out.println(String.format("Invalid Evidence: %s", evidences.toString()));
                        }
                    } else if (jObject.has("reviewEvidences")) {
                        parseHumanEvidence(jObject.get("reviewEvidences").getAsJsonObject(), taskId);
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
     * 音频机审信息
     * @param audioEvidence
     */
    private static void parseAudioEvidence(JsonObject audioEvidence, String taskId) {
        System.out.println("=== 音频机审信息 ===");
        int asrStatus = audioEvidence.get("asrStatus").getAsInt();
        long startTime = audioEvidence.get("startTime").getAsLong();
        long endTime = audioEvidence.get("endTime").getAsLong();
        if (asrStatus == 4) {
            int asrResult = audioEvidence.get("asrResult").getAsInt();
            System.out.println(String.format("检测失败: taskId=%s, asrResult=%s", taskId, asrResult));
        } else {
            int action = audioEvidence.get("action").getAsInt();
            JsonArray segmentArray = audioEvidence.getAsJsonArray("segments");
            if (action == 0) {
                System.out.println(String.format("taskId=%s，结果：通过，时间区间【%s-%s】，证据信息如下：%s", taskId, startTime,
                        endTime, segmentArray.toString()));
            } else if (action == 1 || action == 2) {
                for (JsonElement labelElement : segmentArray) {
                    JsonObject lObject = labelElement.getAsJsonObject();
                    int label = lObject.get("label").getAsInt();
                    int level = lObject.get("level").getAsInt();
                    String evidence = lObject.get("evidence").getAsString();
                }
                System.out.println(String.format("taskId=%s，结果：%s，时间区间【%s-%s】，证据信息如下：%s", taskId,
                        action == 1 ? "不确定" : "不通过", startTime, endTime, segmentArray.toString()));
            }
        }
        System.out.println("================");
    }

    /**
     * 视频计审信息
     * @param videoEvidence
     */
    private static void parseVideoEvidence(JsonObject videoEvidence, String taskId) {
        System.out.println("=== 视频机审信息 ===");
        JsonObject evidence = videoEvidence.get("evidence").getAsJsonObject();
        JsonArray labels = videoEvidence.get("labels").getAsJsonArray();

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
     * @param humanEvidence
     */
    private static void parseHumanEvidence(JsonObject humanEvidence, String taskId) {
        System.out.println("=== 人审信息 ===");
        // 操作
        int action = humanEvidence.get("action").getAsInt();
        // 判断时间点
        long actionTime = humanEvidence.get("actionTime").getAsLong();
        // 违规类型
        int label = humanEvidence.get("label").getAsInt();
        // 违规详情
        String detail = humanEvidence.get("detail").getAsString();
        // 警告次数
        int warnCount = humanEvidence.get("warnCount").getAsInt();
        // 证据信息
        JsonArray evidence = humanEvidence.get("evidence").getAsJsonArray();

        if (action == 2) {
            // 警告
            System.out.println(String.format("警告, taskId:%s, 警告次数:%s, 违规详情:%s, 证据信息:%s", taskId, warnCount, detail, evidence.toString()));
        } else if (action == 3) {
            // 断流
            System.out.println(String.format("断流, taskId:%s, 警告次数:%s, 违规详情:%s, 证据信息:%s", taskId, warnCount, detail, evidence.toString()));
        } else {
            System.out.println(String.format("人审信息：%s", humanEvidence.toString()));
        }
        System.out.println("================");
    }
}
