/*
 * @(#) LiveSolutionQueryMonitorAPIDemo.java 2020-09-28
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.videosolution;

import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾云服务查询直播监控记录接口API示例
 *
 * @author yd-dev
 * @version 2020-09-28
 */
public class LiveSolutionQueryMonitorAPIDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";

    /**
     * 易盾反垃圾云服务直播监控记录获取接口地址
     */
    private final static String API_URL = "https://as.dun.163.com/v1/livewallsolution/query/monitor";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 1000, 1000);

    public static void main(String[] args) throws Exception {
        // 1. 设置公共参数
        Map<String, String> params = Utils.getCommonParams(SECRETID, null, "v1.0", "MD5");
        // 2. 设置私有参数
        params.put("taskId", "49db4e9dc56a424bb720fb46071532b4");
        // 预处理参数
        params = Utils.pretreatmentParams(params);
        // 3. 生成签名信息
        Utils.sign(params, SECRETKEY);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5. 解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        System.out.println(jObject);
        if (code == 200) {
            JsonObject result = jObject.getAsJsonObject("result");
            int status = result.get("status").getAsInt();
            if (status == 0) {
                JsonArray records = result.get("records").getAsJsonArray();
                for (int i = 0; i < records.size(); i++) {
                    JsonObject record = records.get(i).getAsJsonObject();
                    int action = record.get("action").getAsInt();
                    int label = record.get("label").getAsInt();
                    long actionTime = record.get("actionTime").getAsLong();
                    String detail = record.get("detail").getAsString();
                    System.out.printf("action=%s, label=%s, actionTime=%s, detail=%s%n", action, label, actionTime,
                            detail);
                }
                System.out.printf("直播人审结果：%s%n", result.get("records").getAsJsonArray().toString());
            } else if (status == 20) {
                System.out.println("数据过期");
            } else if (status == 30) {
                System.out.println("数据不存在");
            }
        } else {
            System.out.printf("ERROR: code=%s, msg=%s%n", code, msg);
        }
    }
}
