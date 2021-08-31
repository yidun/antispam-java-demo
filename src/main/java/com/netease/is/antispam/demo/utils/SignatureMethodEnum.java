package com.netease.is.antispam.demo.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 签名方式
 *
 * @author yd-dev
 * @version 2020-09-15
 */
public enum SignatureMethodEnum {
    MD5,
    SM3,
    SHA1,
    SHA256;
    public static boolean isValid(String signatureMethod) {
        try {
            SignatureMethodEnum signatureMethodEnum = SignatureMethodEnum.valueOf(StringUtils.upperCase(signatureMethod));
            return signatureMethodEnum != null;
        } catch (Exception e) {
            return false;
        }
    }
}
