package org.example.zjzaiagent.common;

import lombok.Data;

@Data
public class BaseResponse<T> {

    private Integer code;

    private String message;

    private T data;

    public BaseResponse(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(Integer code, T data){
        this(code,data,"");
    }

    public BaseResponse(Integer code, String message){
        this(code,null,message);
    }

    public BaseResponse(ErrorEnum errorEnum){
        this(errorEnum.getCode(),null,errorEnum.getMessage());
    }

}
