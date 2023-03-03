/*
 * @(#) CrawlerResourceCallbackReceiveController.java 2023-03-03
 *
 * Copyright 2023 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.crawlersolution.v3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

import static com.netease.is.antispam.demo.utils.SignatureUtils.verifySignature;

/**
 * 主站检测任务结果获取-推送
 */
@RestController
public class CrawlerJobCallbackReceiveV3Demo {

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
     * 网站任务回调数据接收接口demo
     *
     * @param request
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value = "/crawlerJob/callback/receive")
    public void crawlerResourceCallbackReceive(HttpServletRequest request) throws UnsupportedEncodingException {
        boolean verifyFlag = verifySignature(request, SECRETID, SECRETKEY, BUSINESSID);
        if (!verifyFlag) {
            throw new RuntimeException("signature verify failed");
        }
        String callbackData = request.getParameter("callbackData");
        JsonObject resultObject = new JsonParser().parse(callbackData).getAsJsonObject();

        JsonArray resultArray = resultObject.getAsJsonArray("result");
        if (null == resultArray || resultArray.size() == 0) {
            System.out.println("暂时没有结果需要获取，请稍后重试！");
        } else {
            for (JsonElement jsonElement : resultArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonObject machineCheckResult = jsonObject.getAsJsonObject("antispam");
                if (machineCheckResult != null) {
                    System.out.printf("机器检测结果:%s%n", machineCheckResult);
                }
                JsonObject censorResult = jsonObject.getAsJsonObject("censor");
                if (censorResult != null) {
                    System.out.printf("人工审核结果:%s%n", censorResult);
                }
            }
        }

    }

}
