package com.manleytech.entity.dify.resp;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import java.util.List;

@Data
@Serdeable
public class DifyQueryResponse {
    private List<DifyRecord> records;
}
