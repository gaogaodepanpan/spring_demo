package com.sipc.catalina.enums;

public enum ExamErrorCodeEnum {

    /*************************************文件相关********************************************/
    PARAM_NULL(1011002001, "参数不能为空"),
    NO_DATA_FOR_EXPORT(1011002002, "没有数据导出"),

    /*************************************数据库相关********************************************/
    ADDFAILED(1011000001, "添加数据失败"),
    DELETEFAILED(1011000001, "添加数据失败");





    private Integer code;
    private String message;

    ExamErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
