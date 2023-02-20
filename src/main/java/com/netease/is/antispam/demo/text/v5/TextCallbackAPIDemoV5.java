/*
 * @(#) TextCallbackAPIDemo.java 2016年3月15日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.text.v5;

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
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务文本V5离线检测结果获取接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求
 * 2.commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java 3. gson，用于做json解析
 *
 * @author yidun
 * @version 2021年08月31日
 */
public class TextCallbackAPIDemoV5 {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务文本离线检测结果获取接口地址 */
    private final static String API_URL = "https://as.dun.163.com/v5/text/callback/results";
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
        params.put("version", "v5");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 预处理参数
        params = Utils.pretreatmentParams(params);
        // 2.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 3.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 4.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            if (jObject.has("result")) {
                JsonArray result = jObject.get("result").getAsJsonArray();
                int resultType = 1;
                for (JsonElement jsonElement : result) {
                    JsonObject resultObject = jsonElement.getAsJsonObject();
                    // 内容安全结果
                    if (resultObject.has("antispam")) {
                        JsonObject antispam = resultObject.getAsJsonObject("antispam");
                        if (antispam != null) {
                            String taskId = antispam.get("taskId").getAsString();
                            String dataId = antispam.get("dataId").getAsString();
                            int suggestion = antispam.get("suggestion").getAsInt();
                            // 结果类型，1：机审，2：人审
                            resultType = antispam.get("resultType").getAsInt();
                            if (resultType == 2) {
                                String callback = antispam.get("callback").getAsString();
                                int censorSource = antispam.get("censorSource").getAsInt();
                                int censorRound = antispam.get("censorRound").getAsInt();
                                long censorTime = antispam.get("censorTime").getAsLong();
                                if (antispam.has("censorLabels")) {
                                    JsonArray censorLabels = antispam.get("censorLabels").getAsJsonArray();
                                    for (JsonElement censorLabelElement : censorLabels) {
                                        JsonObject censorLabel = censorLabelElement.getAsJsonObject();
                                        String subLabelCode = censorLabel.get("code").getAsString();
                                        String subLabelDesc = censorLabel.get("desc").getAsString();
                                    }
                                }
                            }
                            int censorType = antispam.get("censorType").getAsInt();
                            boolean isRelatedHit = antispam.get("isRelatedHit").getAsBoolean();
                            JsonArray labels = antispam.get("labels").getAsJsonArray();
                            System.out.println(
                                    String.format("内容安全结果，taskId: %s，dataId: %s，suggestion: %s", taskId, dataId,
                                            suggestion));
                            for (JsonElement labelElement : labels) {
                                JsonObject labelItem = labelElement.getAsJsonObject();
                                int label = labelItem.get("label").getAsInt();
                                int level = labelItem.get("level").getAsInt();
                                JsonArray subLabels = labelItem.get("subLabels").getAsJsonArray();
                                if (subLabels != null && subLabels.size() > 0) {
                                    for (JsonElement subLabelElement : subLabels) {
                                        JsonObject subLabelItem = subLabelElement.getAsJsonObject();
                                        String subLabel = subLabelItem.get("subLabel").getAsString();
                                        System.out.println(
                                                String.format("内容安全分类，label: %s，subLabel: %s", label, subLabel));
                                        if (resultType == 1 && subLabelItem.has("details")) {
                                            JsonObject details = subLabelItem.get("details").getAsJsonObject();
                                            // 自定义敏感词信息
                                            if (details.has("keywords")) {
                                                JsonArray keywords = details.get("keywords").getAsJsonArray();
                                                if (keywords != null && keywords.size() > 0) {
                                                    for (JsonElement keywordElement : keywords) {
                                                        JsonObject keywordItem = keywordElement.getAsJsonObject();
                                                        String word = keywordItem.get("word").getAsString();
                                                    }
                                                }
                                            }
                                            // 自定义名单库信息
                                            if (details.has("libInfos")) {
                                                JsonArray libInfos = details.get("libInfos").getAsJsonArray();
                                                if (libInfos != null && libInfos.size() > 0) {
                                                    for (JsonElement libInfoElement : libInfos) {
                                                        JsonObject libInfoItem = libInfoElement.getAsJsonObject();
                                                        int type = libInfoItem.get("type").getAsInt();
                                                        String entity = libInfoItem.get("entity").getAsString();
                                                    }
                                                }
                                            }
                                            // 线索信息
                                            if (details.has("hitInfos")) {
                                                JsonArray hitInfos = details.get("hitInfos").getAsJsonArray();
                                                if (hitInfos != null && hitInfos.size() > 0) {
                                                    for (JsonElement hitInfoElement : hitInfos) {
                                                        JsonObject hitInfoItem = hitInfoElement.getAsJsonObject();
                                                        String value = hitInfoItem.get("value").getAsString();
                                                        JsonArray positions = hitInfoItem.get("positions")
                                                                .getAsJsonArray();
                                                        if (positions != null && positions.size() > 0) {
                                                            for (JsonElement positionElement : positions) {
                                                                JsonObject positionItem = positionElement
                                                                        .getAsJsonObject();
                                                                String fieldName = positionItem.get("fieldName")
                                                                        .getAsString();
                                                                int startPos = positionItem.get("startPos").getAsInt();
                                                                int endPos = positionItem.get("endPos").getAsInt();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            // 反作弊信息
                                            if (details.has("anticheat")) {
                                                JsonObject anticheat = details.get("anticheat").getAsJsonObject();
                                                if (anticheat != null) {
                                                    int type = anticheat.get("type").getAsInt();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (resultType == 1) {
                        // 情感分析结果
                        if (resultObject.has("emotionAnalysis")) {
                            JsonObject emotionAnalysis = resultObject.getAsJsonObject("emotionAnalysis");
                            if (emotionAnalysis != null) {
                                String taskId = emotionAnalysis.get("taskId").getAsString();
                                String dataId = emotionAnalysis.get("dataId").getAsString();
                                if (emotionAnalysis.has("details")) {
                                    JsonArray details = emotionAnalysis.get("details").getAsJsonArray();
                                    System.out.println(
                                            String.format("情感分析结果，taskId: %s，dataId: %s，details: %s", taskId, dataId,
                                                    details));
                                    if (details != null && details.size() > 0) {
                                        for (JsonElement detailElement : details) {
                                            JsonObject detailItem = detailElement.getAsJsonObject();
                                            double positiveProb = detailItem.get("positiveProb").getAsDouble();
                                            double negativeProb = detailItem.get("negativeProb").getAsDouble();
                                            String sentiment = detailItem.get("sentiment").getAsString();
                                        }
                                    }
                                }
                            }
                        }

                        // 反作弊结果
                        if (resultObject.has("anticheat")) {
                            JsonObject anticheat = resultObject.getAsJsonObject("anticheat");
                            if (anticheat != null) {
                                String taskId = anticheat.get("taskId").getAsString();
                                String dataId = anticheat.get("dataId").getAsString();
                                if (anticheat.has("details")) {
                                    JsonArray details = anticheat.get("details").getAsJsonArray();
                                    System.out.println(
                                            String.format("反作弊结果，taskId: %s，dataId: %s，details: %s", taskId, dataId,
                                                    details));
                                    if (details != null && details.size() > 0) {
                                        for (JsonElement detailElement : details) {
                                            JsonObject detailItem = detailElement.getAsJsonObject();
                                            int suggestion = detailItem.get("suggestion").getAsInt();
                                            JsonArray hitInfos = detailItem.get("hitInfos").getAsJsonArray();
                                            if (hitInfos != null && hitInfos.size() > 0) {
                                                for (JsonElement hitInfoElement : hitInfos) {
                                                    JsonObject hitInfoItem = hitInfoElement.getAsJsonObject();
                                                    int hitType = hitInfoItem.get("hitType").getAsInt();
                                                    String hitMsg = hitInfoItem.get("hitMsg").getAsString();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 用户画像结果
                        if (resultObject.has("userRisk")) {
                            JsonObject userRisk = resultObject.getAsJsonObject("userRisk");
                            if (userRisk != null) {
                                String taskId = userRisk.get("taskId").getAsString();
                                String dataId = userRisk.get("dataId").getAsString();
                                if (userRisk.has("details")) {
                                    JsonArray details = userRisk.get("details").getAsJsonArray();
                                    System.out.println(
                                            String.format("用户画像结果，taskId: %s，dataId: %s，details: %s", taskId, dataId,
                                                    details));
                                    if (details != null && details.size() > 0) {
                                        for (JsonElement detailElement : details) {
                                            JsonObject detailItem = detailElement.getAsJsonObject();
                                            String account = detailItem.get("account").getAsString();
                                            int accountLevel = detailItem.get("accountLevel").getAsInt();
                                        }
                                    }
                                }
                            }
                        }

                        // 语种检测结果
                        if (resultObject.has("language")) {
                            JsonObject language = resultObject.getAsJsonObject("language");
                            if (language != null) {
                                String taskId = language.get("taskId").getAsString();
                                String dataId = language.get("dataId").getAsString();
                                if (language.has("details")) {
                                    JsonArray details = language.get("details").getAsJsonArray();
                                    System.out.println(
                                            String.format("语种检测结果，taskId: %s，dataId: %s，details: %s", taskId, dataId,
                                                    details));
                                    if (details != null && details.size() > 0) {
                                        for (JsonElement detailElement : details) {
                                            JsonObject detailItem = detailElement.getAsJsonObject();
                                            String type = detailItem.get("type").getAsString();
                                        }
                                    }
                                }
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
