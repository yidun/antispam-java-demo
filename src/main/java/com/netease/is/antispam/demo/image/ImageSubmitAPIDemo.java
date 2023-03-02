/*
 * @(#) TextCallbackAPIDemo.java 2016年12月28日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.image;

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
 * 调用易盾反垃圾云服务图片批量提交接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author hzgaomin
 * @version 2019年11月28日
 */
public class ImageSubmitAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务图片离线检测结果获取接口地址 */
    private final static String API_URL = "https://as.dun.163.com/v1/image/submit";
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
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 2.设置私有参数
        JsonArray imageArray = new JsonArray();
        // dataId结构产品自行设计，用于唯一定位该图片数据
        JsonObject image1 = new JsonObject();
        image1.addProperty("name", "image1");
        image1.addProperty("data", "https://nos.netease.com/yidun/2-0-0-a6133509763d4d6eac881a58f1791976.jpg");
        // 分类级别，0：正常，1：不确定，2：确定
        image1.addProperty("level", "2");
        // 类型，1：图片URL（当前只支持url）
        image1.addProperty("type", "1");

        // -----------------------------------以下参数为选填-----------------------------------------

        // 客户的数据唯一标识
        image1.addProperty("dataId", "1234567");
        // 分类信息，100：色情，110：性感低俗，200：广告，210：二维码，260：广告法，300：暴恐，400：违禁，500：涉政，800：恶心类，900：其他，1100：涉价值观
        image1.addProperty("label", "100");
        image1.addProperty("ip", "127.0.0.1");
        image1.addProperty("account", "account");
        image1.addProperty("deviceId", "deviceId");
        // 审核后是否需要主动回调，需要则设置主动回调url
        image1.addProperty("callbackUrl", "http://####");

        // -----------------------------------细分类subLabel相关 开始-----------------------------------------
        // 命中的线索信息
        JsonObject subLabelHitInfoJson = new JsonObject();
        // 图片中包含的可识别内容
        subLabelHitInfoJson.addProperty("value", "广告");
        // 位置信息，对应图片矩形左上角相对坐标
        subLabelHitInfoJson.addProperty("x1", 0.11);
        subLabelHitInfoJson.addProperty("y1", 0.22);
        subLabelHitInfoJson.addProperty("x2", 0.55);
        subLabelHitInfoJson.addProperty("y2", 0.77);

        JsonArray subLabelHitInfoArray = new JsonArray();
        subLabelHitInfoArray.add(subLabelHitInfoJson);

        // 细分类详情
        JsonObject subLabelDetailJson = new JsonObject();
        subLabelDetailJson.add("hitInfos", subLabelHitInfoArray);

        JsonObject subLabelJson = new JsonObject();
        // 细分类，详细编码请参考https://support.dun.163.com/documents/588434277524447232?locale=zh-cn&docId=447229776072957952
        subLabelJson.addProperty("subLabel", "10000");
        subLabelJson.add("details", subLabelDetailJson);

        // 细分类相关
        image1.add("subLabel", subLabelJson);

        // -----------------------------------细分类subLabel相关 结束-----------------------------------------

        // -----------------------------------ocr相关 开始-----------------------------------------
        // OCR行信息
        JsonObject ocrLineJson = new JsonObject();
        // 每行的文字信息
        ocrLineJson.addProperty("lineContent", "广告");
        // 位置信息，对应图片矩形左上角相对坐标
        ocrLineJson.addProperty("x1", 0.11);
        ocrLineJson.addProperty("y1", 0.22);
        ocrLineJson.addProperty("x2", 0.55);
        ocrLineJson.addProperty("y2", 0.77);

        JsonArray ocrLineArray = new JsonArray();
        ocrLineArray.add(ocrLineJson);

        JsonObject ocrJson = new JsonObject();
        ocrJson.add("lineContents", ocrLineArray);

        // ocr相关
        image1.add("ocr", ocrJson);
        // -----------------------------------ocr相关 结束-----------------------------------------

        // -----------------------------------人脸信息相关 开始-----------------------------------------
        // 人脸详细信息
        JsonObject faceLineJson = new JsonObject();

        // 图片出现的人脸名字，未识别则为空
        faceLineJson.addProperty("name", "王XX");
        // 人脸性别，值为男（male）、女（female）；不可识别则为空
        faceLineJson.addProperty("gender", "male");
        // 人脸年龄，值为具体年龄（age）；不可识别则为空
        faceLineJson.addProperty("age", 22);
        // 位置信息，对应图片矩形左上角相对坐标
        faceLineJson.addProperty("x1", 0.11);
        faceLineJson.addProperty("y1", 0.22);
        faceLineJson.addProperty("x2", 0.55);
        faceLineJson.addProperty("y2", 0.77);

        JsonArray faceArray = new JsonArray();
        faceArray.add(faceLineJson);

        JsonObject faceJson = new JsonObject();
        faceJson.add("faceContents", faceArray);

        // 人脸检测信息
        image1.add("face", faceJson);
        // -----------------------------------人脸信息相关 结束-----------------------------------------

        // -----------------------------------图片质量相关 开始-----------------------------------------
        JsonObject qualityJson = new JsonObject();
        // 美观度分数，0-1，分数越高美观度越高
        qualityJson.addProperty("aestheticsRate", 0.66);
        // 清晰度分数，0-1，分数越高清晰度越高
        qualityJson.addProperty("sharpnessRate", 0.66);

        image1.add("quality", qualityJson);
        // -----------------------------------图片质量相关 结束-----------------------------------------

        imageArray.add(image1);

        JsonObject image2 = new JsonObject();
        image2.addProperty("name", "image2");
        image2.addProperty("data",
                "http://dun.163.com/public/res/web/case/sexy_normal_2.jpg?dda0e793c500818028fc14f20f6b492a");
        image2.addProperty("level", "0");
        image2.addProperty("type", "1");
        imageArray.add(image2);
        params.put("images", imageArray.toString());

        // 预处理参数
        params = Utils.pretreatmentParams(params);
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
            JsonArray resultArray = resultObject.getAsJsonArray("result");
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String name = jObject.get("name").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                System.out.println(String.format("图片提交返回name=%s，taskId:%s", name, taskId));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
