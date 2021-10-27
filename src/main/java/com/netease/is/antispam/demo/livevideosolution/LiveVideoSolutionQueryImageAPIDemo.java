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
 * 调用易盾云服务查询视频截图信息接口API示例
 *
 * @author yd-dev
 * @version 2020-10-15
 */
public class LiveVideoSolutionQueryImageAPIDemo {

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
    private final static String API_URL = "http://as.dun.163yun.com/v1/livewallsolution/query/image";
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
        Map<String, String> params = Utils.getCommonParams(SECRETID, "v1", "MD5");

        // 2.设置私有参数
        params.put("taskId", "95b9496929a647d3be6bee74db639eab");
        params.put("pageNum", "1");
        params.put("pageSize", "10");

        // 3.生成签名信息
        Utils.sign(params, SECRETKEY);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject resultObject = new JsonParser().parse(response).getAsJsonObject();
        int code = resultObject.get("code").getAsInt();
        String msg = resultObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject result = resultObject.getAsJsonObject("result");
            int status = result.get("status").getAsInt();
            JsonObject images = result.get("images").getAsJsonObject();
            int count = images.get("count").getAsInt();
            JsonArray rows = images.get("rows").getAsJsonArray();
            if (status == 0) {
                for (JsonElement rowElement : rows) {
                    JsonObject row = rowElement.getAsJsonObject();
                    String url = Utils.getStringProperty(row, "url");
                    Integer label = Utils.getIntegerProperty(row, "label");
                    Integer labelLevel = Utils.getIntegerProperty(row, "labelLevel");
                    Long beginTime = Utils.getLongProperty(row, "beginTime");
                    Long endTime = Utils.getLongProperty(row, "endTime");
                    System.out.printf("url=%s, label=%s, labelLevel=%s, [beginTime=%s -- endTime=%s], ",
                            url, label, labelLevel, beginTime, endTime);
                }
                System.out.println(String.format("live data query success, images: %s", rows));
            } else if (status == 20) {
                System.out.println("taskId is expired");
            } else if (status == 30) {
                System.out.println("taskId is not exist");
            }
        } else {
            System.out.printf("ERROR: code=%s, msg=%s", code, msg);
        }
    }
}
