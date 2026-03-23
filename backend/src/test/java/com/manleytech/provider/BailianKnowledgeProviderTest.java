package com.manleytech.provider;

import com.manleytech.entity.bailian.query.BailianQueryRequest;
import com.manleytech.entity.bailian.resp.BailianQueryResponse;
import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.query.RetrievalSetting;
import com.manleytech.entity.dify.resp.DifyQueryResponse;
import com.manleytech.provider.client.BailianApiClient;
import com.manleytech.provider.config.BailianApiProperties;
import com.manleytech.provider.config.KnowledgeProviderProperties;
import com.manleytech.provider.config.ProviderMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BailianKnowledgeProviderTest {

    @Mock
    private BailianApiClient apiClient;

    @Mock
    private BailianApiProperties apiProperties;

    @Mock
    private KnowledgeProviderProperties providerProperties;

    private BailianKnowledgeProvider provider;

    private static final String TEST_KNOWLEDGE_ID = "test-knowledge-id";
    private static final String TEST_TARGET_ID = "test-target-id";
    private static final String TEST_WORKSPACE_ID = "test-workspace-id";

    @BeforeEach
    void setUp() {
        provider = new BailianKnowledgeProvider(apiClient, apiProperties, providerProperties);
        
        ProviderMapping mapping = new ProviderMapping();
        mapping.setTargetId(TEST_TARGET_ID);
        mapping.setWorkspaceId(TEST_WORKSPACE_ID);
        when(providerProperties.getProviders()).thenReturn(Map.of(TEST_KNOWLEDGE_ID, mapping));
        
        when(apiProperties.getKey()).thenReturn("test-api-key");
        when(apiProperties.getSparseSimilarityTopK()).thenReturn(10);
        when(apiProperties.getDenseSimilarityTopK()).thenReturn(10);
        when(apiProperties.getEnableReranking()).thenReturn(true);
        when(apiProperties.getEnableRewrite()).thenReturn(false);
        when(apiProperties.getSaveRetrieverHistory()).thenReturn(true);
        when(apiProperties.getRerankMinScore()).thenReturn(0.3f);
        when(apiProperties.getRerankTopN()).thenReturn(5);
    }

    @Test
    void getProviderName_returnsBailian() {
        assertEquals("bailian", provider.getProviderName());
    }

    @Test
    void query_sendsCorrectRequest_toBailianAPI() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test query");

        BailianQueryResponse mockResponse = new BailianQueryResponse();
        mockResponse.setSuccess(true);
        when(apiClient.query(any(), any(BailianQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        provider.query(queryEntity).block();

        ArgumentCaptor<BailianQueryRequest> requestCaptor = ArgumentCaptor.forClass(BailianQueryRequest.class);
        verify(apiClient).query(eq("Bearer test-api-key"), requestCaptor.capture());

        BailianQueryRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test query", capturedRequest.getQuery());
        assertEquals(TEST_TARGET_ID, capturedRequest.getIndexId());
        assertEquals(TEST_WORKSPACE_ID, capturedRequest.getWorkspaceId());
    }

    @Test
    void query_mapsResponseCorrectly_toDifyFormat() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test query");

        BailianQueryResponse mockResponse = createMockBailianResponse();
        when(apiClient.query(any(), any(BailianQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        DifyQueryResponse response = provider.query(queryEntity).block();

        assertNotNull(response);
        assertNotNull(response.getRecords());
        assertEquals(1, response.getRecords().size());
        assertEquals("Test document content", response.getRecords().get(0).getContent());
        assertEquals(0.95, response.getRecords().get(0).getScore());
        assertEquals("Test Document", response.getRecords().get(0).getTitle());
    }

    @Test
    void query_appliesTopKOverride_fromDifyRequest() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test query");
        
        RetrievalSetting retrievalSetting = new RetrievalSetting();
        retrievalSetting.setTop_k(20);
        queryEntity.setRetrieval_setting(retrievalSetting);

        BailianQueryResponse mockResponse = createMockBailianResponse();
        when(apiClient.query(any(), any(BailianQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        provider.query(queryEntity).block();

        ArgumentCaptor<BailianQueryRequest> requestCaptor = ArgumentCaptor.forClass(BailianQueryRequest.class);
        verify(apiClient).query(any(), requestCaptor.capture());

        BailianQueryRequest capturedRequest = requestCaptor.getValue();
        assertEquals(10, capturedRequest.getDenseSimilarityTopK());
        assertEquals(10, capturedRequest.getSparseSimilarityTopK());
    }

    @Test
    void query_returnsEmptyRecords_whenResponseHasNoDocuments() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test query");

        BailianQueryResponse mockResponse = new BailianQueryResponse();
        mockResponse.setSuccess(true);
        when(apiClient.query(any(), any(BailianQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        DifyQueryResponse response = provider.query(queryEntity).block();

        assertNotNull(response);
        assertNotNull(response.getRecords());
        assertTrue(response.getRecords().isEmpty());
    }

    @Test
    void query_enablesReranking_whenConfigured() {
        DifyQueryEntity queryEntity = new DifyQueryEntity();
        queryEntity.setKnowledge_id(TEST_KNOWLEDGE_ID);
        queryEntity.setQuery("test query");

        BailianQueryResponse mockResponse = createMockBailianResponse();
        when(apiClient.query(any(), any(BailianQueryRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        provider.query(queryEntity).block();

        ArgumentCaptor<BailianQueryRequest> requestCaptor = ArgumentCaptor.forClass(BailianQueryRequest.class);
        verify(apiClient).query(any(), requestCaptor.capture());

        BailianQueryRequest capturedRequest = requestCaptor.getValue();
        assertTrue(capturedRequest.getEnableReranking());
        assertNotNull(capturedRequest.getRerank());
        assertEquals(1, capturedRequest.getRerank().size());
    }

    private BailianQueryResponse createMockBailianResponse() {
        BailianQueryResponse response = new BailianQueryResponse();
        response.setSuccess(true);
        
        BailianQueryResponse.Data data = new BailianQueryResponse.Data();
        BailianQueryResponse.Document doc = new BailianQueryResponse.Document();
        doc.setText("Test document content");
        doc.setScore(0.95d);
        doc.setDocName("Test Document");
        doc.setMetadata(Map.of("author", "test-author"));
        data.setDocuments(java.util.List.of(doc));
        response.setData(data);
        
        return response;
    }
}