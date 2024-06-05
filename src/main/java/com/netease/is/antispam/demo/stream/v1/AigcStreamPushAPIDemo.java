/*
 * @(#) MediaSolutionSubmitAPIDemo.java 2020-06-23
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.stream.v1;

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
 * 调用易盾反垃圾AIGC流式检测解决方案检测提交接口API示例
 *
 * @author ruicha
 * @version 2024-06-05
 */
public class AigcStreamPushAPIDemo {

    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾AIGC流式检测解决方案在线检测接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v1/stream/push";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    public static void main(String[] args) {

        String sessionId = "yourSessionId" + System.currentTimeMillis();

        // 输入会话检测 demo
        pushDemoForInputCheck(sessionId);

        // 输出会话流式检测 demo 
        // pushDemoForOutputStreamCheck(sessionId);

        // 输出会话完毕后关闭会话 demo
        // pushDemoForOutputStreamClose(sessionId);


    }

    private static void pushDemoForOutputStreamClose(String sessionId) {
        // type = 3：会话结束，content不需要传，如果传了则合并后一起检测
        // 1.设置公共参数
        Map<String, String> params = prepareParams();
        // 2.设置业务参数
        params.put("sessionId", sessionId);
        params.put("type", "3");
        invokeAndParseResponse(params);
    }

    private static void pushDemoForOutputStreamCheck(String sessionId) {
        // type = 1：content必传，流式检测内容片段，可对应aigc场景流式输出的tokens，检测片段传入AIGC-输出文本中，最大长度200
        // 1.设置公共参数
        Map<String, String> params = prepareParams();
        // 2.设置业务参数
        params.put("sessionId", sessionId);
        params.put("type", "1");
        params.put("dataId", "yourDataId");
        params.put("content", "当前输出片段1");
        params.put("publishTime", String.valueOf(System.currentTimeMillis()));
        invokeAndParseResponse(params);
    }

    private static void pushDemoForInputCheck(String sessionId) {
        // type = 2：content必传，流式检测场景下输入内容，建议对内容中json、表情符、HTML标签、UBB标签等做过滤，只传递纯文本，以减少误判概率，对应传入AIGC-输入文本中，最大长度10000
        // 1.设置公共参数
        Map<String, String> params = prepareParams();
        // 2.设置业务参数
        params.put("sessionId", sessionId);
        params.put("type", "2");
        params.put("dataId", "yourDataId");
        params.put("content", "当前会话输入的内容");
        params.put("publishTime", String.valueOf(System.currentTimeMillis()));
        // 预处理参数
        invokeAndParseResponse(params);
    }

    private static Map<String, String> prepareParams() {
        Map<String, String> params = new HashMap<>(16);
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");
        return params;
    }

    private static void invokeAndParseResponse(Map<String, String> params) {
        // 预处理参数
        params = Utils.pretreatmentParams(params);
        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == DemoConstants.SUCCESS_CODE) {
            JsonObject streamCheckResult = jObject.getAsJsonObject("result");
            if (streamCheckResult != null) {
                // doSomething
                // sessionTaskId
                String sessionTaskId = Utils.getStringProperty(streamCheckResult.getAsJsonObject(), "sessionTaskId");
                // sessionId
                String sessionIdReturn = Utils.getStringProperty(streamCheckResult.getAsJsonObject(), "sessionId");
                JsonObject antispam = streamCheckResult.getAsJsonObject().getAsJsonObject("antispam");
                System.out.printf("sessionTaskId=%s, sessionId=%s, antispam=%s", sessionTaskId, sessionIdReturn, antispam);
            }
        } else {
            System.out.printf("ERROR: code=%s, msg=%s%n", code, msg);
        }
    }
}
