package com.wff.mall.order.config;

import com.wff.mall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 20:14
 */
@Configuration
public class OrderWebConfiguration implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 放行支付宝回调请求
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**").excludePathPatterns("/payed/notify");
    }
}
