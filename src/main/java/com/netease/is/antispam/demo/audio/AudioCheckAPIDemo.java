/*
 * @(#) VideoSubmitAPIDemo.java 2016年8月23日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.audio;

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
    private final static String API_URL = "http://as.dun.163.com/v2/audio/check";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 100, 5000, 1000, 1000);

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // 加密方式可选 MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 2.设置私有参数
        params.put("url", "http://xxx.xx");

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        System.out.println(jObject);
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject result = jObject.get("result").getAsJsonObject();
            if (result.has("antisapm")) {
                getAntispam(result.get("antisapm").getAsJsonObject());
            }
            if (result.has("language")) {
                getLanguage(result.get("language").getAsJsonObject());
            }
            if (result.has("asr")) {
                getAsr(result.get("asr").getAsJsonObject());

            }
            if (result.has("voice")) {
                getVoice(result.get("voice").getAsJsonObject());
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }

    private static void getVoice(JsonObject voice) {
        if (voice == null) {
            return;
        }
        String taskId = voice.get("taskId").getAsString();
        String dataId = voice.has("dataId") ? voice.get("dataId").getAsString() : "无";
        String callback = voice.has("callback") ? voice.get("callback").getAsString() : "无";
        JsonObject details = voice.get("details").getAsJsonObject();
        String mainGender = details.get("mainGender").getAsString();
        System.out.println(String.format("人声检测属性结果：taskId %s, dataId %s, callback %s, 人声属性：%s", taskId, dataId,
                callback, mainGender));
    }

    private static void getAsr(JsonObject asr) {
        if (asr == null) {
            return;
        }
        String taskId = asr.get("taskId").getAsString();
        String dataId = asr.has("dataId") ? asr.get("dataId").getAsString() : "无";
        String callback = asr.has("callback") ? asr.get("callback").getAsString() : "无";
        JsonArray details = asr.get("details").getAsJsonArray();
        System.out.println(
                String.format("语音识别检测结果：taskId %s, dataId %s, callback %s, 语音识别详情如下: ", taskId, dataId, callback));
        for (JsonElement detailEle : details) {
            JsonObject detail = detailEle.getAsJsonObject();
            long startTime = detail.get("startTime").getAsLong();
            long endTime = detail.get("endTime").getAsLong();
            String content = detail.get("content").getAsString();
            System.out.println(String.format("开始时间 %s秒，结束时间 %s秒，语音内容 \"%s\" ", startTime, endTime, content));
        }
    }

    private static void getLanguage(JsonObject language) {
        if (language == null) {
            return;
        }
        String taskId = language.get("taskId").getAsString();
        String dataId = language.has("dataId") ? language.get("dataId").getAsString() : "无";
        String callback = language.has("callback") ? language.get("callback").getAsString() : "无";
        JsonArray details = language.get("details").getAsJsonArray();
        System.out.println(
                String.format("语种检测结果：taskId %s, dataId %s, callback %s, 语种检测详情如下: ", taskId, dataId, callback));
        for (JsonElement detailEle : details) {
            JsonObject detail = detailEle.getAsJsonObject();
            String type = detail.get("type").getAsString();
            JsonArray segments = detail.get("segments").getAsJsonArray();
            System.out.println(String.format("语种类型 %s， 音频断句时间 %s", type, segments));
        }

    }

    private static void getAntispam(JsonObject antispam) {
        if (antispam == null) {
            return;
        }
        String taskId = antispam.get("taskId").getAsString();
        int status = antispam.get("status").getAsInt();
        // 检测失败
        if (status == 3) {
            int failureReason = antispam.get("failureReason").getAsInt();
            String reason = getFailureReason(failureReason);
            System.out.println(String.format("内容安全检测结果：taskId = %s，检测失败，失败原因：\"%s\"", taskId, reason));
        }
        // 检测成功
        if (status == 2) {
            System.out.println("----------------------------------------------------------");
            System.out.println(String.format("内容安全检测结果：taskId = %s，检测成功", taskId));
            // 详细字段意义可前往官网查看
            int suggestion = antispam.get("suggestion").getAsInt();
            int censorSource = antispam.get("censorSource").getAsInt();
            int resultType = antispam.get("resultType").getAsInt();
            Long censorTime = antispam.has("censorTime") ? antispam.get("censorTime").getAsLong() : null;
            String censorLabels = antispam.has("censorLabels") ? antispam.get("censorLabels").getAsString() : "无";
            String dataId = antispam.has("dataId") ? antispam.get("dataId").getAsString() : "无";
            String callback = antispam.has("callback") ? antispam.get("callback").getAsString() : "无";
            System.out.println(String.format(
                    "建议结果 %s， 结果类型 %s， 审核来源 %s，审核完成时间 %s，提交时传递的dataId %s， 提交时传递的callback %s， 自定义标签分类信息 %s",
                    suggestion, resultType, censorSource, censorTime, dataId, callback, censorLabels));
            System.out.println("音频数据断句详细信息：");
            if (antispam.has("segments")) {
                JsonArray segments = antispam.get("segments").getAsJsonArray();
                for (JsonElement segmentEle : segments) {
                    JsonObject segment = segmentEle.getAsJsonObject();
                    long statTime = segment.get("startTime").getAsLong();
                    long endTime = segment.get("endTime").getAsLong();
                    int type = segment.get("type").getAsInt();
                    String content = segment.get("content").getAsString();
                    System.out
                            .println(String.format("音频断句开始时间：%s秒，结束时间：%s秒，内容：\"%s\"，类型 %s", statTime, endTime, content,
                                    type == 0 ? "语音识别" : "声纹检测"));
                    JsonArray labels = segment.get("labels").getAsJsonArray();
                    for (JsonElement labelEle : labels) {
                        JsonObject labelObj = labelEle.getAsJsonObject();
                        int label = labelObj.get("label").getAsInt();
                        int level = labelObj.get("level").getAsInt();
                        System.out.println(String.format("分类信息 %s， 分类级别 %s", label, level));
                        JsonElement subLabelsEle = labelObj.get("subLabels");
                        if (subLabelsEle.isJsonArray()) {
                            System.out.println("细分类信息：");
                            for (JsonElement subLabelEle : subLabelsEle.getAsJsonArray()) {
                                JsonObject subLabelObj = subLabelEle.getAsJsonObject();
                                String subLabel = subLabelObj.get("subLabel").getAsString();
                                JsonObject details = subLabelObj.get("details").getAsJsonObject();
                                System.out.println(String.format("细分类类别 %s ，其他信息: %s ", subLabel, details));
                                String hitInfo = details.has("hitInfo") ? details.get("hitInfo").getAsString() : "";
                                JsonArray keywords = details.has("keywords") ? details.get("keywords").getAsJsonArray()
                                        : null;
                                if (keywords != null && keywords.size() > 0) {
                                    for (JsonElement keywordEle : keywords) {
                                        String keyword = keywordEle.getAsString();
                                    }
                                }
                                JsonArray libInfos = details.has("libInfos") ? details.get("libInfos").getAsJsonArray()
                                        : null;
                                if (libInfos != null && libInfos.size() > 0) {
                                    for (JsonElement libInfoEle : libInfos) {
                                        JsonObject libInfo = libInfoEle.getAsJsonObject();
                                        int listType = libInfo.get("listType").getAsInt();
                                        String entity = libInfo.get("entity").getAsString();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("----------------------------------------------------------");
        }

    }

    private static String getFailureReason(int failureReason) {
        String reason;
        switch (failureReason) {
            case 1:
                reason = "文件格式错误";
                break;
            case 2:
                reason = "文件下载失败";
                break;
            case 3:
                reason = "解析失败";
                break;
            case 4:
                reason = "音频流不存在";
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + failureReason);
        }
        return reason;
    }
}
