package com.manleytech.provider.client;

import com.manleytech.entity.ragflow.query.RAGflowQueryRequest;
import com.manleytech.entity.ragflow.resp.RAGflowQueryResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import reactor.core.publisher.Mono;

/**
 * RAGflow平台API客户端接口
 */
@Client("${ragflow.api.endpoint}")
public interface RAGflowApiClient {

    /**
     * 向RAGflow平台发送查询请求
     *
     * @param authorization 授权令牌，格式为 "Bearer {token}"
     * @param request 查询请求体
     * @return 查询响应
     */
    @Post("/v1/knowledgebases/search")
    Mono<RAGflowQueryResponse> query(
            @Header("Authorization") String authorization,
            @Body RAGflowQueryRequest request
    );
}