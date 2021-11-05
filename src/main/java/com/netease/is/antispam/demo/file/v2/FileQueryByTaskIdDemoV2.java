/*
 * @(#) filequerybytaskidAPIDemo.java 2019年11月22日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.file.v2;

import com.google.gson.*;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.*;

/**
 * 调用易盾反垃圾云服务文本结果查询接口API示例，该示例依赖以下jar包： 1. httpclient，用于发送http请求 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 * @author hzzhuxiafeng
 * @version 2020年11月01日
 */
public class FileQueryByTaskIdDemoV2 {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 易盾反垃圾云服务文本离线检测结果获取接口地址 */
    private final static String API_URL = "http://as-file.dun.163.com/v2/file/query";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 100000, 2000, 2000);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v2.0");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        Set<String> taskIds = new HashSet<String>();
        taskIds.add("26f8561ce4d144e8bf2c2aafd04ab645");
        params.put("taskIds", new Gson().toJson(taskIds));

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
            if (resultArray == null) {
                System.out.println("Can't find Data");
                return;
            }
            for (JsonElement jsonElement : resultArray) {
                JsonObject antispam = jsonElement.getAsJsonObject();
                JsonObject jObject = antispam.getAsJsonObject("antispam");
                String dataId = jObject.get("dataId").getAsString();
                String taskId = jObject.get("taskId").getAsString();
                int result = jObject.get("suggestion").getAsInt();
                String callback = !jObject.has("callback") ? "" : jObject.get("callback").getAsString();
                JsonObject evidencesObject = jObject.get("evidences").getAsJsonObject();
                System.out.println(String.format("SUCCESS: dataId=%s, taskId=%s, result=%s, callback=%s, evidences=%s",
                        dataId, taskId, result, callback, evidencesObject));
            }

        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }

    }
}
