/*
 * @(#) LiveVideoSolutionQueryAudioAPIDemo.java 2020-10-15
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.livevideosolution;

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
 * 调用易盾云服务查询音频断句信息接口API示例
 *
 * @author yd-dev
 * @version 2020-10-15
 */
public class LiveVideoSolutionQueryAudioAPIDemo {

    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾云服务直播音视频解决方案在线提交接口地址
     */
    private final static String API_URL = "http://as.dun.163yun.com/v1/livewallsolution/query/audio/task";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 1.设置公共参数
        Map<String, String> params = Utils.getCommonParams(SECRETID, "v1.0", "MD5");

        // 2.设置私有参数
        params.put("taskId", "292604e0200b4551b411c2d53adde893");
        params.put("startTime", String.valueOf(System.currentTimeMillis() - 10 * 60 * 1000));
        params.put("endTime", String.valueOf(System.currentTimeMillis()));// 最长支持查10分钟跨度

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
            JsonArray resultArray = resultObject.getAsJsonArray("result");
            if (resultArray.size() == 0) {
                System.out.println("没有结果");
            } else {
                for (JsonElement jsonElement : resultArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    String taskId = jObject.get("taskId").getAsString();
                    int action = jObject.get("action").getAsInt();
                    long startTime = jObject.get("startTime").getAsLong();
                    long endTime = jObject.get("endTime").getAsLong();
                    JsonArray segmentArray = jObject.getAsJsonArray("segments");
                    if (action == 0) {
                        System.out.println(String.format("taskId=%s，结果：通过，时间区间【%s-%s】，证据信息如下：%s", taskId, startTime,
                                endTime, segmentArray.toString()));
                    } else if (action == 1 || action == 2) {
                        // for (JsonElement labelElement : segmentArray) {
                        // JsonObject lObject = labelElement.getAsJsonObject();
                        // int label = lObject.get("label").getAsInt();
                        // int level = lObject.get("level").getAsInt();
                        // }
                        System.out.println(String.format("taskId=%s，结果：%s，时间区间【%s-%s】，证据信息如下：%s", taskId,
                                action == 1 ? "不确定" : "不通过", startTime, endTime, segmentArray.toString()));
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
