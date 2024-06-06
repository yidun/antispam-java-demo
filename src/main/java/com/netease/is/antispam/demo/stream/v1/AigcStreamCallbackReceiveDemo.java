package com.netease.is.antispam.demo.stream.v1;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.Utils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

import static com.netease.is.antispam.demo.utils.SignatureUtils.verifySignature;

/**
 * AIGC流式检测结果获取-推送
 * @author ruicha
 * @version 2024-06-05
 */
@RestController
public class AigcStreamCallbackReceiveDemo {

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
     * AIGC流式检测回调数据接收接口demo
     *
     * @param request
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value = "/stream/callback/receive")
    public void streamCallbackReceive(HttpServletRequest request) throws UnsupportedEncodingException {
        boolean verifyFlag = verifySignature(request, SECRETID, SECRETKEY, BUSINESSID);
        if (!verifyFlag) {
            throw new RuntimeException("signature verify failed");
        }
        String callbackData = request.getParameter("callbackData");
        JsonObject streamCheckResult = new JsonParser().parse(callbackData).getAsJsonObject();
        // sessionTaskId
        String sessionTaskId = Utils.getStringProperty(streamCheckResult.getAsJsonObject(), "sessionTaskId");
        // sessionId
        String sessionId = Utils.getStringProperty(streamCheckResult.getAsJsonObject(), "sessionId");
        JsonObject antispam = streamCheckResult.getAsJsonObject().getAsJsonObject("antispam");
        // antispam.suggestion
        String suggestion = Utils.getStringProperty(antispam, "suggestion");
        // antispam.label
        String label = Utils.getStringProperty(antispam, "label");
        System.out.printf("sessionTaskId=%s, sessionId=%s, suggestion=%s, label=%s%n",
                sessionTaskId, sessionId, suggestion, label);
    }

}
