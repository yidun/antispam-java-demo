/*
 * @(#) AudioQueryByTaskIdsDemo.java 2019-04-11
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.audio;

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

/**
 * 调用易盾反垃圾云服务查询点播语音结果接口API示例
 *
 * @author yd-dev
 * @version 2020-04-22
 */
public class AudioQueryByTaskIdsDemo {
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
     * 易盾反垃圾云服务查询点播语音结果接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v3/audio/query/task";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
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
        params.put("version", "v3");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        Set<String> taskIds = new HashSet<String>();
        taskIds.add("202b1d65f5854cecadcb24382b681c1a");
        taskIds.add("0f0345933b05489c9b60635b0c8cc721");
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
            JsonArray antispamArray = resultObject.getAsJsonArray("antispam");
            if (antispamArray == null || antispamArray.size() == 0) {
                System.out.println("暂无审核回调数据");
            } else {
                for (JsonElement jsonElement : antispamArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    String taskId = jObject.get("taskId").getAsString();
                    int status = jObject.get("status").getAsInt();
                    if (status == 30) {
                        System.out.println(String.format("antispam callback taskId=%s，结果：数据不存在", taskId));
                    } else {
                        int action = jObject.get("action").getAsInt();
                        JsonArray labelArray = jObject.getAsJsonArray("labels");
                        if (action == 0) {
                            System.out.println(String.format("callback taskId=%s，结果：通过", taskId));
                        } else if (action == 2) {
                            for (JsonElement labelInfo : labelArray) {
                                JsonObject lObject = labelInfo.getAsJsonObject();
                                int label = lObject.get("label").getAsInt();
                                int level = lObject.get("level").getAsInt();
                                JsonObject details = lObject.get("details").getAsJsonObject();
                                JsonArray hintArr = details.getAsJsonArray("hint");
                                // 二级细分类
                                JsonArray subLabels = lObject.get("subLabels").getAsJsonArray();
                            }
                            System.out.println(
                                    String.format("callback=%s，结果：不通过，分类信息如下：%s", taskId, labelArray.toString()));
                        }
                    }
                }
            }
            JsonArray languageArray = resultObject.getAsJsonArray("language");
            if (languageArray == null || languageArray.size() == 0) {
                System.out.println("暂无语种检测数据");
            } else {
                for (JsonElement jsonElement : languageArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int status = jObject.get("status").getAsInt();
                    String taskId = jObject.get("taskId").getAsString();
                    if (status == 30) {
                        System.out.println(String.format("language callback taskId=%s，结果：数据不存在", taskId));
                    } else {
                        JsonArray detailsArray = jObject.getAsJsonArray("details");
                        if (detailsArray != null && detailsArray.size() > 0) {
                            for (JsonElement details : detailsArray) {
                                JsonObject language = details.getAsJsonObject();
                                String type = language.get("type").getAsString();
                                JsonArray segmentsArray = language.getAsJsonArray("segments");
                                if (segmentsArray != null && segmentsArray.size() > 0) {
                                    for (JsonElement segmentObj : segmentsArray) {
                                        JsonObject segment = segmentObj.getAsJsonObject();
                                        System.out.println(String.format("taskId=%s，语种类型=%s，开始时间=%s秒，结束时间=%s秒", taskId,
                                                type, segment.get("startTime").getAsInt(),
                                                segment.get("endTime").getAsInt()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            JsonArray asrArray = resultObject.getAsJsonArray("asr");
            if (asrArray == null || asrArray.size() == 0) {
                System.out.println("暂无语音翻译数据");
            } else {
                for (JsonElement jsonElement : asrArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int status = jObject.get("status").getAsInt();
                    String taskId = jObject.get("taskId").getAsString();
                    if (status == 30) {
                        System.out.println(String.format("asr callback taskId=%s，结果：数据不存在", taskId));
                    } else {
                        JsonArray detailsArray = jObject.getAsJsonArray("details");
                        if (detailsArray != null && detailsArray.size() > 0) {
                            for (JsonElement details : detailsArray) {
                                JsonObject asr = details.getAsJsonObject();
                                int startTime = asr.get("startTime").getAsInt();
                                int endTime = asr.get("endTime").getAsInt();
                                String content = asr.get("content").getAsString();
                                System.out.println(String.format("taskId=%s，文字翻译结果=%s，开始时间=%s秒，结束时间=%s秒", taskId,
                                        content, startTime, endTime));
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }

    }

}
