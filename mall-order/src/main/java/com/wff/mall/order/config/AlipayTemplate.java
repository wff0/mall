package com.wff.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.wff.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/3 16:42
 */
@ConfigurationProperties(prefix = "alipay")
@Data
@Component
public class AlipayTemplate {
    //在支付宝创建的应用的id
    private String app_id = "2021000117669585";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCUX87S1YJY4Cyu1RAEzidTmm1cHdcZmFHpXnNL242GjoZZXXpXhc84jHNflsM/QH1472rKupzGKWbemGpk6jNOtpn+JL+hZSfTPiGUdDqJ5HWRFf49U27BiPq5bPDKbM/MjoTFuDMRme6HyvpWyONAcDiSxME7uNZJ8itwezT5Dt69LJlKfQEHCkOFTsZBFqIE5KGuyoJ9+5z5G10o5A2T71pHZfGe1UnmKwcxnUTrBfyK3e14h1MWGiH9pNPmx4W5sq3Lnxwo9n2GH2L1BLqJiMJOI7f64dBiIbZJ4bkSr9WrTvN+bTOMPC3TaNLVz5yoWv8UePDHp4SAVca8pjvFAgMBAAECggEAD2SNIWRTHINNP2oNOmJkxBCeTdMusJIT1WEeFv2VBiOdHoJLIe9Y2yX/Biiu4s3+9l/oNVX807YR3P/08bk2T3E0MqB/XPAQnKKyeyM/u3R/GektMhGIyP07/aamqGl3eWJDtftxGN9eVqohJIW8xb+eruNIpLh9Srib9xRhH4FZ5Tpu9liTYzlTM8MLnXrb+zO+cYIHena+IPUiXAxBvT6tdaXobeGjkU+R5MySgfbUTMeEhJcWGrYu4XDL7e9s5XMUR+XUJ5eJYsbgD+vWwUF+LMzxgFc1J6pbjbPn7pa/D6GVybl3VqhT/dzJOON5kwwI21fhR9dAIsv2cx0lNQKBgQDQRSAr5OfKPI4pk5b4UixohOGopGmN9cahkyGPoQQzz7AO911HPSWpuZCqV3gVSds8I7ykQD9aS8Hrv8qUzFIGjv2vzeotHC83cxBUDaEVdkAuvZG028FZq9psPEr8uidtivYef75tx3+5hKrgzKyXPFDGy/c9u1ZrpqlKqzbTuwKBgQC2YLEJjOjZuKX0egLU9FtQzgAf43c+/YYtJhHGjuRqJTmBvvtyUyZKLXa8U0GQhvK27dueFbF8ZNKF2nY6LlOAtx1MzH2ewjzmuezgL0WSlZb9lVkITpR9iCwiLTzCgJHVRFswNybrapu/V4+hK5ALsLuke4krNUNHiaiFl2F2fwKBgQC2Uo1jX7R6mqBpTUbwhB9UMF/L3oJ5QBtu/vp2XiLrqHkb4PjLkIAUINrABGqfWFSQRUizEXtkdMWCuBeEQ4nbK/pxmPXUH8RF6nj03CmUPDzbriCMD4UoFwXTksSikeRKRUhIUtRGDkuGANoGiabdjnO4VH5QkFuLSg8RyujoWQKBgQCgqz5MM59Pb6SEZJjqEvv/i/wAG9yJ2b/DNq6iRnBkMrce3qmWZjptTvAzqQUx1jEthw7bjjD2bbwpv/Q3/WgDFU7ywZRJsMUGXvWX6w1XvgFvFYuCDfgzPyviUAgfMjVHtRjAuVXIP/tF7lXSnxYftmsY1Nxa/GlxNI0KY/qw8QKBgC3/o1AlGVlkp6g4SKUFcRIZVaVHXrOIt0zCFmjy73JXJXnXT8jxJKAZlp6PllvJYfYP4lDvhdSrudzBn6I6H0GyxxSknbRM6qc0p97WlZBUgxzYSR8uUqCgbFcaN4fKtxKm0rWIwPi03koVRxKphrWK2NbUjyQMOljRTv4J3kuN";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhoj6Uq3PpSnYvgQFfHsVZ7UCEcNec5SIrZBJYpyj49cG4b/lsudPBdX8d2jdQTnVHzya2KQb1OoVKoSgjRuP909Lyr1YrGxuf8gQqInexAfnEG36sXe4EOge4QDwMYrzlgArCMiiRwb4I3nCOAUuEIvHTqDtdovpzh1op6BwAB6W4CZ+Jza0nUxa7ppDx2/iJCl00YLpEpeJ9nen4fJvJM4FP+b0IRLFs+VnZDXqGVok3xlz7hNtw717KkHNIDjCKD03sbYl+wlWZWaT1tPm0fe3o1+0CiRZCH97uW9JmpbTeIu94BnLpnBqBVwizjgBr5AeWdu9Ql8WaZdInzGlWwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://4o0113122k.qicp.vip/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.mall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 自动关单时间
    private String timeout = "1m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        // 30分钟内不付款就会自动关单
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        return result;
    }
}
