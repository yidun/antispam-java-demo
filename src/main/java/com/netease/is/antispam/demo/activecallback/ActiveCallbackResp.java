package com.netease.is.antispam.demo.activecallback;

/**
 * 主动回调响应
 */
public class ActiveCallbackResp {
    private int code;
    private String msg;

    public static final int SIGN_ERROR = 400;
    public static final int SERVER_ERROR = 500;

    public ActiveCallbackResp(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ActiveCallbackResp ok() {
        return new ActiveCallbackResp(200, null);
    }

    public static ActiveCallbackResp fail(int code) {
        return new ActiveCallbackResp(code, null);
    }
}
