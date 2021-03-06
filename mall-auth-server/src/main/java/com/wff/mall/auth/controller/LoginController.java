package com.wff.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.wff.common.constant.AuthServerConstant;
import com.wff.common.exception.BizCodeEnum;
import com.wff.common.utils.R;
import com.wff.common.vo.MemberRsepVo;
import com.wff.mall.auth.feign.MemberFeignService;
import com.wff.mall.auth.feign.ThirdPartFeignService;
import com.wff.mall.auth.vo.UserLoginVo;
import com.wff.mall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/28 23:34
 */
@Slf4j
@Controller
public class LoginController {
    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping({"/login.html", "/", "/index", "/index.html"})
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            return "login";
        }
        return "redirect:http://mall.com";
    }

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes, HttpSession session) {
        // ????????????
        R r = memberFeignService.login(userLoginVo);
        if (r.getCode() == 0) {
            // ????????????
            MemberRsepVo rsepVo = r.getData("data", new TypeReference<MemberRsepVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, rsepVo);
            log.info("\n?????? [" + rsepVo.getUsername() + "] ??????");
            return "redirect:http://mall.com";
        } else {
            HashMap<String, String> error = new HashMap<>();
            // ??????????????????
            error.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.mall.com/login.html";
        }
    }

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendcode(@RequestParam("phone") String phone) {

        // TODO ????????????
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.hasLength(redisCode)) {
            long CuuTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - CuuTime < 60 * 1000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 6);
        String redis_code = code + "_" + System.currentTimeMillis();
        // ???????????????
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redis_code, 1, TimeUnit.MINUTES);
        try {
            return thirdPartFeignService.sendCode(phone, code);
        } catch (Exception e) {
            log.warn("??????????????????????????? [????????????]");
        }
        return R.ok();
    }

    /**
     * TODO ?????????????????????,??????session?????? ???????????????sessoin??? ?????????????????????
     * <p>
     * TODO 1. ???????????????session??????
     * ??????
     * RedirectAttributes redirectAttributes ??? ???????????????????????????
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // ??????????????????????????????????????????
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, fieldError -> fieldError.getDefaultMessage()));
            // addFlashAttribute ????????????????????????
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/reg.html";
        }
        // ???????????? ??????????????????
        // 1.???????????????
        String code = vo.getCode();

        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.hasLength(redisCode)) {
            // ???????????????
            if (code.equals(redisCode.split("_")[0])) {
                // ???????????????
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                // ??????????????????????????????
                R r = memberFeignService.register(vo);
                if (r.getCode() == 0) {
                    // ??????
                    return "redirect:http://auth.mall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "???????????????");
                // addFlashAttribute ????????????????????????
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.mall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "???????????????");
            // addFlashAttribute ????????????????????????
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/reg.html";
        }
    }
}
