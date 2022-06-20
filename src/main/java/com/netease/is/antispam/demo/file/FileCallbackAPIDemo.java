/*
 * @(#) FileCallbackResultsApiDemo.java 2019-04-01
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.file;

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
 * FileCallbackResultsApiDemo
 *
 * @author jinxiaotian01
 * @version 19-4-1
 */
public class FileCallbackAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 易盾反垃圾云服务文档解决方案检测结果获取接口地址 */
    private final static String API_URL = "http://as-file.dun.163.com/v1/file/callback/results";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
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
        params.put("version", "v1.1");
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
            if (resultArray == null) {
                System.out.println("Can't find Callback Data");
                return;
            }
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String dataId = jObject.get("dataId").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                int result = jObject.get("result").getAsInt();
                String callback = !jObject.has("callback") ? "" : jObject.get("callback").getAsString();
                JsonObject evidencesObject = jObject.get("evidences").getAsJsonObject();

                System.out.println(String.format("SUCCESS: dataId=%s, taskId=%s, result=%s, callback=%s, evidences=%s",
                        dataId, taskId, result, callback, evidencesObject));
            }

        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
