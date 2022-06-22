/*
 * @(#) LiveAudioCallbackV2APIDemo.java 2022-06-20
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.videosolution;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import com.netease.is.antispam.demo.utils.Utils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 调用易盾反垃圾云服务获取点播音视频解决方案结果查询接口API示例-v2版本
 *
 * @author yaoyi
 * @version 2022-06-20
 */
public class VideoSolutionQueryV2APIDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾云服务点播音视频解决方案离线结果获取接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v2/videosolution/query/task";
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
        params.put("version", "v2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        Set<String> taskIds = new HashSet<String>();
        taskIds.add("aa2f542eb7854d78a1906021aab2890d");
        taskIds.add("3718c32ab21b4ed78a29e2e9a44bb7cb");
        params.put("taskIds", new Gson().toJson(taskIds));

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
            if (resultArray == null || resultArray.size() == 0) {
                System.out.println("暂无审核回调数据");
                return;
            }
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String taskId = jObject.get("taskId").getAsString();
                // status任务状态， 0：正常，1：已过期，2：数据不存在，3：检测中
                int status = jObject.get("status").getAsInt();
                if (status == 1) {
                    System.out.println(String.format("callback taskId=%s，结果：数据已过期", taskId));
                } else if (status == 2) {
                    System.out.println(String.format("callback taskId=%s，结果：数据不存在", taskId));
                } else if (status == 3) {
                    System.out.println(String.format("callback taskId=%s，结果：数据检测中", taskId));
                } else {
                    // 解析反垃圾检测结果
                    parseAntispam(jObject, taskId);
                    // 解析语种检测数据
                    parseLanguage(jObject, taskId);
                    // 解析语音翻译数据
                    parseAsr(jObject, taskId);
                    // 解析人声属性检测结果
                    parseVoice(jObject, taskId);
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

    private static void parseVoice(JsonObject jObject, String taskId) {
        JsonObject voice = jObject.getAsJsonObject("voice");
        if (voice == null) {
            System.out.println("暂无人声属性检测数据");
        } else {
            JsonObject detail = voice.getAsJsonObject("detail");
            String mainGender = detail.get("mainGender").getAsString();
            System.out.println(String.format("taskId=%s，人声属性检测结果=%s", taskId, mainGender));
        }
    }

    private static void parseAsr(JsonObject jObject, String taskId) {
        JsonObject asr = jObject.getAsJsonObject("asr");
        if (asr == null) {
            System.out.println("暂无语音翻译数据");
        } else {
            JsonArray detailsArray = asr.getAsJsonArray("details");
            if (detailsArray != null && detailsArray.size() > 0) {
                for (JsonElement details : detailsArray) {
                    JsonObject detail = details.getAsJsonObject();
                    int startTime = detail.get("startTime").getAsInt();
                    int endTime = detail.get("endTime").getAsInt();
                    String content = detail.get("content").getAsString();
                    System.out.println(String.format("taskId=%s，文字翻译结果=%s，开始时间=%s秒，结束时间=%s秒", taskId,
                            content, startTime, endTime));
                }
            }
        }
    }

    private static void parseLanguage(JsonObject jObject, String taskId) {
        JsonObject language = jObject.getAsJsonObject("language");
        if (language == null) {
            System.out.println("暂无语种检测数据");
        } else {
            JsonArray detailsArray = language.getAsJsonArray("details");
            if (detailsArray != null && detailsArray.size() > 0) {
                for (JsonElement details : detailsArray) {
                    JsonObject detail = details.getAsJsonObject();
                    String type = detail.get("type").getAsString();
                    JsonArray segmentsArray = detail.getAsJsonArray("segments");
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

    private static void parseAntispam(JsonObject jObject, String taskId) {
        JsonObject antispam = jObject.get("antispam").getAsJsonObject();
        // 建议结果 0-通过 1-嫌疑 2-不通过
        int suggestion = antispam.get("suggestion").getAsInt();
        if (suggestion == 0) {
            System.out.println(String.format("callback taskId=%s，结果：通过", taskId));
        } else if (suggestion == 1 || suggestion == 2) {
            // 解析机审结果
            if (antispam.has("evidences")) {
                JsonObject evidences = antispam.get("evidences").getAsJsonObject();
                // 文本检测信息
                if (evidences.has("text")) {
                    JsonObject text = evidences.get("text").getAsJsonObject();
                    System.out.println(String.format("文本信息, taskId:%s, 检测结果:%s",
                            taskId, text.get("suggestion").getAsInt()));
                }
                // 图片检测结果
                if (evidences.has("images")) {
                    JsonArray images = evidences.get("images").getAsJsonArray();
                    for (int i = 0; i < images.size(); i++) {
                        JsonObject image = images.get(i).getAsJsonObject();
                        System.out.println(
                                String.format("图片信息, taskId:%s, 检测结果:%s",
                                        taskId, image.get("labels").getAsJsonArray().toString()));
                    }
                }
                // 音频检测结果
                if (evidences.has("audio")) {
                    JsonObject audio = evidences.get("audio").getAsJsonObject();
                    System.out.println(String.format("语音信息, taskId:%s, 检测状态:%s, 检测结果:%s",
                            taskId, audio.get("status").getAsInt(), audio.get("suggestion").getAsInt()));
                }
                if (evidences.has("video")) {
                    JsonObject video = evidences.get("video").getAsJsonObject();
                    System.out.println(String.format("视频信息, taskId:%s, 检测状态:%s, 检测结果:%s",
                            taskId, video.get("status").getAsInt(), video.get("suggestion").getAsInt()));
                }
            }
            // 解析人审结果
            if (antispam.has("reviewEvidences")) {
                JsonObject reviewEvidences = antispam.get("reviewEvidences").getAsJsonObject();
                String spamType = reviewEvidences.get("spamType").getAsString();
                JsonArray text = reviewEvidences.get("texts").getAsJsonArray();
                JsonArray image = reviewEvidences.get("images").getAsJsonArray();
                JsonArray audio = reviewEvidences.get("audios").getAsJsonArray();
                JsonArray video = reviewEvidences.get("videos").getAsJsonArray();
                System.out.println(
                        String.format("人审证据信息, 音视频taskId:%s, 人审垃圾类型:%s, 文本:%s, 图片:%s, 语音:%s, 视频:%s", taskId,
                                spamType, text, image, audio, video));
            }
        }
    }

}
