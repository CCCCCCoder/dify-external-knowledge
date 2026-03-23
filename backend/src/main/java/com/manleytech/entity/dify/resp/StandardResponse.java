package com.manleytech.entity.dify.resp;

import com.manleytech.constant.dify.ErrorCode;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Serdeable
@Introspected
public class StandardResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public StandardResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public StandardResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>(0, "success", data);
    }

    public static <T> StandardResponse<T> success() {
        return new StandardResponse<>(0, "success");
    }

    public static <T> StandardResponse<T> error(Integer code, String message) {
        return new StandardResponse<>(code, message);
    }

    public static <T> StandardResponse<T> error(ErrorCode errorCode) {
        return new StandardResponse<>(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> StandardResponse<T> error(ErrorCode errorCode, String customMessage) {
        return new StandardResponse<>(errorCode.getCode(), customMessage);
    }
}