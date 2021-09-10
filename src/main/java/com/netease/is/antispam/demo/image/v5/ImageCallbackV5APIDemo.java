/*
 * @(#) ImageCallbackAPIDemo.java 2016年3月15日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.image.v5;

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

/**
 * 调用易盾反垃圾云服务图片离线检测结果获取接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2.
 * commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java 3. gson，用于做json解析
 *
 * @author hzgaomin
 * @version 2016年2月3日
 */
public class ImageCallbackV5APIDemo {
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
     * 易盾反垃圾云服务图片离线检测结果获取接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v5/image/callback/results";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v5");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

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
            if (resultArray.size() == 0) {
                System.out.println("暂时没有图片人工复审结果需要获取，请稍后重试！");
            }
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                JsonObject antispam = jObject.get("antispam").getAsJsonObject();
                String name = antispam.get("name").getAsString();
                String taskId = antispam.get("taskId").getAsString();
                int suggestion = antispam.get("suggestion").getAsInt();
                JsonArray labelArray = antispam.get("labels").getAsJsonArray();
                System.out.println(String.format("taskId=%s，name=%s，action=%s", taskId, name, suggestion));
                // 产品需根据自身需求，自行解析处理，本示例只是简单判断分类级别
                for (JsonElement labelElement : labelArray) {
                    JsonObject lObject = labelElement.getAsJsonObject();
                    int label = lObject.get("label").getAsInt();
                    int level = lObject.get("level").getAsInt();
                    double rate = lObject.get("rate").getAsDouble();
                    System.out.println(String.format("label:%s, level=%s, rate=%s", label, level, rate));
                }
                switch (suggestion) {
                    case 0:
                        System.out.println("#图片机器异步检测/人工复审结果：最高等级为\"正常\"\n");
                        break;
                    case 2:
                        System.out.println("#图片机器异步检测/人工复审结果：最高等级为\"确定\"\n");
                        break;
                    default:
                        break;
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
