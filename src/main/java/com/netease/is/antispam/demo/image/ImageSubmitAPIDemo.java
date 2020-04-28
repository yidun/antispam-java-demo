/*
 * @(#) TextCallbackAPIDemo.java 2016年12月28日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.image;

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
 * 调用易盾反垃圾云服务图片批量提交接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author hzgaomin
 * @version 2019年11月28日
 */
public class ImageSubmitAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务图片离线检测结果获取接口地址 */
    private final static String API_URL = "http://as.dun.163yun.com/v1/image/submit";
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
        params.put("businessId", BUSINESSID);
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.设置私有参数
        JsonArray imageArray = new JsonArray();
        // dataId结构产品自行设计，用于唯一定位该图片数据
        JsonObject image1 = new JsonObject();
        image1.addProperty("name", "image1");
        image1.addProperty("data", "https://nos.netease.com/yidun/2-0-0-a6133509763d4d6eac881a58f1791976.jpg");
        image1.addProperty("level", "2");
        // image1.addProperty("ip", "127.0.0.1");
        // image1.addProperty("account", "account");
        // image1.addProperty("deviceId", "deviceId");
        // 审核后是否需要主动回调，需要则设置主动回调url
        // image1.addProperty("callbackUrl", "http://####");
        imageArray.add(image1);

        JsonObject image2 = new JsonObject();
        image2.addProperty("name", "image2");
        image2.addProperty("data",
                "http://dun.163.com/public/res/web/case/sexy_normal_2.jpg?dda0e793c500818028fc14f20f6b492a");
        image2.addProperty("level", "0");
        // image2.addProperty("ip", "127.0.0.1");
        // image2.addProperty("account", "account");
        // image2.addProperty("deviceId", "deviceId");
        // 审核后是否需要主动回调，需要则设置主动回调url
        // image2.addProperty("callbackUrl", "http://####");
        imageArray.add(image2);
        params.put("images", imageArray.toString());

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
            JsonArray resultArray = resultObject.getAsJsonArray("result");
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String name = jObject.get("name").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                System.out.println(String.format("图片提交返回name=%s，taskId:%s", name, taskId));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
