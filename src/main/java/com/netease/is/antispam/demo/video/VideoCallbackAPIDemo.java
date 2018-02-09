/*
 * @(#) VideoCallbackAPIDemo.java 2016年8月23日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.video;

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
 * 调用易盾反垃圾云服务视频离线结果获取接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author hzdingyong
 * @version 2016年8月23日
 */
public class VideoCallbackAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务视频离线结果获取接口地址 */
    private final static String API_URL = "https://as.dun.163yun.com/v3/video/callback/results";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 1000, 1000);

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
        params.put("version", "v3.1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

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
                System.out.println("暂无回调数据");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int status = jObject.get("status").getAsInt();
                    if(status!=0){//异常，异常码定义见官网文档
                        System.out.println("视频异常，status="+status);
                        continue;
                    }
                    String callback = jObject.get("callback").getAsString();
                    int videoLevel = jObject.get("level").getAsInt();
                    if (videoLevel == 0) {
                        System.out.println(String.format("正常, callback=%s", callback));
                    } else if (videoLevel == 1 || videoLevel == 2) {
                        JsonArray evidenceArray = jObject.get("evidences").getAsJsonArray();
                        for (JsonElement evidenceElement : evidenceArray) {
                            JsonObject eObject = evidenceElement.getAsJsonObject();
                            long beginTime = eObject.get("beginTime").getAsLong();
                            long endTime = eObject.get("endTime").getAsLong();
                            int type = eObject.get("type").getAsInt();
                            String url = eObject.get("url").getAsString();

                            JsonArray labelArray = eObject.get("labels").getAsJsonArray();
                            for (JsonElement labelElement : labelArray) {
                                JsonObject lObject = labelElement.getAsJsonObject();
                                int label = lObject.get("label").getAsInt();
                                int level = lObject.get("level").getAsInt();
                                double rate = lObject.get("rate").getAsDouble();
                            }
                            System.out.println(String.format("%s, callback=%s, 证据信息：%s, 证据分类：%s, ", videoLevel == 1 ? "不确定"
                                            : "确定", callback, eObject, labelArray));
                        }
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
