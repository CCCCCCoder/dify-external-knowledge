package com.manleytech.entity.dify.resp;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class DifyError {
    /**
     * 错误代码
     */
    private Integer error_code;

    /**
     * API 异常描述
     */
    private String error_msg;
}
