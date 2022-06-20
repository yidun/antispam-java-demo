/*
 * @(#) LiveAudioCheckAPIDemo.java 2019-04-11
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.videosolution;

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
import com.netease.is.antispam.demo.utils.Utils;

/**
 * 调用易盾反垃圾点播音视频解决方案检测提交接口API示例
 *
 * @author maxiaofeng
 * @version 2019-06-10
 */
public class VideoSolutionSubmitAPIDemo {
    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾点播音视频解决方案在线检测接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v2/videosolution/submit";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        // 点播音视频解决方案版本v1.1及以上语音二级细分类subLabels结构进行调整
        params.put("version", "v2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 2.设置私有参数
        params.put("url", "http://xxx.xx");
        JsonArray jsonArray = new JsonArray();
        // 传图片url进行检测，name结构产品自行设计，用于唯一定位该图片数据
        JsonObject image1 = new JsonObject();
        image1.addProperty("name", "http://p1.music.126.net/lEQvXzoC17AFKa6yrf-ldA==/1412872446212751.jpg");
        image1.addProperty("type", 1);
        image1.addProperty("data", "http://p1.music.126.net/lEQvXzoC17AFKa6yrf-ldA==/1412872446212751.jpg");
        jsonArray.add(image1);
        // 传图片base64编码进行检测，name结构产品自行设计，用于唯一定位该图片数据
        // JsonObject image2 = new JsonObject();
        // image2.addProperty("name", "{\"imageId\": 33451123, \"contentId\": 78978}");
        // image2.addProperty("type", 2);
        // image2.addProperty("data","xxx");
        // jsonArray.add(image2);
        // params.put("images", jsonArray.toString());

        // 预处理参数
        params = Utils.pretreatmentParams(params);
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
            String dataId = result.has("dataId") ? result.get("dataId").getAsString() : "无";
            long dealingCount = result.get("dealingCount").getAsLong();
            System.out.println(String.format("SUBMIT SUCCESS: taskId=%s, dataId=%s, dealingCount=%s", taskId, dataId,
                    dealingCount));
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
