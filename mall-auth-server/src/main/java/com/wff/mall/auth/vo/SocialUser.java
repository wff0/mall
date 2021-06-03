package com.wff.mall.auth.vo;

import lombok.Data;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/29 20:02
 */
@Data
public class SocialUser {
    private String accessToken;

    private String remindIn;

    private int expiresIn;

    private String uid;

    private String isrealname;
}
