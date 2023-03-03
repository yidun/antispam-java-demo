package com.netease.is.antispam.demo.crawlersolution;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import com.netease.is.antispam.demo.utils.Utils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 调用易盾反垃圾云服务网站检测解决方案微博提交接口API实例
 */
public class WeiBoJobSubmitAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 易盾反垃圾云服务在线提交地址 */
    private final static String API_URL = "http://as.dun.163.com/v1/crawler/weibo-job/submit";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v1.0");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 2.设置私有参数
        params.put("dataId", "6a7c754f9de34eb8bfdf03f209fcfc02");
        params.put("blogger", "xxx微博");
        params.put("url", "http://xxx.com");
        params.put("strategy", "3");
        params.put("maxCheckCount", "100");
        params.put("maxComment", "100");
        params.put("frequency", "86400000");
        params.put("type", "6");
        params.put("startTime", String.valueOf(System.currentTimeMillis()));
        params.put("endTime", String.valueOf(System.currentTimeMillis()));
        // 多个检测项时用英文逗号分隔
        params.put("checkFlags", "1,2");
        params.put("callbackUrl", "http://xxx.com");

        // 预处理参数
        params = Utils.pretreatmentParams(params);
        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject result = jObject.getAsJsonObject("result");
            System.out.println(String.format("SUCCESS: taskId=%s, dataId=%s", result.get("taskId").getAsString(),
                    result.get("dataId").getAsString()));
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
