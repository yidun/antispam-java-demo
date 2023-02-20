/*
 * @(#) DemoController.java 2019-03-25
 *
 * Copyright 2019 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.activecallback;

import static com.netease.is.antispam.demo.utils.SignatureUtils.verifySignature;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 易盾反垃圾调用客户回调数据接收接口示例
 *
 * @author hzhumin1
 * @version 2019-03-25
 */
@RestController
public class CallbackReceiveController {
    /**
     * 产品密钥ID，产品标识
     */
    private static final String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private static final String SECRETKEY = "your_secret_key";
    /**
     * 业务ID，易盾根据产品业务特点分配
     */
    private static final String BUSINESSID = "your_business_id";

    /**
     * 文本回调数据接收接口demo
     *
     * @param request
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value = "/text/callback/receive")
    public void textCallbackReceive(HttpServletRequest request) throws UnsupportedEncodingException {
        boolean verifyFlag = verifySignature(request, SECRETID, SECRETKEY, BUSINESSID);
        if (!verifyFlag) {
            throw new RuntimeException("signature verify failed");
        }
        String callbackData = request.getParameter("callbackData");
        JsonObject resultObject = new JsonParser().parse(callbackData).getAsJsonObject();
        int action = resultObject.get("action").getAsInt();
        String taskId = resultObject.get("taskId").getAsString();
        String callback = resultObject.get("callback").getAsString();
        JsonArray labelArray = resultObject.getAsJsonArray("labels");
        /*for (JsonElement labelElement : labelArray) {
            JsonObject lObject = labelElement.getAsJsonObject();
            int label = lObject.get("label").getAsInt();
            int level = lObject.get("level").getAsInt();
            JsonObject detailsObject=lObject.getAsJsonObject("details");
            JsonArray hintArray=detailsObject.getAsJsonArray("hint");
        }*/
        if (action == 0) {
            System.out.println(String.format("taskId=%s，callback=%s，文本人工复审结果：通过", taskId, callback));
        } else if (action == 2) {
            System.out.println(String.format("taskId=%s，callback=%s，文本人工复审结果：不通过，分类信息如下：%s", taskId, callback,
                    labelArray.toString()));
        }
    }

    /**
     * 图片回调数据接收接口demo
     *
     * @param request
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value = "/image/callback/receive")
    public void imageCallbackReceive(HttpServletRequest request) throws UnsupportedEncodingException {
        boolean verifyFlag = verifySignature(request, SECRETID, SECRETKEY, BUSINESSID);
        if (!verifyFlag) {
            throw new RuntimeException("signature verify failed");
        }
        String callbackData = request.getParameter("callbackData");
        JsonObject resultObject = new JsonParser().parse(callbackData).getAsJsonObject();
        String name = resultObject.get("name").getAsString();
        String taskId = resultObject.get("taskId").getAsString();
        JsonArray labelArray = resultObject.get("labels").getAsJsonArray();
        System.out.println(String.format("taskId=%s，name=%s，labels：", taskId, name));
        int maxLevel = -1;
        // 产品需根据自身需求，自行解析处理，本示例只是简单判断分类级别
        for (JsonElement labelElement : labelArray) {
            JsonObject lObject = labelElement.getAsJsonObject();
            int label = lObject.get("label").getAsInt();
            int level = lObject.get("level").getAsInt();
            double rate = lObject.get("rate").getAsDouble();
            System.out.println(String.format("label:%s, level=%s, rate=%s", label, level, rate));
            maxLevel = level > maxLevel ? level : maxLevel;
        }
        switch (maxLevel) {
            case 0:
                System.out.println("#图片人工复审结果：最高等级为\"正常\"\n");
                break;
            case 2:
                System.out.println("#图片人工复审结果：最高等级为\"确定\"\n");
                break;
            default:
                break;
        }
    }

}
