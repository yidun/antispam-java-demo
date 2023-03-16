/*
 * @(#) LabelQueryApiDemo.java 2023-03-16
 *
 * Copyright 2023 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.label;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author zhuliyang02
 * @version 2023-03-16
 */
public class LabelQueryApiDemo {

    /**
     * 商户密钥，请联系支持人员配置，对应客户秘钥-AccessKey SECRETID
     */
    private final static String SECRETID = "SECRETID";
    /**
     * 商户密钥，请联系支持人员配置，对应客户秘钥-AccessKey SECRETKEY
     */
    private final static String SECRETKEY = "SECRETKEY";
    /**
     * 业务ID，易盾根据产品业务特点分配
     */
    private final static String BUSINESSID = "your_business_id";
    /**
     * 易盾反垃圾云服务文本在线检测接口地址
     */
    private final static String API_URL = "https://openapi.dun.163.com/openapi/v2/antispam/label/query";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 2000, 2000, 2000);


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> headers = new HashMap<String, String>();
        // 1.设置查询参数
        params.put("clientId", "clientId");
        params.put("businessId", BUSINESSID);
        //指定标签支持的业务类型
        List<String> businessTypes = Arrays.asList(LabelBusinessTypeEnum.TEXT.getCode(), LabelBusinessTypeEnum.IMAGE.getCode());
        params.put("businessTypes", StringUtils.join(businessTypes, ","));
        //指定标签的最大层级
        params.put("maxDepth", "3");

        //2.设置header
        headers.put("X-YD-SECRETID", SECRETID);
        headers.put("X-YD-TIMESTAMP", String.valueOf(System.currentTimeMillis()));
        headers.put("X-YD-NONCE", String.valueOf(new Random().nextInt()));

        // 生成签名信息
        String signature = SignatureUtils.genOpenApiSignature(SECRETKEY, params, headers);
        headers.put("X-YD-SIGN", signature);

        // 3.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendGetByHeader(httpClient, API_URL, params, headers);

        // 4.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            //标签列表，结构请参官网考
            JsonArray jsonArray = jObject.getAsJsonArray("data");
            jsonArray.forEach(jsonElement -> {
                System.out.println(jsonElement.toString());
            });
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }

    }

}
