package com.bigdemo.demo;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer code;
    private String msg;
    private Object obj;

    //无参构造器
    public Message() {
    }

    public Message(Integer code, String msg, Object obj) {
        this.code = code;
        this.msg = msg;
        if (obj != null) {
            this.obj = obj;
        }

    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public Object getObj() {
        return this.obj;
    }

    public static Message success() {
        return success((Object)null);
    }

    public static Message fail() {
        return fail((Object)null);
    }

    public static Message exception() {
        return exception((Object)null);
    }

    public static Message status(boolean status) {
        return status ? success() : fail();
    }

    public static Message success(Object obj) {
        return new Message(0, "操作成功", obj);
    }

    public static Message fail(Object obj) {
        return new Message(1, "操作失败", obj);
    }

    public static Message exception(Object obj) {
        return new Message(3, "操作异常", obj);
    }
}
