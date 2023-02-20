/*
 * @(#) ImageCheckAPIDemo.java 2016年3月15日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.image;

import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务图片离线检测接口API示例，建议离线提交30秒后进行查询，最长不能超过4小时，否则数据将会丢失
 *
 * @author yd-dev
 * @version 2020-10-15
 */
public class ImageAsyncCheckAPIDemo {
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
     * 易盾反垃圾云服务图片离线检测接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v4/image/asyncCheck";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 1.设置公共参数
        Map<String, String> params = Utils.getCommonParams(SECRETID, BUSINESSID, "v4", "MD5");

        // 2.设置私有参数
        JsonArray jsonArray = new JsonArray();
        // 传图片url进行检测，name结构产品自行设计，用于唯一定位该图片数据
        JsonObject image1 = new JsonObject();
        image1.addProperty("name", "https://nos.netease.com/yidun/2-0-0-a6133509763d4d6eac881a58f1791976.jpg");
        image1.addProperty("type", 1);
        // 主动回调地址url,如果设置了则走主动回调逻辑
        // image1.addProperty("callbackUrl", "http://***");
        image1.addProperty("data", "https://nos.netease.com/yidun/2-0-0-a6133509763d4d6eac881a58f1791976.jpg");
        jsonArray.add(image1);

        params.put("images", jsonArray.toString());

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
            JsonObject result = resultObject.get("result").getAsJsonObject();
            JsonArray resultArray = result.getAsJsonArray("checkImages");
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                String name = jObject.get("name").isJsonNull() ? "" : jObject.get("name").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                System.out.println(String.format("name=%s，taskId=%s", name, taskId));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }

    }
}
