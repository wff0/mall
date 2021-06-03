package com.wff.mall.order.interceptor;

import com.wff.common.constant.AuthServerConstant;
import com.wff.common.vo.MemberRsepVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 20:12
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberRsepVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean match1 = matcher.match("/order/order/status/**", requestURI);
        boolean match2 = matcher.match("/payed/notify", requestURI);
        if (match1 || match2) {
            return true;
        }
        HttpSession session = request.getSession();
        MemberRsepVo memberRsepVo = (MemberRsepVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberRsepVo != null) {
            threadLocal.set(memberRsepVo);
            return true;
        } else {
            // 没登陆就去登录
            session.setAttribute("msg", AuthServerConstant.NOT_LOGIN);
            response.sendRedirect("http://auth.mall.com/login.html");
            return false;
        }
    }
}
