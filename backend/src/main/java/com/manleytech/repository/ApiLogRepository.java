package com.manleytech.repository;

import com.manleytech.entity.db.ApiLogEntity;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface ApiLogRepository extends CrudRepository<ApiLogEntity, Long> {

    Optional<ApiLogEntity> findByRequestId(String requestId);

    List<ApiLogEntity> findByKnowledgeIdOrderByCreatedAtDesc(String knowledgeId);

    List<ApiLogEntity> findByProviderTypeOrderByCreatedAtDesc(String providerType);

    List<ApiLogEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}
