/*
 * @(#) TextCallbackAPIDemo.java 2016年12月28日
 * 
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.video;

import com.google.gson.*;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.*;

/**
 * 调用易盾反垃圾云服务点播视频结果查询接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 * 
 * @author hzhumin1
 * @version 2017年5月27日
 */
public class VideoQueryByTaskIdsDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务点播查询检测结果获取接口地址 */
    private final static String API_URL = "https://as.dun.163yun.com/v1/video/query/task";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
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
        params.put("businessId", BUSINESSID);
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.设置私有参数
        Set<String> taskIds = new HashSet<String>();
        taskIds.add("c679d93d4a8d411cbe3454214d4b1fd7");
        taskIds.add("49800dc7877f4b2a9d2e1dec92b988b6");
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
            for (JsonElement jsonElement : resultArray) {
                JsonObject jObject = jsonElement.getAsJsonObject();
                int status = jObject.get("status").getAsInt();
                if(status!=0){//-1:提交检测失败，0:正常，10：检测中，20：不是7天内数据，30：taskId不存在，110：请求重复，120：参数错误，130：解析错误，140：数据类型错误
                    System.out.println("获取结果异常，status="+status);
                    continue;
                }
                String taskId = jObject.get("taskId").getAsString();
                String callback = jObject.get("callback").getAsString();
                int videoLevel = jObject.get("level").getAsInt();
                if (videoLevel == 0) {
                    System.out.println(String.format("正常, callback=%s", callback));
                } else if (videoLevel == 1 || videoLevel == 2) {
                    JsonArray evidenceArray = jObject.get("evidences").getAsJsonArray();
                    for (JsonElement evidenceElement : evidenceArray) {
                        JsonObject eObject = evidenceElement.getAsJsonObject();
                        long beginTime = eObject.get("beginTime").getAsLong();
                        long endTime = eObject.get("endTime").getAsLong();
                        int type = eObject.get("type").getAsInt();
                        String url = eObject.get("url").getAsString();
                        JsonArray labelArray = eObject.get("labels").getAsJsonArray();
                        for (JsonElement labelElement : labelArray) {
                            JsonObject lObject = labelElement.getAsJsonObject();
                            int label = lObject.get("label").getAsInt();
                            int level = lObject.get("level").getAsInt();
                            double rate = lObject.get("rate").getAsDouble();
                        }
                        System.out.println(String.format("%s, callback=%s, 证据信息：%s, 证据分类：%s, ", videoLevel == 1 ? "不确定"
                                : "确定", callback, eObject, labelArray));
                    }
                }
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }

    }
}
