package com.manleytech.provider;

import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.query.RetrievalSetting;
import com.manleytech.entity.dify.resp.DifyQueryResponse;
import com.manleytech.entity.ragflow.query.RAGflowQueryRequest;
import com.manleytech.entity.ragflow.resp.RAGflowQueryResponse;
import com.manleytech.provider.client.RAGflowApiClient;
import com.manleytech.provider.config.KnowledgeProviderProperties;
import com.manleytech.provider.config.ProviderMapping;
import com.manleytech.provider.config.RAGflowApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RAGflowKnowledgeProviderTest {

    @Mock
    private RAGflowApiClient apiClient;

    @Mock
    private RAGflowApiProperties apiProperties;

    @Mock
    private KnowledgeProviderProperties providerProperties;

    private RAGflowKnowledgeProvider provider;

    private static final String TEST_KNOWLEDGE_ID = "test-knowledge-id";
    private static final String TEST_KB_ID = "test-kb-id";

    @BeforeEach
    void setUp() {
        provider = new RAGflowKnowledgeProvider(apiClient, apiProperties, providerProperties);
        
        ProviderMapping mapping = new ProviderMapping();
        mapping.setTargetId(TEST_KB_ID);
        when(providerProperties.getProviders()).thenReturn(Map.of(TEST_KNOWLEDGE_ID, mapping));
        
        when(apiProperties.getDefaultTopK()).thenReturn(10);
        when(apiProperties.getKey()).thenReturn("test-api-key");
    }

    @Test
    void getProviderName_returnsRAGflow() {
        assertEquals("ragflow", provider.getProviderName());
    }

    @Test
    void query_sendsCorrectRequest_toRAGflowAPI() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test question");

        RAGflowQueryResponse mockResponse = createMockRAGflowResponse();
        when(apiClient.query(any(), any(RAGflowQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        provider.query(queryEntity).block();

        ArgumentCaptor<RAGflowQueryRequest> requestCaptor = ArgumentCaptor.forClass(RAGflowQueryRequest.class);
        verify(apiClient).query(eq("Bearer test-api-key"), requestCaptor.capture());

        RAGflowQueryRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test question", capturedRequest.getQuestion());
        assertTrue(capturedRequest.getDatasetIds().contains(TEST_KB_ID));
    }

    @Test
    void query_mapsResponseCorrectly_toDifyFormat() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test question");

        RAGflowQueryResponse mockResponse = createMockRAGflowResponse();
        when(apiClient.query(any(), any(RAGflowQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        DifyQueryResponse response = provider.query(queryEntity).block();

        assertNotNull(response);
        assertNotNull(response.getRecords());
        assertEquals(1, response.getRecords().size());
        assertEquals("Test chunk content", response.getRecords().get(0).getContent());
        assertEquals(0.92, response.getRecords().get(0).getScore());
        assertEquals("Test Doc", response.getRecords().get(0).getTitle());
    }

    @Test
    void query_appliesTopKOverride_fromDifyRequest() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test question");
        
        RetrievalSetting retrievalSetting = new RetrievalSetting();
        retrievalSetting.setTop_k(25);
        queryEntity.setRetrieval_setting(retrievalSetting);

        RAGflowQueryResponse mockResponse = createMockRAGflowResponse();
        when(apiClient.query(any(), any(RAGflowQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        provider.query(queryEntity).block();

        ArgumentCaptor<RAGflowQueryRequest> requestCaptor = ArgumentCaptor.forClass(RAGflowQueryRequest.class);
        verify(apiClient).query(any(), requestCaptor.capture());

        RAGflowQueryRequest capturedRequest = requestCaptor.getValue();
        assertEquals(25, capturedRequest.getTopK());
    }

    @Test
    void query_appliesScoreThreshold_fromDifyRequest() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test question");
        
        RetrievalSetting retrievalSetting = new RetrievalSetting();
        retrievalSetting.setScore_threshold(0.8d);
        queryEntity.setRetrieval_setting(retrievalSetting);

        RAGflowQueryResponse mockResponse = createMockRAGflowResponse();
        when(apiClient.query(any(), any(RAGflowQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        provider.query(queryEntity).block();

        ArgumentCaptor<RAGflowQueryRequest> requestCaptor = ArgumentCaptor.forClass(RAGflowQueryRequest.class);
        verify(apiClient).query(any(), requestCaptor.capture());

        RAGflowQueryRequest capturedRequest = requestCaptor.getValue();
        assertEquals(0.8d, capturedRequest.getScoreThreshold(), 0.0001);
    }

    @Test
    void query_returnsEmptyRecords_whenAPIReturnsError() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test question");

        RAGflowQueryResponse mockResponse = new RAGflowQueryResponse();
        mockResponse.setCode(500);
        mockResponse.setMsg("Internal error");
        when(apiClient.query(any(), any(RAGflowQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        DifyQueryResponse response = provider.query(queryEntity).block();

        assertNotNull(response);
        assertNotNull(response.getRecords());
        assertTrue(response.getRecords().isEmpty());
    }

    @Test
    void query_returnsEmptyRecords_whenNoChunks() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test question");

        RAGflowQueryResponse mockResponse = new RAGflowQueryResponse();
        mockResponse.setCode(0);
        mockResponse.setData(null);
        when(apiClient.query(any(), any(RAGflowQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        DifyQueryResponse response = provider.query(queryEntity).block();

        assertNotNull(response);
        assertNotNull(response.getRecords());
        assertTrue(response.getRecords().isEmpty());
    }

    private RAGflowQueryResponse createMockRAGflowResponse() {
        RAGflowQueryResponse response = new RAGflowQueryResponse();
        response.setCode(0);
        response.setMsg("success");
        
        RAGflowQueryResponse.Data data = new RAGflowQueryResponse.Data();
        RAGflowQueryResponse.Doc chunk = new RAGflowQueryResponse.Doc();
        chunk.setContent("Test chunk content");
        chunk.setSimilarity(0.92d);
        chunk.setDocName("Test Doc");
        chunk.setDocumentId("doc-123");
        chunk.setDocumentKeyword("test keyword");
        chunk.setVectorSimilarity(0.9d);
        chunk.setTermSimilarity(0.85d);
        data.setChunks(java.util.List.of(chunk));
        response.setData(data);
        
        return response;
    }
}