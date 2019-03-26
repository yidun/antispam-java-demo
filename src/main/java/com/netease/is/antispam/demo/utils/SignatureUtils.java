/*
 * @(#) SignatureUtils.java 2016年2月2日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.antispam.demo.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成及验证签名信息工具类
 *
 * @author hzgaomin
 * @version 2016年2月2日
 */
public class SignatureUtils {

    /**
     * 通过HttpServletRequest做签名验证
     *
     * @param request
     * @param secretid
     * @param secretkey
     * @param businessid
     * @return
     */
    public static boolean verifySignature(HttpServletRequest request, String secretid, String secretkey, String businessid)
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
        String serverSignature = genSignature(secretkey, params);
        // 客户根据需要确认是否鉴权是否要精确到业务维度，不需要则去掉businessid.equals(businessId)
        return signature.equals(serverSignature) && secretid.equals(secretId) && businessid.equals(businessId);
    }

    /**
     * 生成签名信息
     *
     * @param secretKey 产品私钥
     * @param params    接口请求参数名和参数值map，不包括signature参数名
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String genSignature(String secretKey, Map<String, String> params) throws UnsupportedEncodingException {
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuffer paramBuffer = new StringBuffer();
        for (String key : keys) {
            paramBuffer.append(key).append(params.get(key) == null ? "" : params.get(key));
        }
        // 3. 将secretKey拼接到最后
        paramBuffer.append(secretKey);

        // 4. MD5是128位长度的摘要算法，用16进制表示，一个十六进制的字符能表示4个位，所以签名后的字符串长度固定为32个十六进制字符。
        return DigestUtils.md5Hex(paramBuffer.toString().getBytes("UTF-8"));
    }

}
