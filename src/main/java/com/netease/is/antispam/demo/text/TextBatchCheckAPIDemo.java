/*
 * @(#) TextCheckAPIDemo.java 2016年2月3日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 调用易盾反垃圾云服务文本批量在线检测接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 * @author hzgaomin
 * @version 2016年2月3日
 */
public class TextBatchCheckAPIDemo {
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
     * 易盾反垃圾云服务文本批量在线检测接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v3/text/batch-check";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v3.1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        JsonArray textArray = new JsonArray();
        JsonObject text1 = new JsonObject();
        text1.addProperty("dataId", "ebfcad1c-dba1-490c-b4de-e784c2691768");
        text1.addProperty("content", "易盾批量检测接口！v3接口!");
        // text1.addProperty("dataType", "1");
        // text1.addProperty("ip", "123.115.77.137");
        // text1.addProperty("account", "java@163.com");
        // text1.addProperty("deviceType", "4");
        // text1.addProperty("deviceId", "92B1E5AA-4C3D-4565-A8C2-86E297055088");
        // text1.addProperty("callback", "ebfcad1c-dba1-490c-b4de-e784c2691768");
        // text1.addProperty("publishTime", String.valueOf(System.currentTimeMillis()));
        // 主动回调地址url,如果设置了则走主动回调逻辑
        // text1.addProperty("callbackUrl", "http://***");
        textArray.add(text1);

        JsonObject text2 = new JsonObject();
        text2.addProperty("dataId", "ebfcad1c-dba1-490c-b4de-e784c2691767");
        text2.addProperty("content", "易盾批量检测接口！v3接口!");
        textArray.add(text2);

        params.put("texts", textArray.toString());
        params.put("checkLabels", "200, 500"); // 指定过检分类

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
            if (resultArray != null && resultArray.size() != 0) {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    String dataId = jObject.get("dataId").getAsString();
                    String taskId = jObject.get("taskId").getAsString();
                    int action = jObject.get("action").getAsInt();
                    int status = jObject.get("status").getAsInt();
                    System.out.println(String.format("dataId=%s，批量文本提交返回taskId:%s", dataId, taskId));
                    if (status == 0) {
                        JsonArray labelArray = jObject.getAsJsonArray("labels");
                        for (JsonElement labelElement : labelArray) {
                            JsonObject lObject = labelElement.getAsJsonObject();
                            int label = lObject.get("label").getAsInt();
                            int level = lObject.get("level").getAsInt();
                            JsonObject detailsObject = lObject.getAsJsonObject("details");
                            JsonArray hint = detailsObject.getAsJsonArray("hint");
                            JsonArray subLabels = lObject.getAsJsonArray("subLabels");
                        }
                        if (action == 0) {
                            System.out.println(String.format("taskId=%s，文本机器检测结果：通过", taskId));
                        } else if (action == 1) {
                            System.out.println(String.format("taskId=%s，文本机器检测结果：嫌疑，需人工复审，分类信息如下：%s", taskId, labelArray.toString()));
                        } else if (action == 2) {
                            System.out.println(String.format("taskId=%s，文本机器检测结果：不通过，分类信息如下：%s", taskId, labelArray.toString()));
                        }
                    } else if (status == 1) {
                        System.out.println("提交失败");
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
