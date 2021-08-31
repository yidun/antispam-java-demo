/*
 * @(#) LiveVideoSolutionQueryAudioAPIDemo.java 2020-10-15
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.livevideosolution;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.Utils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.Map;


/**
 * 调用易盾云服务实时更新直播音视频信息接口API示例
 *
 * @author yd-dev
 * @version 2020-10-15
 */
public class LiveVideoSolutionFeedbackAPIDemo {

    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 易盾反垃圾云服务直播音视频解决方案在线提交接口地址 */
    private final static String API_URL = "http://as.dun.163yun.com/v1/livewallsolution/feedback";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 1.设置公共参数
        Map<String, String> params = Utils.getCommonParams(SECRETID, "v1.0", "MD5");

        // 2.设置私有参数
        JsonObject realTimeInfo = new JsonObject();
        realTimeInfo.addProperty("taskId", "****");
        realTimeInfo.addProperty("status", 100);

        JsonArray realTimeInfoArray = new JsonArray();
        realTimeInfoArray.add(realTimeInfo);
        params.put("realTimeInfoList", new Gson().toJson(realTimeInfoArray));

        // 3.生成签名信息
        Utils.sign(params, SECRETKEY);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        JsonArray resultArray = jObject.get("result").getAsJsonArray();
        if (code == 200) {
            for (int i = 0; i < resultArray.size(); i++) {
                JsonObject result = resultArray.get(i).getAsJsonObject();
                String taskId = result.get("taskId").getAsString();
                int r = result.get("result").getAsInt();
                if (r == 0) {
                    System.out.println("SUCCESS, taskId=" + taskId);
                } else if (r == 2) {
                    System.out.println("NOT EXISTS, taskId=" + taskId);
                } else if (r == 1) {
                    System.out.println("SERVER ERROR, taskId=" + taskId);
                }
            }
        } else {
            System.out.printf("ERROR: code=%s, msg=%s", code, msg);
        }
    }
}
