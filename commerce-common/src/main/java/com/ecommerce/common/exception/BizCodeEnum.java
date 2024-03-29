package com.ecommerce.common.exception;

public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),

    PRODUCT_DP_EXCEPTION(11000, "商品上架异常"),;

    private int code;
    private String message;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
