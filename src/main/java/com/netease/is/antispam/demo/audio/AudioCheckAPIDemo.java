/*
 * @(#) VideoSubmitAPIDemo.java 2016年8月23日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.audio;

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
 * 调用易盾反垃圾云服务点播语音在线检测接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 * @author maxiaofeng
 * @version 2021年01月05日
 */
public class AudioCheckAPIDemo {
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
     * 易盾反垃圾云服务音频信息提交接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v1/audio/check";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 100, 5000, 1000, 1000);

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v1.0");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        params.put("url", "http://xxx.xx");

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        JsonObject result = jObject.get("result").getAsJsonObject();
        if (code == 200) {
            String taskId = result.get("taskId").getAsString();
            int status = result.get("status").getAsInt();
            if (status == 0) {
                System.out.println(String.format("CHECK SUCCESS: taskId=%s", taskId));
                getAntispam(result);
                getLanguage(result);
                getAsr(result);
                getVoice(result);
            } else if (status == 1) {
                System.out.println(String.format("CHECK TIMEOUT: taskId=%s, status=%s", taskId, status));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

    private static void getAntispam(JsonObject result) {
        JsonArray antispamArray = result.getAsJsonArray("antispam");
        if (antispamArray == null || antispamArray.size() == 0) {
            System.out.println("暂无反垃圾检测数据");
        } else {
            for (JsonElement jsonElement : antispamArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String taskId = jObject.get("taskId").getAsString();
                int action = jObject.get("action").getAsInt();
                JsonArray labelArray = jObject.getAsJsonArray("labels");
                if (action == 0) {
                    System.out.println(String.format("taskId=%s，结果：通过", taskId));
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
                            String.format("uuid=%s，结果：不通过，分类信息如下：%s", taskId, labelArray.toString()));
                }
            }
        }
    }

    private static void getLanguage(JsonObject result) {
        JsonArray languageArray = result.getAsJsonArray("language");
        if (languageArray == null || languageArray.size() == 0) {
            System.out.println("暂无语种检测数据");
        } else {
            for (JsonElement jsonElement : languageArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String taskId = jObject.get("taskId").getAsString();
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

    private static void getAsr(JsonObject result) {
        JsonArray asrArray = result.getAsJsonArray("asr");
        if (asrArray == null || asrArray.size() == 0) {
            System.out.println("暂无语音翻译数据");
        } else {
            for (JsonElement jsonElement : asrArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String taskId = jObject.get("taskId").getAsString();
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

    private static void getVoice(JsonObject result) {
        JsonArray voiceArray = result.getAsJsonArray("voice");
        if (voiceArray == null || voiceArray.size() == 0) {
            System.out.println("暂无翻译数据");
        } else {
            for (JsonElement jsonElement : voiceArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String taskId = jObject.get("taskId").getAsString();
                String mainGender = jObject.get("mainGender").getAsString();
                System.out.println(String.format("taskId=%s，人声属性=%s", taskId, mainGender));
            }
        }
    }
}
