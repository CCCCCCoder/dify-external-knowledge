package com.manleytech.entity.dify.resp;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

/**
 * Dify错误响应实体
 * 文档定义: https://github.com/langgenius/dify/blob/master/api/services/errors.py
 */
@Data
@Serdeable
@Introspected
public class DifyError {
    /**
     * 错误代码
     * 1001: 无效的 Authorization 头格式
     * 1002: 授权失败
     * 2001: 知识库不存在
     * 3001: RAG框架连接失败
     * 3002: 查询参数格式错误
     * 5001: 内部服务器错误
     */
    private Integer error_code;

    /**
     * 错误信息描述
     */
    private String error_msg;

    public DifyError() {
    }

    public DifyError(Integer error_code, String error_msg) {
        this.error_code = error_code;
        this.error_msg = error_msg;
    }
}
