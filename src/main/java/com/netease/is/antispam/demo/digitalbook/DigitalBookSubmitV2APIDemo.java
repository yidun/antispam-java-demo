/*
 * @(#) DigitalBookSubmitV2APIDemo.java 2021-11-01
 *
 * Copyright 2021 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.digitalbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.DemoConstants;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 数字阅读解决方案检测提交接口API示例-v2版本
 *
 * @author spring404
 * @version 2021-11-01
 */
public class DigitalBookSubmitV2APIDemo {

    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 数字阅读解决方案提交接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v2/digital/submit";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);


    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>(16);
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 2.设置私有参数
        params.put("bookId", "book0001");
        params.put("type", "1");
        params.put("callback", "i am callback");

        //作品信息
        JsonArray jsonArray = new JsonArray();
        JsonObject text = new JsonObject();
        // 作品文本内容
        text.addProperty("type", "text");
        text.addProperty("data", "文章文本内容");
        text.addProperty("dataId", "0001");
        jsonArray.add(text);
        // 作品图片内容
        JsonObject image = new JsonObject();
        image.addProperty("type", "image");
        image.addProperty("data", "http://xxx.jpg");
        image.addProperty("dataId", "0002");
        jsonArray.add(image);
        params.put("bookInformation", jsonArray.toString());

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
            JsonObject result = jObject.getAsJsonObject("result").getAsJsonObject("antispam");
            String taskId = result.get("taskId").getAsString();
            String dataId = result.has("dataId") ? result.get("dataId").getAsString() : "";
            System.out.printf("SUBMIT SUCCESS: taskId=%s, dataId=%s%n", taskId, dataId);
        } else {
            System.out.printf("ERROR: code=%s, msg=%s%n", code, msg);
        }
    }
}
