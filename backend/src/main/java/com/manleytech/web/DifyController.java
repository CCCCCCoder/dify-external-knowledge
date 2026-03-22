package com.manleytech.web;

import com.manleytech.constant.dify.ErrorCode;
import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.resp.DifyError;
import com.manleytech.entity.dify.resp.DifyQueryResponse;
import com.manleytech.provider.KnowledgeProvider;
import com.manleytech.provider.KnowledgeProviderFactory;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Dify外部知识库控制器
 */
@Controller("/dify")
public class DifyController {

    private static final Logger LOG = LoggerFactory.getLogger(DifyController.class);

    private static final String EXPECTED_API_KEY = "dify-external-knowledge-api-key";

    private final KnowledgeProviderFactory providerFactory;

    public DifyController(KnowledgeProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * 处理Dify的查询请求
     *
     * @param authorization Authorization头，格式为 "Bearer {api-key}"
     * @param queryEntity Dify查询实体
     * @return Dify查询响应
     */
    @Post(value = "/retrieval", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public Mono<DifyQueryResponse> retrieval(
            @Header("Authorization") String authorization,
            @Body @Valid DifyQueryEntity queryEntity) {

        LOG.info("Received query request: {}", queryEntity);

        // 验证Authorization头
        if (!isValidAuthorization(authorization)) {
            LOG.warn("Invalid authorization header: {}", authorization);
            return Mono.error(new UnauthorizedException(
                    new DifyError(ErrorCode.INVALID_AUTHORIZATION_HEADER.getCode(),
                            ErrorCode.INVALID_AUTHORIZATION_HEADER.getMessage())));
        }

        try {
            KnowledgeProvider provider = providerFactory.getProvider(queryEntity.getKnowledge_id());
            return provider.query(queryEntity)
                    .doOnSuccess(response -> LOG.info("Query successful: {}", response))
                    .doOnError(error -> LOG.error("Query failed", error));
        } catch (IllegalArgumentException e) {
            LOG.error("Error processing query", e);
            if (e.getMessage().contains("No provider mapping found")) {
                return Mono.error(new UnauthorizedException(
                        new DifyError(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND.getCode(),
                                ErrorCode.KNOWLEDGE_BASE_NOT_FOUND.getMessage())));
            }
            return Mono.error(new UnauthorizedException(
                    new DifyError(ErrorCode.AUTHORIZATION_FAILED.getCode(),
                            e.getMessage())));
        } catch (Exception e) {
            LOG.error("Error processing query", e);
            return Mono.error(e);
        }
    }

    /**
     * 验证Authorization头是否有效
     *
     * @param authorization Authorization头值
     * @return 是否有效
     */
    private boolean isValidAuthorization(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return false;
        }

        if (!authorization.startsWith("Bearer ")) {
            return false;
        }

        String apiKey = authorization.substring(7);
        return EXPECTED_API_KEY.equals(apiKey);
    }

    /**
     * 处理全局异常
     *
     * @param e 异常
     * @return 错误响应
     */
    @Error
    public HttpResponse<DifyError> handleException(Exception e) {
        LOG.error("Error handling request", e);

        DifyError error;
        HttpStatus status;

        if (e instanceof UnauthorizedException) {
            status = HttpStatus.FORBIDDEN;
            error = ((UnauthorizedException) e).getError();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = new DifyError(5001, e.getMessage());
        }

        return HttpResponse.<DifyError>status(status).body(error);
    }

    /**
     * 认证异常
     */
    public static class UnauthorizedException extends RuntimeException {
        private final DifyError error;

        public UnauthorizedException(DifyError error) {
            super(error.getError_msg());
            this.error = error;
        }

        public DifyError getError() {
            return error;
        }
    }
}
