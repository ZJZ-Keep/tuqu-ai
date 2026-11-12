package org.example.zjzaiagent.common;

public class ResultUtil {

    /**
     * 成功返回结果
     * @param data
     * @return
     */
    public static <T> BaseResponse<T> success( T data){
        return new BaseResponse<>(0,data,"success");
    }

    /**
     * 失败返回结果
     * @param errorEnum
     * @return
     */
    public static BaseResponse error(ErrorEnum errorEnum){
        return  new BaseResponse<>(errorEnum.getCode(),errorEnum.getMessage());
    }


}
