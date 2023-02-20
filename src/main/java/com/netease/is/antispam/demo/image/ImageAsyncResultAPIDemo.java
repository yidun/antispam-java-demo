/*
 * @(#) ImageCheckAPIDemo.java 2016年12月28日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.image;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务图片结果查询接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author hzgaomin
 * @version 2016年12月28日
 */
public class ImageAsyncResultAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务图片离线結果查詢接口地址 */
    private final static String API_URL = "https://as.dun.163.com/v4/image/asyncResult";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 1.设置公共参数
        Map<String, String> params = Utils.getCommonParams(SECRETID, BUSINESSID, "v4", "MD5");

        // 2.设置私有参数
        Set<String> taskIds = new HashSet<String>();
        taskIds.add("202b1d65f5854cecadcb24382b681c1a");
        taskIds.add("0f0345933b05489c9b60635b0c8cc721");
        params.put("taskIds", new Gson().toJson(taskIds));

        // 预处理参数
        params = Utils.pretreatmentParams(params);

        // 3.生成签名信息
        Utils.sign(params, SECRETKEY);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject resultObject = new JsonParser().parse(response).getAsJsonObject();
        int code = resultObject.get("code").getAsInt();
        String msg = resultObject.get("msg").getAsString();
        if (code == 200) {
            // 图片反垃圾结果
            JsonArray antispamArray = resultObject.getAsJsonArray("antispam");
            for (JsonElement jsonElement : antispamArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String name = jObject.get("name").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                int status = jObject.get("status").getAsInt();
                // 图片检测状态码，定义为：0：检测成功，610：图片下载失败，620：图片格式错误，630：其它
                if (0 == status) {
                    // 图片维度结果
                    int action = jObject.get("action").getAsInt();
                    JsonArray labelArray = jObject.get("labels").getAsJsonArray();
                    System.out.println(
                            String.format("taskId=%s，status=%s，name=%s，action=%s", taskId, status, name, action));
                    // 产品需根据自身需求，自行解析处理，本示例只是简单判断分类级别
                    for (JsonElement labelElement : labelArray) {
                        JsonObject lObject = labelElement.getAsJsonObject();
                        int label = lObject.get("label").getAsInt();
                        int level = lObject.get("level").getAsInt();
                        double rate = lObject.get("rate").getAsDouble();
                        JsonArray subLabels = lObject.getAsJsonArray("subLabels");
                        System.out.println(String.format("label:%s, level=%s, rate=%s, subLabels=%s", label, level,
                                rate, subLabels.toString()));
                    }
                    switch (action) {
                        case 0:
                            System.out.println("#图片机器检测结果：最高等级为\"正常\"\n");
                            break;
                        case 1:
                            System.out.println("#图片机器检测结果：最高等级为\"嫌疑\"\n");
                            break;
                        case 2:
                            System.out.println("#图片机器检测结果：最高等级为\"确定\"\n");
                            break;
                        default:
                            break;
                    }
                } else {
                    // status对应失败状态码：610：图片下载失败，620：图片格式错误，630：其它
                    System.out.println(String.format("图片检测失败，taskId=%s，status=%s，name=%s", taskId, status, name));
                }
            }
            // 图片OCR结果
            JsonArray ocrArray = resultObject.getAsJsonArray("ocr");
            for (JsonElement jsonElement : ocrArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String name = jObject.get("name").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                JsonArray details = jObject.get("details").getAsJsonArray();
                System.out.println(String.format("taskId=%s,name=%s", taskId, name));
                // 产品需根据自身需求，自行解析处理，本示例只是简单输出ocr结果信息
                for (JsonElement detail : details) {
                    JsonObject lObject = detail.getAsJsonObject();
                    String content = lObject.get("content").getAsString();
                    JsonArray lineContents = lObject.getAsJsonArray("lineContents");
                    System.out.println(String.format("识别ocr文本内容:%s, ocr片段及坐标信息:%s", content, lineContents.toString()));
                }
            }
            // 图片人脸检测结果
            JsonArray faceArray = resultObject.getAsJsonArray("face");
            for (JsonElement jsonElement : faceArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String name = jObject.get("name").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                JsonArray details = jObject.get("details").getAsJsonArray();
                System.out.println(String.format("taskId=%s,name=%s", taskId, name));
                // 产品需根据自身需求，自行解析处理，本示例只是简单输出人脸结果信息
                for (JsonElement detail : details) {
                    JsonObject lObject = detail.getAsJsonObject();
                    int faceNumber = lObject.get("faceNumber").getAsInt();
                    JsonArray faceContents = lObject.getAsJsonArray("faceContents");
                    System.out.println(String.format("识别人脸数量:%s, 人物信息及坐标信息:%s", faceNumber, faceContents.toString()));
                }
            }
            // 图片质量检测结果
            JsonArray qualityArray = resultObject.getAsJsonArray("quality");
            for (JsonElement jsonElement : qualityArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String name = jObject.get("name").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                JsonArray details = jObject.get("details").getAsJsonArray();
                System.out.println(String.format("taskId=%s,name=%s", taskId, name));
                // 产品需根据自身需求，自行解析处理，本示例只是简单输出质量结果信息
                for (JsonElement detail : details) {
                    JsonObject lObject = detail.getAsJsonObject();
                    float aestheticsRate = lObject.get("aestheticsRate").getAsFloat();
                    JsonObject metaInfo = lObject.getAsJsonObject("metaInfo");
                    JsonObject boarderInfo = lObject.getAsJsonObject("boarderInfo");
                    System.out.println(String.format("图片美观度分数:%s, 图片基本信息:%s, 图片边框信息:%s", aestheticsRate,
                            metaInfo.toString(), boarderInfo.toString()));
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
