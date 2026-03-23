package com.manleytech.repository;

import com.manleytech.entity.db.KnowledgeMappingEntity;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface KnowledgeMappingRepository extends CrudRepository<KnowledgeMappingEntity, Long> {

    Optional<KnowledgeMappingEntity> findByKnowledgeId(String knowledgeId);

    List<KnowledgeMappingEntity> findByProviderType(String providerType);

    List<KnowledgeMappingEntity> findByStatus(Integer status);

    List<KnowledgeMappingEntity> findByProviderTypeAndStatus(String providerType, Integer status);

    void deleteByKnowledgeId(String knowledgeId);
}
