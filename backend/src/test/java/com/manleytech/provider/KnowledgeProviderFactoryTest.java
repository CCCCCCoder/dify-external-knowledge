package com.manleytech.provider;

import com.manleytech.entity.db.KnowledgeMappingEntity;
import com.manleytech.service.DatabaseConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeProviderFactoryTest {

    @Mock
    private BailianKnowledgeProvider bailianProvider;

    @Mock
    private RAGflowKnowledgeProvider ragflowProvider;

    @Mock
    private DatabaseConfigService configService;

    private KnowledgeProviderFactory factory;

    @BeforeEach
    void setUp() {
        when(bailianProvider.getProviderName()).thenReturn("bailian");
        when(ragflowProvider.getProviderName()).thenReturn("ragflow");
        factory = new KnowledgeProviderFactory(List.of(bailianProvider, ragflowProvider), configService);
    }

    @Test
    void getProvider_returnsBailian_whenKnowledgeIdMappedToBailian() {
        KnowledgeMappingEntity mapping = new KnowledgeMappingEntity();
        mapping.setKnowledgeId("test-knowledge-1");
        mapping.setProviderType("bailian");
        when(configService.getMapping("test-knowledge-1")).thenReturn(Optional.of(mapping));

        KnowledgeProvider provider = factory.getProvider("test-knowledge-1");

        assertEquals(bailianProvider, provider);
        verify(configService).getMapping("test-knowledge-1");
    }

    @Test
    void getProvider_returnsRAGflow_whenKnowledgeIdMappedToRAGflow() {
        KnowledgeMappingEntity mapping = new KnowledgeMappingEntity();
        mapping.setKnowledgeId("test-knowledge-2");
        mapping.setProviderType("ragflow");
        when(configService.getMapping("test-knowledge-2")).thenReturn(Optional.of(mapping));

        KnowledgeProvider provider = factory.getProvider("test-knowledge-2");

        assertEquals(ragflowProvider, provider);
        verify(configService).getMapping("test-knowledge-2");
    }

    @Test
    void getProvider_throwsException_whenNoMappingFound() {
        when(configService.getMapping("unknown-knowledge")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getProvider("unknown-knowledge")
        );

        assertTrue(exception.getMessage().contains("No provider mapping found"));
        assertTrue(exception.getMessage().contains("unknown-knowledge"));
    }

    @Test
    void getProvider_throwsException_whenUnknownProviderType() {
        KnowledgeMappingEntity mapping = new KnowledgeMappingEntity();
        mapping.setKnowledgeId("test-knowledge-3");
        mapping.setProviderType("unknown-provider");
        when(configService.getMapping("test-knowledge-3")).thenReturn(Optional.of(mapping));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getProvider("test-knowledge-3")
        );

        assertTrue(exception.getMessage().contains("Unknown provider type"));
        assertTrue(exception.getMessage().contains("unknown-provider"));
    }

    @Test
    void getProvider_throwsException_whenMappingReturnsNull() {
        when(configService.getMapping("null-mapping")).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> factory.getProvider("null-mapping")
        );
    }
}