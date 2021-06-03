package com.wff.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wff.common.utils.PageUtils;
import com.wff.mall.member.entity.MemberEntity;
import com.wff.mall.member.exception.PhoneExistException;
import com.wff.mall.member.exception.UserNameExistException;
import com.wff.mall.member.vo.MemberLoginVo;
import com.wff.mall.member.vo.SocialUser;
import com.wff.mall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 11:33:49
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo userRegisterVo);

    void checkPhone(String phone) throws PhoneExistException;

    void checkUserName(String username) throws UserNameExistException;

    /**
     * 普通登录
     */
    MemberEntity login(MemberLoginVo vo);

    /**
     * 社交登录
     */
    MemberEntity login(SocialUser socialUser);
}

