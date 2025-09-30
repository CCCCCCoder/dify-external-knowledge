package com.manleytech.web;

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
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Dify外部知识库控制器
 */
@Controller("/dify")
public class DifyController {

    private static final Logger LOG = LoggerFactory.getLogger(DifyController.class);

    private final KnowledgeProviderFactory providerFactory;

    public DifyController(KnowledgeProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * 处理Dify的查询请求
     *
     * @param queryEntity Dify查询实体
     * @return Dify查询响应
     */
    @Post(value = "/retrieval", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public Mono<DifyQueryResponse> retrieval(@Body DifyQueryEntity queryEntity) {
        LOG.info("Received query request: {}", queryEntity);

        try {
            KnowledgeProvider provider = providerFactory.getProvider(queryEntity.getKnowledge_id());
            return provider.query(queryEntity)
                    .doOnSuccess(response -> LOG.info("Query successful: {}", response))
                    .doOnError(error -> LOG.error("Query failed", error));
        } catch (Exception e) {
            LOG.error("Error processing query", e);
            return Mono.error(e);
        }
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

        DifyError error = new DifyError();
        error.setMessage(e.getMessage());

        return HttpResponse.<DifyError>status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}