package com.wff.mall.auth.feign;

import com.wff.common.utils.R;
import com.wff.mall.auth.vo.SocialUser;
import com.wff.mall.auth.vo.UserLoginVo;
import com.wff.mall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/29 20:01
 */
@FeignClient("mall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo userRegisterVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R login(@RequestBody SocialUser socialUser);
}
