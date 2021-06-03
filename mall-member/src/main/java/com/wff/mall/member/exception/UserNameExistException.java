package com.wff.mall.member.exception;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/29 20:34
 */
public class UserNameExistException extends RuntimeException {
    public UserNameExistException() {
        super("用户名存在");
    }
}
