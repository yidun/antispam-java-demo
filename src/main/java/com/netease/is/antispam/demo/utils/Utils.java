/*
 * @(#) Utils.java 2020-09-28
 *
 * Copyright 2020 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

/**
 * @author yd-dev
 * @version 2020-09-28
 */
public class Utils {

    public static final String SECRET_ID = "secretId";
    public static final String BUSINESS_ID = "businessId";
    public static final String VERSION = "version";
    public static final String TIMESTAMP = "timestamp";
    public static final String NONCE = "nonce";
    public static final String SIGNATURE_METHOD = "signatureMethod";
    public static final String SIGNATURE = "signature";

    public static Map<String, String> getCommonParams(String secretId, String version, String signatureMethod) {
        return getCommonParams(secretId, "", version, signatureMethod);
    }

    public static Map<String, String> getCommonParams(String secretId, String businessId, String version, String signatureMethod) {
        Map<String, String> params = new HashMap<>();
        params.put(SECRET_ID, secretId);
        if (StringUtils.isNotBlank(businessId)) {
            params.put(BUSINESS_ID, businessId);
        }
        params.put(VERSION, version);
        params.put(TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        params.put(NONCE, String.valueOf(new Random().nextInt()));
        params.put(SIGNATURE_METHOD, signatureMethod);
        return params;
    }

    public static void sign(Map<String, String> params, String secretKey) {
        params.remove(SIGNATURE);
        String signature = SignatureUtils.genSignature(secretKey, params);
        params.put(SIGNATURE, signature);
    }

    public static String getStringProperty(JsonObject obj, String memberName) {
        return obj.has(memberName) ? obj.get(memberName).getAsString() : "无";
    }

    public static Integer getIntegerProperty(JsonObject obj, String memberName) {
        return obj.has(memberName) ? obj.get(memberName).getAsInt() : null;
    }

    public static Long getLongProperty(JsonObject obj, String memberName) {
        return obj.has(memberName) ? obj.get(memberName).getAsLong() : null;
    }
}
