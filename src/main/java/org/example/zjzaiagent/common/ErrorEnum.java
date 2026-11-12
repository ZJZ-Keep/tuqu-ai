package org.example.zjzaiagent.common;

public enum ErrorEnum {
    SUCCESS(0,"成功"),
    PARAM_ERROR(1001,"参数错误"),
    NULL_ERROR(1002,"空指针错误");


    private Integer code;

    private String message;


    ErrorEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}
