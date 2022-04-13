/*
 * @(#) LiveAudioQueryTaskAPIDemo.java 2020-01-08
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
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
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务查询直播语音片段离线结果接口API示例
 *
 * @author maxiaofeng
 * @version 2020-01-08
 */
public class LiveAudioQueryTaskAPIDemo {
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
     * 易盾反垃圾云服务图片在线检测接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v1/liveaudio/query/task";
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
        params.put("version", "v1.0");
        params.put("taskId", "xxx");
        params.put("startTime", "1578326400000");
        params.put("endTime", "1578327000000");// 最长支持查10分钟跨度
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
            JsonArray resultArray = resultObject.get("result").getAsJsonArray();
            if (resultArray.size() == 0) {
                System.out.println("没有结果");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    String taskId = jObject.get("taskId").getAsString();
                    int action = jObject.get("action").getAsInt();
                    int asrStatus = jObject.get("asrStatus").getAsInt();
                    int asrResult = jObject.get("asrResult").getAsInt();
                    String callback = jObject.has("callback") ? jObject.get("callback").getAsString() : "";
                    long startTime = jObject.get("startTime").getAsLong();
                    long endTime = jObject.get("endTime").getAsLong();
                    int censorSource = jObject.get("censorSource").getAsInt();
                    String speakerId = jObject.get("speakerId").getAsString();
                    String segmentId = jObject.get("segmentId").getAsString();
                    JsonArray segmentArray = jObject.getAsJsonArray("segments");
                    JsonArray recordsArray = jObject.getAsJsonArray("records");
                    if (action == 0) {
                        System.out.println(String.format(
                                "taskId=%s，结果：通过，语音识别状态 %s，语音识别结果 %s，回调信息 %s，时间区间【%s-%s】，审核类型 %s，说话人id %s，断句id %s，证据信息如下：%s, 记录信息如下：%s",
                                taskId, asrStatus, asrResult, callback, startTime, endTime, censorSource, speakerId,
                                segmentId, segmentArray.toString(), recordsArray));
                    } else if (action == 1 || action == 2) {
                        System.out.println(String.format("taskId=%s，结果：%s，时间区间【%s-%s】，证据信息如下：%s", taskId,
                                action == 1 ? "不确定" : "不通过", startTime, endTime, segmentArray.toString()));
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

}
