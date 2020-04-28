/*
 * @(#) TextCallbackAPIDemo.java 2016年12月28日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.video;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;

import java.util.Map;
import java.util.Random;


/**
 * 调用易盾反垃圾云服务直播视频截图结果查询接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author wangmiao5
 * @version 2020年04月28日
 */
public class LiveDataQueryByTaskIdDemo {

    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务直播视频截图结果获取接口地址 */
    private final static String API_URL = "http://as.dun.163yun.com/v1/livevideo/query/image";
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
        params.put("version", "v1.1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.设置私有参数
        params.put("taskId", "c633a8cb6d45497c9f4e7bd6d8218443");
        params.put("levels", "[1,2]");
        params.put("callbackStatus", "1");
        params.put("pageNum", "1");
        params.put("pageSize", "10");

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
            JsonObject result = resultObject.getAsJsonObject("result");
            int status = result.get("status").getAsInt();
            JsonObject images = result.get("images").getAsJsonObject();
            int count = images.get("count").getAsInt();
            JsonArray rows = images.get("rows").getAsJsonArray();
            if (status == 0) {
                for (JsonElement rowElement : rows) {
                    JsonObject row = rowElement.getAsJsonObject();
                    String url = row.get("url").getAsString();
                    int label = row.get("label").getAsInt();
                    int labelLevel = row.get("labelLevel").getAsInt();
                    int callbackStatus = row.get("callbackStatus").getAsInt();
                    long beginTime = row.get("beginTime").getAsLong();
                    long endTime = row.get("endTime").getAsLong();
                }
                System.out.println(String.format("live data query success, images: %s", rows));
            } else if (status == 20) {
                System.out.println("taskId is expired");
            } else if (status == 30) {
                System.out.println("taskId is not exist");
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
