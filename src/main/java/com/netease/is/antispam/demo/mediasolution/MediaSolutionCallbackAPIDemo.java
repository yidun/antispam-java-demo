/*
 * @(#) MediaSolutionCallbackAPIDemo.java 2020-06-23
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.mediasolution;

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
 * 调用易盾反垃圾云服务获取融媒体解决方案离线结果接口API示例
 *
 * @author maxiaofeng
 * @version 2020-06-23
 */
public class MediaSolutionCallbackAPIDemo {

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
    private final static String API_URL = "http://as.dun.163.com/v1/mediasolution/callback/results";
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
        params.put("version", "v1");
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
            if (null == resultArray || resultArray.size() == 0) {
                System.out.println("暂时没有结果需要获取，请稍后重试！");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    String taskId = jObject.get("taskId").getAsString();
                    String callback = jObject.get("callback").getAsString();
                    String dataId = jObject.get("dataId").getAsString();
                    int checkStatus = jObject.get("checkStatus").getAsInt();
                    int result = jObject.get("result").getAsInt();
                    System.out.println(String.format("taskId:%s, callback:%s, dataId:%s, 检测状态:%s, 检测结果:%s", taskId,
                            callback, dataId, checkStatus, result));
                    if (jObject.has("evidences")) {
                        JsonObject evidences = jObject.get("evidences").getAsJsonObject();
                        if (evidences.has("texts")) {
                            JsonArray texts = evidences.get("texts").getAsJsonArray();
                            for (int i = 0; i < texts.size(); i++) {
                                JsonObject text = texts.get(i).getAsJsonObject();
                                System.out.println(String.format("文本信息, dataId:%s, 检测结果:%s",
                                        text.get("dataId").getAsString(), text.get("action").getAsInt()));
                            }
                        } else if (evidences.has("images")) {
                            JsonArray images = evidences.get("images").getAsJsonArray();
                            for (int i = 0; i < images.size(); i++) {
                                JsonObject image = images.get(i).getAsJsonObject();
                                System.out.println(String.format("图片信息, dataId:%s, 检测状态:%s, 检测结果:%s",
                                        image.get("dataId").getAsString(), image.get("status").getAsInt(),
                                        image.get("action").getAsInt()));
                            }
                        } else if (evidences.has("audios")) {
                            JsonArray audios = evidences.get("audios").getAsJsonArray();
                            for (int i = 0; i < audios.size(); i++) {
                                JsonObject audio = audios.get(i).getAsJsonObject();
                                System.out.println(String.format("语音信息, dataId:%s, 检测状态:%s, 检测结果:%s",
                                        audio.get("dataId").getAsString(), audio.get("asrStatus").getAsInt(),
                                        audio.get("action").getAsInt()));
                            }
                        } else if (evidences.has("videos")) {
                            JsonArray videos = evidences.get("videos").getAsJsonArray();
                            for (int i = 0; i < videos.size(); i++) {
                                JsonObject video = videos.get(i).getAsJsonObject();
                                System.out.println(String.format("视频信息, dataId:%s, 检测状态:%s, 检测结果:%s",
                                        video.get("dataId").getAsString(), video.get("status").getAsInt(),
                                        video.get("level").getAsInt()));
                            }
                        } else if (evidences.has("audiovideos")) {
                            JsonArray audiovideos = evidences.get("audiovideos").getAsJsonArray();
                            for (int i = 0; i < audiovideos.size(); i++) {
                                JsonObject audiovideo = audiovideos.get(i).getAsJsonObject();
                                System.out.println(String.format("音视频信息, dataId:%s, 检测结果:%s",
                                        audiovideo.get("dataId").getAsString(), audiovideo.get("result").getAsInt()));
                            }
                        } else if (evidences.has("files")) {
                            JsonArray files = evidences.get("files").getAsJsonArray();
                            for (int i = 0; i < files.size(); i++) {
                                JsonObject file = files.get(i).getAsJsonObject();
                                System.out.println(String.format("文档信息, dataId:%s, 检测结果:%s",
                                        file.get("dataId").getAsString(), file.get("result").getAsInt()));
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
