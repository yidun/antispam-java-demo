package com.netease.is.antispam.demo.livevideosolution;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.activecallback.ActiveCallbackResp;
import com.netease.is.antispam.demo.utils.SignatureUtils;

/**
 * 主动回调解析结果demo
 */
@RestController
@RequestMapping("callback/receive")
public class LiveVideoSolutionActiveCallbackDemo {

    /**
     * 产品密钥ID，产品标识
     */
    private static final String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private static final String SECRETKEY = "your_secret_key";

    @RequestMapping("/liveSolution")
    public ActiveCallbackResp receiveCallback(HttpServletRequest request) {
        try {
            if (!SignatureUtils.verifySignature(request.getParameterMap(), SECRETKEY)) {
                return ActiveCallbackResp.fail(ActiveCallbackResp.SIGN_ERROR);
            }
            String callbackData = request.getParameter("callbackData");
            JsonObject resultObject = new JsonParser().parse(callbackData).getAsJsonObject();
            // 根据需要解析字段，具体返回字段的说明，请参考官方接口文档中字段说明
            // https://support.dun.163.com/documents/594247746924453888?docId=600827721634357248#%E5%93%8D%E5%BA%94%E7%BB%93%E6%9E%9C

            return ActiveCallbackResp.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return ActiveCallbackResp.fail(ActiveCallbackResp.SERVER_ERROR);
        }
    }
}
