/*
 * @(#) MediaSolutionCallbackAPIDemo.java 2020-06-23
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.stream.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.DemoConstants;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import com.netease.is.antispam.demo.utils.Utils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 调用易盾反垃圾云服务获取AIGC流式检测解决方案离线结果接口API示例
 *
 * @author ruicha
 * @version 2024-06-05
 */
public class AigcStreamCallbackAPIDemo {

    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾云服务AIGC流式检测解决方案离线结果获取接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v1/stream/callback/results";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>(16);
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v1");
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
        if (code == DemoConstants.SUCCESS_CODE) {
            JsonArray resultArray = resultObject.getAsJsonArray("result");
            if (null == resultArray || resultArray.size() == 0) {
                System.out.println("暂时没有结果需要获取，请稍后重试！");
            } else {
                for (JsonElement streamCheckResult : resultArray) {
                    // sessionTaskId
                    String sessionTaskId = Utils.getStringProperty(streamCheckResult.getAsJsonObject(), "sessionTaskId");
                    // sessionId
                    String sessionId = Utils.getStringProperty(streamCheckResult.getAsJsonObject(), "sessionId");
                    JsonObject antispam = streamCheckResult.getAsJsonObject().getAsJsonObject("antispam");
                    // antispam.suggestion
                    String suggestion = Utils.getStringProperty(antispam, "suggestion");
                    // antispam.label
                    String label = Utils.getStringProperty(antispam, "label");
                    System.out.printf("sessionTaskId=%s, sessionId=%s, suggestion=%s, label=%s%n",
                            sessionTaskId, sessionId, suggestion, label);
                }
            }
        } else {
            System.out.printf("ERROR: code=%s, msg=%s%n", code, msg);
        }
    }
}
