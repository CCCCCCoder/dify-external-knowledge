package com.manleytech.provider.client;

import com.manleytech.entity.bailian.query.BailianQueryRequest;
import com.manleytech.entity.bailian.resp.BailianQueryResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import reactor.core.publisher.Mono;

/**
 * 百炼平台API客户端接口
 */
@Client("${bailian.api.endpoint}")
public interface BailianApiClient {

    /**
     * 向百炼平台发送查询请求
     *
     * @param authorization 授权令牌，格式为 "Bearer {token}"
     * @param request 查询请求体
     * @return 查询响应
     */
    @Post("/v1/api/knowledge/query") 
    Mono<BailianQueryResponse> query(
            @Header("Authorization") String authorization,
            @Body BailianQueryRequest request
    );
}