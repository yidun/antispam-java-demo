/*
 * @(#) ListSubmitAPIDemo.java 2020-01-02
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.list;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import com.netease.is.antispam.demo.utils.Utils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 调用易盾反垃圾云服务名单批量提交接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * <p>
 * 自定义用户名单删除接口
 *
 * @author zhaojipu
 * @version 2023年02月07日
 */
public class ListDeleteAPIDemo {
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
     * 易盾反垃圾云服务自定义用户名单删除接口地址
     */
    private final static String API_URL = "https://as.dun.163yun.com/v2/list/batchDelete";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256
        params.put("version", "v2");

        // 2.设置私有参数
        Set<String> listSet = new HashSet<>();
        listSet.add("用户黑名单1");
        listSet.add("用户黑名单2");

        Set<String> uuidSet = new HashSet<>();
        uuidSet.add("用户名单uuid1");
        uuidSet.add("用户名单uuid2");

        // 名单分类，1: 白名单，2: 黑名单
        params.put("listType", "2");
        // 名单类型，1: 用户名单，2: IP名单，3: 设备名单
        params.put("entityType", "1");
        // uuids和entities传一个即可，名单唯一标识id列表数组,json字符串
        params.put("uuids", new Gson().toJson(uuidSet));
        params.put("entities", new Gson().toJson(listSet));

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
            Boolean result = resultObject.get("result").getAsBoolean();
            System.out.println(String.format("用户名单删除返回 result=%s", result));
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
