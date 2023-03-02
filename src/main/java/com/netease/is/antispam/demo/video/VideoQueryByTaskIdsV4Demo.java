/*
 * @(#) TextCallbackAPIDemo.java 2016年12月28日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.video;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务点播视频结果查询接口API示例-v4版本
 *
 * @author yaoyi
 * @version 2022年6月20日
 */
public class VideoQueryByTaskIdsV4Demo {
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
     * 易盾反垃圾云服务点播查询检测结果获取接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v4/video/query/task";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v4");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 2.设置私有参数
        Set<String> taskIds = new HashSet<String>();
        taskIds.add("1b826d7e408d4b7fa7d3ddc9062f7994");
        taskIds.add("e7043c55c4d84948a14d9e89dc900ddf");
        params.put("taskIds", new Gson().toJson(taskIds));

        // 预处理参数
        params = Utils.pretreatmentParams(params);
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
            if (resultArray == null || resultArray.size() == 0) {
                System.out.println("暂无审核回调数据");
                return;
            }
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String taskId = jObject.get("taskId").getAsString();
                int status = jObject.get("status").getAsInt();
                // status任务状态， 0：正常，1：已过期，2：数据不存在，3：检测中
                if (status == 1) {
                    System.out.println(String.format("video callback taskId=%s，结果：数据已过期", taskId));
                } else if (status == 2) {
                    System.out.println(String.format("video callback taskId=%s，结果：数据不存在", taskId));
                } else if (status == 3) {
                    System.out.println(String.format("video callback taskId=%s，结果：数据检测中", taskId));
                } else {
                    JsonObject antispam = jObject.getAsJsonObject("antispam");
                    // 建议结果 0：通过，1：嫌疑，2：不通过
                    int suggestion = jObject.get("suggestion").getAsInt();
                    if (suggestion == 0) {
                        System.out.println(String.format("callback taskId=%s，结果：通过", taskId));
                    } else if (suggestion == 1 || suggestion == 2) {
                        JsonArray evidenceArray = jObject.get("pictures").getAsJsonArray();
                        for (JsonElement evidenceElement : evidenceArray) {
                            JsonObject eObject = evidenceElement.getAsJsonObject();
                            long startTime = eObject.get("startTime").getAsLong();
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
                            System.out.println(String.format("callback taskId=%s, 证据分类：%s", taskId, labelArray));
                        }
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
