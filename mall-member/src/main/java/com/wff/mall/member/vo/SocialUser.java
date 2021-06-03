package com.wff.mall.member.vo;

import lombok.Data;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/29 20:31
 */
@Data
public class SocialUser {
    private String accessToken;

    private String remindIn;

    private Long expiresIn;

    private String uid;

    private String isrealname;
}
