/*
 * @(#) VideoCallbackAPIDemo.java 2016年8月23日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.video;

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
 * 调用易盾反垃圾云服务视频离线结果获取接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 * @author hzdingyong
 * @version 2016年8月23日
 */
public class VideoCallbackAPIDemo {
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
     * 易盾反垃圾云服务视频离线结果获取接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v4/video/callback/results";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 1000, 1000);

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
            if (resultArray.size() == 0) {
                System.out.println("暂无回调数据");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject antispam = jsonElement.getAsJsonObject();
                    int status = antispam.get("status").getAsInt();
                    if (status != 2) {// 异常，异常码定义见官网文档
                        int failReason = antispam.get("failureReason").getAsInt();
                        System.out.println(String.format("视频检测失败，status=%s，失败类型=%s", status, failReason));
                        continue;
                    }
                    String taskId = antispam.get("taskId").getAsString();
                    int suggestion = antispam.get("suggestion").getAsInt();
                    int resultType = antispam.get("resultType").getAsInt();
                    String callback = Utils.getStringProperty(antispam, "callback");
                    Integer censorSource = Utils.getIntegerProperty(antispam, "censorSource");
                    Long censorTime = Utils.getLongProperty(antispam, "censorTime");
                    String censorLabels = Utils.getStringProperty(antispam, "censorLabels");
                    System.out.printf("检测成功，taskId=%s, 嫌疑类型 %s，结果类型 %s，回调信息 %s， 审核来源 %s， 人审时长 %s， 分类标签 %s",
                            taskId, suggestion, resultType, callback, censorSource, censorTime, censorLabels);
                    if (antispam.has("pictures")) {
                        System.out.print("截图证据信息: ");
                        JsonArray picturesArr = antispam.get("pictures").getAsJsonArray();
                        if (picturesArr != null && picturesArr.size() > 0) {
                            for (JsonElement pictureEle : picturesArr) {
                                JsonObject picture = pictureEle.getAsJsonObject();
                                int type = picture.get("type").getAsInt();
                                String url = picture.get("url").getAsString();
                                int censorSource1 = picture.get("censorSource").getAsInt();
                                long startTime = picture.get("startTime").getAsLong();
                                long endTime = picture.get("endTime").getAsLong();
                                System.out.printf("图片类型 %s， 图片地址 %s， 审核来源 %s， 开始时间 %s - 结束时间 %s ", type, url,
                                        censorSource1, startTime, endTime);
                                if (picture.has("frontPics")) {
                                    System.out.print(
                                            String.format("关联信息-命中前截图信息 %s ,", picture.get("frontPics").getAsString()));
                                }
                                if (picture.has("backPics")) {
                                    System.out.print(
                                            String.format("关联信息-命中后截图信息 %s ,", picture.get("backPics").getAsString()));
                                }
                                JsonArray labels = picture.get("labels").getAsJsonArray();
                                System.out.println(" 命中的分类信息: ");
                                for (JsonElement labelEle : labels) {
                                    JsonObject labelObj = labelEle.getAsJsonObject();
                                    int label = labelObj.get("label").getAsInt();
                                    int level = labelObj.get("level").getAsInt();
                                    int rate = labelObj.get("rate").getAsInt();
                                    System.out.printf("分类信息 %s， 分类级别 %s，置信分值 %s，", label, level, rate);
                                    for (JsonElement subLabelEle : labelObj.get("subLabels").getAsJsonArray()) {
                                        JsonObject subLabelObj = subLabelEle.getAsJsonObject();
                                        int subLabel = subLabelObj.get("subLabel").getAsInt();
                                        int subLabelRate = subLabelObj.get("rate").getAsInt();
                                        System.out.printf("细分类类型 %s，置信分值 %s，", subLabel, rate);
                                        JsonObject details = subLabelObj.get("details").getAsJsonObject();
                                        if (details == null) {
                                            continue;
                                        }
                                        System.out.printf("命中的详细信息：");
                                        if (details.has("keywords")) {
                                            JsonArray keywordsArr = details.get("keywords").getAsJsonArray();
                                            if (keywordsArr != null && keywordsArr.size() > 0) {
                                                for (JsonElement keywordsEle : keywordsArr) {
                                                    JsonObject keywordsObj = keywordsEle.getAsJsonObject();
                                                    System.out.printf("命中敏感词内容 %s, ", keywordsObj);
                                                }
                                            }
                                        }
                                        JsonArray libInfosArr = details.get("libInfos").getAsJsonArray();
                                        if (libInfosArr != null && libInfosArr.size() > 0) {
                                            for (JsonElement ele : libInfosArr) {
                                                JsonObject libInfoObj = ele.getAsJsonObject();
                                                System.out.printf("命中的自定义图片名单信息 %s, ", libInfoObj);
                                            }
                                        }
                                        JsonArray hintInfosArr = details.get("hintInfos").getAsJsonArray();
                                        if (hintInfosArr != null && hintInfosArr.size() > 0) {
                                            for (JsonElement ele : hintInfosArr) {
                                                JsonObject hintInfoObj = ele.getAsJsonObject();
                                                System.out.printf("命中的线索信息 %s, ", hintInfoObj);
                                            }
                                        }
                                        if (details.has("anticheat")) {
                                            JsonArray anticheatArr = details.get("hintInfos").getAsJsonArray();
                                            if (anticheatArr != null && anticheatArr.size() > 0) {
                                                for (JsonElement ele : anticheatArr) {
                                                    JsonObject anticheatObj = ele.getAsJsonObject();
                                                    System.out.printf("命中的线索信息 %s, ", anticheatObj);
                                                }
                                            }
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
