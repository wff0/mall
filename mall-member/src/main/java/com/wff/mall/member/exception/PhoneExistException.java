package com.wff.mall.member.exception;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/29 20:34
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号存在");
    }
}
