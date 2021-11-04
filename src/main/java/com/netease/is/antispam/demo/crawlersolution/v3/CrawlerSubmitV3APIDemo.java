/*
 * @(#) CrawlerSubmitAPIDemo.java 2020-04-23
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.crawlersolution.v3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;

/**
 * 调用易盾反垃圾云服务网站检测解决方案提交接口V3 API实例
 *
 * @author huangwu
 * @version 2020-09-23
 */
public class CrawlerSubmitV3APIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 易盾反垃圾云服务文档检测在线提交地址 */
    private final static String API_URL = "http://as.dun.163.com/v3/crawler/submit";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v3.0");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        params.put("dataId", "6a7c754f9de34eb8bfdf03f209fcfc02");
        params.put("callback", "34eb8bfdf03f209fcfc02");
        params.put("url", "http://xxx.com");
        // 多个检测项时用英文逗号分隔
        params.put("checkFlags", "1,2");
        // 回调地址。调用方用来接收易盾主动回调结果的api地址
        params.put("callbackUrl", "http://xxx");

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
            JsonObject result = jObject.getAsJsonObject("result");
            System.out.println(String.format("SUCCESS: taskId=%s, dataId=%s", result.get("taskId").getAsString(),
                    result.get("dataId").getAsString()));
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
