package com.sipc.catalina.exception;


import com.sipc.catalina.enums.ExamErrorCodeEnum;

public class ExamException extends RuntimeException {

    private Integer code;
    private String message;

    public ExamException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ExamException(String message) {
        this.code=500;
        this.message = message;
    }

    public ExamException(ExamErrorCodeEnum errorCodeEnum) {
        this.code=errorCodeEnum.getCode();
        this.message=errorCodeEnum.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
