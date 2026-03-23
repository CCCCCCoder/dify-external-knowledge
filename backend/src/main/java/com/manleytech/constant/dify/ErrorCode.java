package com.manleytech.constant.dify;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    INVALID_AUTHORIZATION_HEADER(1001, "无效的 Authorization 头格式"),
    AUTHORIZATION_FAILED(1002, "授权失败"),
    KNOWLEDGE_BASE_NOT_FOUND(2001, "知识库不存在"),
    VALIDATION_ERROR(4001, "请求参数验证失败"),
    RESOURCE_NOT_FOUND(4004, "资源不存在"),
    MAPPING_CREATE_FAILED(5001, "创建知识库映射失败"),
    MAPPING_UPDATE_FAILED(5002, "更新知识库映射失败"),
    MAPPING_DELETE_FAILED(5003, "删除知识库映射失败"),
    RAG_CONFIG_UPDATE_FAILED(5004, "更新RAG配置失败"),
    ROLLBACK_FAILED(5005, "配置回滚失败"),
    PROVIDER_CONNECTION_FAILED(5006, "Provider连接失败"),
    INTERNAL_SERVER_ERROR(5000, "内部服务器错误"),
    ;

    private final Integer code;
    private final String message;
}