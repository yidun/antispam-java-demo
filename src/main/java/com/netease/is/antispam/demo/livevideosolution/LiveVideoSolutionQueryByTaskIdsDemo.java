/*
 * @(#) LiveVideoSolutionQueryTaskIdsDemo.java 2021-09-03
 *
 * Copyright 2021 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.livevideosolution;

import java.util.*;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.*;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import com.netease.is.antispam.demo.utils.Utils;

/**
 * @author yidun
 * @version 2021-09-03
 */
public class LiveVideoSolutionQueryByTaskIdsDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾云服务直播音视频解决方案在线提交接口地址
     */
    private final static String API_URL = "http://as.dun.163yun.com/v1/livewallsolution/query/task";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        Set<String> taskIds = new HashSet<String>();
        taskIds.add("95b9496929a647d3be6bee74db639eab");
        taskIds.add("08c71667adff49b4bc916a2e2114081a");
        params.put("taskIds", new Gson().toJson(taskIds));

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
            JsonArray resultArr = resultObject.get("result").getAsJsonArray();
            for (JsonElement resultEle : resultArr) {
                JsonObject result = resultEle.getAsJsonObject();
                String taskId = Utils.getStringProperty(result, "taskId");
                Integer status = Utils.getIntegerProperty(result, "status");
                Integer callbackStatus = Utils.getIntegerProperty(result, "callbackStatus");
                System.out.println(
                        String.format("taskId=%s, status=%s, callbackStatus=%s", taskId, status, callbackStatus));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
