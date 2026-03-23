package com.manleytech.repository;

import com.manleytech.entity.db.RagConfigEntity;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface RagConfigRepository extends CrudRepository<RagConfigEntity, Long> {

    Optional<RagConfigEntity> findByProviderType(String providerType);

    List<RagConfigEntity> findByStatus(Integer status);

    void deleteByProviderType(String providerType);
}
