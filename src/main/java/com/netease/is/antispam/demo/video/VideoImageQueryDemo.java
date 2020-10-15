package com.netease.is.antispam.demo.video;

import com.google.gson.*;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.*;

/**
 * 调用易盾反垃圾云服务视频截图查询接口API示例，该示例依赖以下jar包：
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 * @author wangmiao5
 * @version 2020-07-16 17:41
 */
public class VideoImageQueryDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "your_secret_id";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "your_secret_key";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "your_business_id";
    /** 易盾反垃圾云服务视频截图查询获取接口地址 */
    private final static String API_URL = "http://as.dun.163.com/v1/video/query/image";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 1000, 1000);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        params.put("signatureMethod", "MD5"); // MD5, SM3, SHA1, SHA256

        // 2.设置私有参数
        params.put("taskId", "4bc345f4bdc74a92b64543b35412d678");
        params.put("levels", "[0,1,2]");
        params.put("pageNum", "1");
        params.put("pageSize", "20");
        params.put("orderType", "3"); // 详情查看官网VideoDataOderType

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject resultObject = new JsonParser().parse(response).getAsJsonObject();
        int code = resultObject.get("code").getAsInt();
        String msg = resultObject.get("msg").getAsString();
        if (code == 200) {
            JsonObject result = resultObject.getAsJsonObject("result");
            int status = result.get("status").getAsInt();
            if (status == 0) {
                JsonObject images = result.getAsJsonObject("images");
                long count = images.get("count").getAsLong();
                JsonArray rows = images.getAsJsonArray("rows");
                for (JsonElement row : rows) {
                    JsonObject rowObject = row.getAsJsonObject();
                    String url = rowObject.get("url").getAsString();
                    Integer label = rowObject.get("label").getAsInt();
                    Integer labelLevel = rowObject.get("labelLevel").getAsInt();
                    Long beginTime = rowObject.get("beginTime").getAsLong();
                    Long endTime = rowObject.get("endTime").getAsLong();
                    System.out.println(
                            String.format("成功, count: %s, url: %s, label: %s, labelLevel: %s, 开始时间: %s, 结束时间: %s",
                                    count, url, label, labelLevel, beginTime, endTime));
                }
            } else if (status == 20) {
                System.out.println("taskId不是7天内数据");
            } else if (status == 30) {
                System.out.println("taskId不存在");
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
    }
}
