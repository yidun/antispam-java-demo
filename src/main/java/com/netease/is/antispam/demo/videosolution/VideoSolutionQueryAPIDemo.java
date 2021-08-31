/*
 * @(#) LiveAudioCallbackAPIDemo.java 2019-04-11
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.videosolution;

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
 * 调用易盾反垃圾云服务获取点播音视频解决方案结果查询接口API示例
 *
 * @author maxiaofeng
 * @version 2020-06-23
 */
public class VideoSolutionQueryAPIDemo {
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
    private final static String API_URL = "http://as.dun.163.com/v1/videosolution/query/task";
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
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        Set<String> taskIds = new HashSet<String>();
        taskIds.add("202b1d65f5854cecadcb24382b681c1a");
        taskIds.add("0f0345933b05489c9b60635b0c8cc721");
        params.put("taskIds", new Gson().toJson(taskIds));

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
                System.out.println("暂时没有结果需要获取，请稍后重试！");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int status = jObject.get("status").getAsInt();
                    String taskId = jObject.get("taskId").getAsString();
                    if (status == 0) {
                        int result = jObject.get("result").getAsInt();
                        System.out.println(String.format("点播音视频, taskId:%s, 检测结果:%s", taskId, result));
                        if (jObject.has("evidences")) {
                            JsonObject evidences = jObject.get("evidences").getAsJsonObject();
                            if (evidences.has("text")) {
                                JsonObject text = evidences.get("text").getAsJsonObject();
                                System.out.println(String.format("文本信息, taskId:%s, 检测结果:%s",
                                        text.get("taskId").getAsString(), text.get("action").getAsInt()));

                            } else if (evidences.has("images")) {
                                JsonArray images = evidences.get("images").getAsJsonArray();
                                for (int i = 0; i < images.size(); i++) {
                                    JsonObject image = images.get(i).getAsJsonObject();
                                    System.out.println(
                                            String.format("图片信息, taskId:%s, 检测结果:%s", image.get("taskId").getAsString(),
                                                    image.get("labels").getAsJsonArray().toString()));
                                }
                            } else if (evidences.has("audio")) {
                                JsonObject audio = evidences.get("audio").getAsJsonObject();
                                System.out.println(String.format("语音信息, taskId:%s, 检测状态:%s, 检测结果:%s",
                                        audio.get("taskId").getAsString(), audio.get("asrStatus").getAsInt(),
                                        audio.get("action").getAsInt()));
                            } else if (evidences.has("video")) {
                                JsonObject video = evidences.get("video").getAsJsonObject();
                                System.out.println(String.format("视频信息, taskId:%s, 检测状态:%s, 检测结果:%s",
                                        video.get("taskId").getAsString(), video.get("status").getAsInt(),
                                        video.get("level").getAsInt()));
                            }
                        } else if (jObject.has("reviewEvidences")) {
                            JsonObject reviewEvidences = jObject.get("reviewEvidences").getAsJsonObject();
                            String reason = reviewEvidences.get("reason").getAsString();
                            JsonObject detail = reviewEvidences.get("detail").getAsJsonObject();
                            JsonArray text = detail.get("text").getAsJsonArray();
                            JsonArray image = detail.get("image").getAsJsonArray();
                            JsonArray audio = detail.get("audio").getAsJsonArray();
                            JsonArray video = detail.get("video").getAsJsonArray();
                            System.out.println(
                                    String.format("人审证据信息, 音视频taskId:%s, reason:%s, 文本:%s, 图片:%s, 语音:%s, 视频:%s", taskId,
                                            reason, text, image, audio, video));
                        }
                    } else if (status == 20) {
                        System.out.println(String.format("点播音视频, taskId:%s, 数据非7天内", taskId));
                    } else if (status == 30) {
                        System.out.println(String.format("点播音视频, taskId:%s, 数据不存在", taskId));
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

}
