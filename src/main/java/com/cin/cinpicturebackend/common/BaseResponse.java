package com.cin.cinpicturebackend.common;

import com.cin.cinpicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;
@Data
public class BaseResponse<T> implements Serializable {
    private int code;

    private String message;

    private T data;

    BaseResponse(int code,T data,String message){
        this.code=code;
        this.data=data;
        this.message=message;
    }

    BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage());
    }

    BaseResponse(int code,T data) {
        this(code,data,null);
    }
}
