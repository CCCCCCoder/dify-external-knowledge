package com.manleytech.constant.dify;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    INVALID_AUTHORIZATION_HEADER(1001, "无效的 Authorization 头格式"),
    AUTHORIZATION_FAILED(1002, "授权失败"),
    KNOWLEDGE_BASE_NOT_FOUND(2001, "知识库不存在"),
    ;


    private final Integer code;
    private final String message;

}
