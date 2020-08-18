package com.sipc.catalina.base;


public class ResponseMessage {
    private String code;
    private String message;
    private Object data;

    public ResponseMessage() {
    }

    public static ResponseMessage success() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode("200");
        return responseMessage;
    }

    public static ResponseMessage success(Object data) {
        ResponseMessage responseMessage = success();
        responseMessage.setData(data);
        return responseMessage;
    }

    public static ResponseMessage error(String code, String msg) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode(code);
        responseMessage.setMessage(msg);
        return responseMessage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}