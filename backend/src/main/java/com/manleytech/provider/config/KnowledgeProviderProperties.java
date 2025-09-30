package com.manleytech.provider.config;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.Map;
import lombok.Data;

@Data
@ConfigurationProperties("knowledge")
public class KnowledgeProviderProperties {

    private Map<String, ProviderMapping> providers;
}