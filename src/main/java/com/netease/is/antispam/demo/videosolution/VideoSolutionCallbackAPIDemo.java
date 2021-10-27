/*
 * @(#) LiveAudioCallbackAPIDemo.java 2019-04-11
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.videosolution;

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
 * 调用易盾反垃圾云服务获取点播音视频解决方案离线结果接口API示例
 *
 * @author maxiaofeng
 * @version 2019-06-10
 */
public class VideoSolutionCallbackAPIDemo {
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
    private final static String API_URL = "http://as.dun.163.com/v2/videosolution/callback/results";
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
        // 点播音视频解决方案版本v1.1及以上语音二级细分类subLabels结构进行调整
        params.put("version", "v2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 3.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);
        System.out.println(response);
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
                    String taskId = jObject.get("taskId").getAsString();
                    String dataId = jObject.get("dataId").getAsString();
                    String callback = jObject.get("callback").getAsString();
                    int result = jObject.get("result").getAsInt();
                    int censorSource = jObject.get("censorSource").getAsInt();
                    int checkStatus = jObject.get("checkStatus").getAsInt();
                    long checkTime = jObject.get("checkTime").getAsLong();
                    long duration = jObject.get("duration").getAsLong();
                    long receiveTime = jObject.get("receiveTime").getAsLong();
                    long censorTime = jObject.get("censorTime").getAsLong();
                    System.out.println(String.format(
                            "taskId:%s, dataId:%s, callback:%s, result:%s, censorSource:%s, checkStatus:%s, checkTime:%s, duration:%s, receiveTime:%s, censorTime:%s",
                            taskId, dataId, callback, result, censorSource, checkStatus, checkTime, duration,
                            receiveTime, censorTime));
                    if (jObject.has("evidences")) {
                        JsonObject evidences = jObject.get("evidences").getAsJsonObject();
                        if (evidences.has("audio")) {
                            JsonObject audio = evidences.get("audio").getAsJsonObject();
                            int asrStatus = audio.get("asrStatus").getAsInt();
                            if (asrStatus == 4) {
                                int asrResult = audio.get("asrResult").getAsInt();
                                System.out.println(String.format("检测失败: taskId=%s, asrResult=%s", taskId, asrResult));
                            } else {
                                int action = audio.get("action").getAsInt();
                                JsonArray labelArray = audio.getAsJsonArray("labels");
                                if (action == 0) {
                                    System.out.println(String.format("taskId=%s，结果：通过", taskId));
                                } else if (action == 1 || action == 2) {
                                    for (JsonElement labelElement : labelArray) {
                                        JsonObject lObject = labelElement.getAsJsonObject();
                                        int label = lObject.get("label").getAsInt();
                                        int level = lObject.get("level").getAsInt();
                                        // 注意二级细分类结构
                                        JsonArray subLabels = lObject.get("subLabels").getAsJsonArray();
                                        if (subLabels != null && subLabels.size() > 0) {
                                            for (int i = 0; i < subLabels.size(); i++) {
                                                JsonObject subLabelObj = subLabels.get(i).getAsJsonObject();
                                                String subLabel = subLabelObj.get("subLabel").getAsString();
                                                JsonObject details = subLabelObj.getAsJsonObject("details");
                                                JsonArray hintArray = details.getAsJsonArray("hint");
                                            }
                                        }
                                    }
                                    System.out.println(String.format("taskId=%s，结果：%s，证据信息如下：%s", taskId,
                                            action == 1 ? "不确定" : "不通过", labelArray.toString()));
                                }
                            }
                        }
                    }
                    if (jObject.has("reviewEvidences")) {
                        String reviewEvidences = jObject.get("reviewEvidences").getAsString();
                        System.out.println(String.format("人审证据信息 %s", reviewEvidences));
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

}
