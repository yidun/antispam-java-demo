/*
 * @(#) LiveAudioCallbackAPIDemo.java 2019-04-11
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
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务获取直播语音离线结果接口API示例
 *
 * @author yd-dev
 * @version 2020-04-22
 */
public class LiveAudioCallbackAPIDemo {
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
     * 易盾反垃圾云服务直播语音回调接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v4/liveaudio/callback/results";
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
        // 直播语音版本v2.1及以上二级细分类结构进行调整
        params.put("version", "v4");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // 加密方式可选 MD5, SM3, SHA1, SHA256
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
            JsonArray result = resultObject.getAsJsonArray("result");
            for (JsonElement resultEle : result) {
                JsonObject resultObj = resultEle.getAsJsonObject();
                if (resultObj.has("antispam")) {
                    getAntispam(resultObj.get("antispam").getAsJsonObject());
                }
                if (resultObj.has("asr")) {
                    getAsr(resultObj.get("asr").getAsJsonObject());
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

    private static void getAntispam(JsonObject antispam) {
        String taskId = antispam.get("taskId").getAsString();
        int status = antispam.get("status").getAsInt();
        // 断句审核证据信息
        JsonObject evidences = antispam.has("evidences") ? antispam.get("evidences").getAsJsonObject() : null;
        // 直播墙人审证据信息
        JsonObject reviewEvidences = antispam.has("reviewEvidences") ? antispam.get("reviewEvidences").getAsJsonObject()
                : null;
        System.out.println(String.format("taskId:%s, status:%s, evidences:%s, reviewEvidences:%s", taskId, status,
                evidences, reviewEvidences));
    }

    private static void getAsr(JsonObject asr) {
        String taskId = asr.get("taskId").getAsString();
        String content = asr.get("content").getAsString();
        long startTime = asr.get("startTime").getAsLong();
        long endTime = asr.get("endTime").getAsLong();
        System.out.println(String.format("taskId:%s, content:%s, startTime:%s, endTime:%s",
                taskId, content, startTime, endTime));
    }
}
