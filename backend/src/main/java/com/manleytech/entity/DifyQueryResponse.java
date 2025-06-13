package com.manleytech.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import java.util.List;

@Data
@Serdeable
public class DifyQueryResponse {
    private List<DifyRecord> records;
}
