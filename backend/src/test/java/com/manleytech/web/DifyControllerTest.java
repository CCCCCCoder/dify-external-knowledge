package com.manleytech.web;

import com.manleytech.constant.dify.ErrorCode;
import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.query.RetrievalSetting;
import com.manleytech.entity.dify.resp.DifyQueryResponse;
import com.manleytech.entity.dify.resp.DifyRecord;
import com.manleytech.provider.KnowledgeProvider;
import com.manleytech.provider.KnowledgeProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DifyControllerTest {

    @Mock
    private KnowledgeProviderFactory providerFactory;

    @Mock
    private KnowledgeProvider knowledgeProvider;

    private DifyController controller;

    private static final String VALID_API_KEY = "dify-external-knowledge-api-key";
    private static final String INVALID_API_KEY = "invalid-key";
    private static final String VALID_AUTH_HEADER = "Bearer " + VALID_API_KEY;
    private static final String INVALID_AUTH_HEADER = "Bearer " + INVALID_API_KEY;

    @BeforeEach
    void setUp() {
        controller = new DifyController(providerFactory);
    }

    @Test
    void retrieval_returnsQueryResponse_whenValidRequest() {
        DifyQueryEntity queryEntity = createDifyQueryEntity("test-knowledge", "test query");
        
        DifyQueryResponse mockResponse = createDifyQueryResponse();
        when(providerFactory.getProvider("test-knowledge")).thenReturn(knowledgeProvider);
        when(knowledgeProvider.query(any(DifyQueryEntity.class))).thenReturn(Mono.just(mockResponse));

        Mono<DifyQueryResponse> result = controller.retrieval(VALID_AUTH_HEADER, queryEntity);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(1, response.getRecords().size());
                    assertEquals("Test content", response.getRecords().get(0).getContent());
                })
                .verifyComplete();

        verify(providerFactory).getProvider("test-knowledge");
        verify(knowledgeProvider).query(queryEntity);
    }

    @Test
    void retrieval_returnsUnauthorizedError_whenInvalidAuthHeader() {
        DifyQueryEntity queryEntity = createDifyQueryEntity("test-knowledge", "test query");

        Mono<DifyQueryResponse> result = controller.retrieval(INVALID_AUTH_HEADER, queryEntity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof DifyController.UnauthorizedException &&
                    ((DifyController.UnauthorizedException) throwable).getError().getError_code() == 1001
                )
                .verify();

        verifyNoInteractions(providerFactory);
    }

    @Test
    void retrieval_returnsUnauthorizedError_whenNullAuthHeader() {
        DifyQueryEntity queryEntity = createDifyQueryEntity("test-knowledge", "test query");

        Mono<DifyQueryResponse> result = controller.retrieval(null, queryEntity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof DifyController.UnauthorizedException &&
                    ((DifyController.UnauthorizedException) throwable).getError().getError_code() == 1001
                )
                .verify();
    }

    @Test
    void retrieval_returnsUnauthorizedError_whenEmptyAuthHeader() {
        DifyQueryEntity queryEntity = createDifyQueryEntity("test-knowledge", "test query");

        Mono<DifyQueryResponse> result = controller.retrieval("", queryEntity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof DifyController.UnauthorizedException &&
                    ((DifyController.UnauthorizedException) throwable).getError().getError_code() == 1001
                )
                .verify();
    }

    @Test
    void retrieval_returnsUnauthorizedError_whenNoBearerPrefix() {
        DifyQueryEntity queryEntity = createDifyQueryEntity("test-knowledge", "test query");

        Mono<DifyQueryResponse> result = controller.retrieval(VALID_API_KEY, queryEntity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof DifyController.UnauthorizedException &&
                    ((DifyController.UnauthorizedException) throwable).getError().getError_code() == 1001
                )
                .verify();
    }

    @Test
    void retrieval_returnsError_whenNoProviderMapping() {
        DifyQueryEntity queryEntity = createDifyQueryEntity("unknown-knowledge", "test query");
        when(providerFactory.getProvider("unknown-knowledge"))
                .thenThrow(new IllegalArgumentException("No provider mapping found for knowledge_id: unknown-knowledge"));

        Mono<DifyQueryResponse> result = controller.retrieval(VALID_AUTH_HEADER, queryEntity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof DifyController.UnauthorizedException &&
                    ((DifyController.UnauthorizedException) throwable).getError().getError_code() == 2001
                )
                .verify();
    }

    @Test
    void retrieval_returnsError_whenProviderThrowsException() {
        DifyQueryEntity queryEntity = createDifyQueryEntity("test-knowledge", "test query");
        when(providerFactory.getProvider("test-knowledge")).thenReturn(knowledgeProvider);
        when(knowledgeProvider.query(any(DifyQueryEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Provider error")));

        Mono<DifyQueryResponse> result = controller.retrieval(VALID_AUTH_HEADER, queryEntity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Provider error")
                )
                .verify();
    }

    private DifyQueryEntity createDifyQueryEntity(String knowledgeId, String query) {
        DifyQueryEntity entity = new DifyQueryEntity();
        entity.setKnowledge_id(knowledgeId);
        entity.setQuery(query);
        RetrievalSetting setting = new RetrievalSetting();
        setting.setTop_k(10);
        entity.setRetrieval_setting(setting);
        return entity;
    }

    private DifyQueryResponse createDifyQueryResponse() {
        DifyQueryResponse response = new DifyQueryResponse();
        DifyRecord record = new DifyRecord();
        record.setContent("Test content");
        record.setScore(0.95);
        record.setTitle("Test Title");
        response.setRecords(List.of(record));
        return response;
    }
}