/*
 * @(#) MediaSolutionSubmitAPIDemo.java 2020-06-23
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.mediasolution;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;

/**
 * 调用易盾反垃圾融媒体解决方案检测提交接口API示例
 *
 * @author maxiaofeng
 * @version 2020-06-23
 */
public class MediaSolutionSubmitAPIDemo {

    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾直播音视频解决方案在线检测接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v1/mediasolution/submit";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
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
        params.put("version", "v1.0");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.设置私有参数
        params.put("title", "融媒体解决方案的标题");
        JsonArray jsonArray = new JsonArray();
        // 传图片url进行检测，name结构产品自行设计，用于唯一定位该图片数据
        JsonObject text = new JsonObject();
        text.addProperty("type", "text");
        text.addProperty("data", "融媒体文本段落");
        jsonArray.add(text);
        JsonObject image = new JsonObject();
        image.addProperty("type", "image");
        image.addProperty("data", "http://xxx");
        jsonArray.add(image);
        JsonObject audio = new JsonObject();
        audio.addProperty("type", "audio");
        audio.addProperty("data", "http://xxx");
        jsonArray.add(audio);
        JsonObject video = new JsonObject();
        video.addProperty("type", "video");
        video.addProperty("data", "http://xxx");
        jsonArray.add(video);
        JsonObject audiovideo = new JsonObject();
        audiovideo.addProperty("type", "audiovideo");
        audiovideo.addProperty("data", "http://xxx");
        jsonArray.add(audiovideo);
        JsonObject file = new JsonObject();
        file.addProperty("type", "file");
        file.addProperty("data", "http://xxx");
        jsonArray.add(file);
        params.put("content", jsonArray.toString());

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject result = jObject.get("result").getAsJsonObject();
            String taskId = result.get("taskId").getAsString();
            String dataId = result.get("dataId").getAsString();
            System.out.println(String.format("SUBMIT SUCCESS: taskId=%s, dataId=%s", taskId, dataId));
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
