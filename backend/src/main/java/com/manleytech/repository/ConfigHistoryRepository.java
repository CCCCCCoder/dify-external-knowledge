package com.manleytech.repository;

import com.manleytech.entity.db.ConfigHistoryEntity;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface ConfigHistoryRepository extends CrudRepository<ConfigHistoryEntity, Long> {

    List<ConfigHistoryEntity> findByConfigTypeAndConfigIdOrderByCreatedAtDesc(String configType, Long configId);

    List<ConfigHistoryEntity> findByConfigTypeOrderByCreatedAtDesc(String configType);
}
