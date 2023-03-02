/*
 * @(#) SignatureUtils.java 2016年2月2日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.digests.SM3Digest;

/**
 * 生成及验证签名信息工具类
 *
 * @author hzgaomin
 * @version 2016年2月2日
 */
public class SignatureUtils {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f'};

    /**
     * 通过HttpServletRequest做签名验证
     *
     * @param request
     * @param secretid
     * @param secretkey
     * @param businessid
     * @return
     */
    public static boolean verifySignature(HttpServletRequest request, String secretid, String secretkey,
            String businessid)
            throws UnsupportedEncodingException {
        String secretId = request.getParameter("secretId");
        String businessId = request.getParameter("businessId");
        String signature = request.getParameter("signature");
        if (StringUtils.isEmpty(secretId) || StringUtils.isEmpty(signature)) {
            // 签名参数为空，直接返回失败
            return false;
        }
        Map<String, String> params = new HashMap<>();
        for (String paramName : request.getParameterMap().keySet()) {
            if (!"signature".equals(paramName)) {
                params.put(paramName, request.getParameter(paramName));
            }
        }
        // SECRETKEY:产品私有密钥 SECRETID:产品密钥ID BUSINESSID:业务ID,开通服务时，易盾会提供相关密钥信息
        String serverSignature = genSignature(secretkey, request.getParameter("signatureMethod"), params);
        // 客户根据需要确认是否鉴权是否要精确到业务维度，不需要则去掉businessid.equals(businessId)
        return signature.equals(serverSignature) && secretid.equals(secretId) && businessid.equals(businessId);
    }

    /**
     * 验证签名是否匹配
     *
     * @param requestParams 签名的参数
     * @param secretKey 签名的key
     * @return 是否匹配
     */
    public static boolean verifySignature(Map<String, String[]> requestParams, String secretKey) {
        if (StringUtils.isBlank(secretKey) || requestParams == null) {
            return false;
        }

        Map<String, String> params = new HashMap<>();
        String signature = null;
        for (String paramName : requestParams.keySet()) {
            String[] value = requestParams.get(paramName);
            if (value == null || value.length == 0) {
                continue;
            }

            if ("signature".equals(paramName)) {
                signature = value[0];
            } else {
                params.put(paramName, value[0]);
            }
        }
        if (StringUtils.isBlank(signature)) {
            // 签名为空，直接返回失败
            return false;
        }
        String generatedSignature = genSignature(secretKey, params);
        if (StringUtils.isBlank(generatedSignature)) {
            return false;
        }

        return signature.equals(generatedSignature);
    }

    /**
     * 默认使用md5方式
     *
     * @param secretKey
     * @param params
     * @return
     */
    public static String genSignature(String secretKey, Map<String, String> params) {
        return genSignature(secretKey, params.get("signatureMethod"), params);
    }

    /**
     * 通用签名方式
     * 
     * @param secretKey
     * @param signatureMethod
     * @param params
     * @return
     */
    public static String genSignature(String secretKey, String signatureMethod, Map<String, String> params) {
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        // 2. 按照排序拼接参数名与参数值
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append(params.get(key));
        }
        // 3. 将secretKey拼接到最后
        sb.append(secretKey);
        try {
            // 默认使用MD5
            SignatureMethodEnum signatureMethodEnum = StringUtils.isBlank(signatureMethod) ? SignatureMethodEnum.MD5
                    : SignatureMethodEnum.valueOf(StringUtils.upperCase(signatureMethod));
            switch (signatureMethodEnum) {
                case MD5:
                    return DigestUtils.md5Hex(sb.toString().getBytes("UTF-8"));
                case SHA1:
                    return DigestUtils.sha1Hex(sb.toString().getBytes("UTF-8"));
                case SHA256:
                    return DigestUtils.sha256Hex(sb.toString().getBytes("UTF-8"));
                case SM3:
                    return sm3DigestHex(sb.toString().getBytes("UTF-8"));
                default:
                    System.out.println("[ERROR] unsupported signature method: " + signatureMethod);
                    return null;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] not supposed to happen: " + e.getMessage());
        }
        return null;
    }

    public static String sm3DigestHex(byte[] srcData) {
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.update(srcData, 0, srcData.length);
        byte[] hash = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(hash, 0);
        return Hex.encodeHexString(hash);
    }

    public static String getCheckSum(String key, String secret, String nonce, String currentTime) {
        StringBuilder stringBuffer = new StringBuilder(key).append(secret).append(nonce).append(currentTime);
        return encode("sha1", stringBuffer.toString());
    }

    private static String encode(String algorithm, String value) {
        String result = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(value.getBytes("UTF-8"));
            result = getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    private static String getFormattedText(byte[] bytes) {
        int length = bytes.length;
        StringBuilder buf = new StringBuilder(length * 2);
        for (int j = 0; j < length; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

}
