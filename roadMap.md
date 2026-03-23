# dify-external-knowledge Implementation Roadmap

**Generated**: 2026-03-23
**Based on**: DESIGN.md
**Implementation Strategy**: Parallel development (frontend + backend simultaneously)
**Fallback Strategy**: YAML config preserved as degraded mode
**Real-time Strategy**: SSE (Server-Sent Events) for log streaming

---

## Current Implementation Status

| Category | Status | Progress |
|----------|--------|----------|
| Backend: Core RAG Routing (Dify → Bailian/RAGflow) | ✅ Complete | 100% |
| Backend: KnowledgeProvider Interface (Strategy Pattern) | ✅ Complete | 100% |
| Frontend: Config Management UI | ✅ Complete | 100% |
| Frontend: Log Viewing UI | ✅ Complete | 100% |
| Frontend: Dashboard with ECharts | ✅ Complete | 100% |
| Backend: Database-backed Configuration | ✅ Complete | 100% |
| Backend: API Logging to DB | ✅ Complete | 100% |
| Frontend: Real-time SSE Features | ✅ Complete | 100% |
| Backend: Config History & Rollback | ✅ Complete | 100% |
| Frontend: Import/Export Config | ✅ Complete | 100% |
| Backend: Provider Health Check | ✅ Complete | 100% |

**Overall Completion**: ~90%

---

## Phase 1: Database Foundation (Backend)
**Goal**: 建立数据库层，实现配置与代码分离

### 1.1 Database Schema
```sql
-- 4 core tables as specified in DESIGN.md
CREATE TABLE knowledge_mapping (...);
CREATE TABLE rag_config (...);
CREATE TABLE config_history (...);
CREATE TABLE api_log (...);

-- Indexes for query optimization
CREATE INDEX idx_knowledge_id ON knowledge_mapping(knowledge_id);
CREATE INDEX idx_provider_type ON knowledge_mapping(provider_type);
CREATE INDEX idx_status ON knowledge_mapping(status);
CREATE INDEX idx_provider_type ON rag_config(provider_type);
CREATE INDEX idx_config_type_id ON config_history(config_type, config_id);
CREATE INDEX idx_created_at ON api_log(created_at);
```

**Status**: ✅ IMPLEMENTED - Tables created in MySQL database `dify-external`

**Deliverables**:
- Flyway migration scripts or schema SQL file
- Index optimization for common queries

### 1.2 Entity Classes
```
backend/src/main/java/com/manleytech/entity/db/
├── KnowledgeMappingEntity.java
├── RagConfigEntity.java
├── ConfigHistoryEntity.java
└── ApiLogEntity.java
```

**Annotations**:
- `@Entity`, `@Table` for JPA
- `@Serdeable` for Micronaut serialization
- `@DateCreated`, `@DateUpdated` for timestamps

### 1.3 Repository Interfaces
```
backend/src/main/java/com/manleytech/repository/
├── KnowledgeMappingRepository.java  # extends MicronautDataRepository
├── RagConfigRepository.java
├── ConfigHistoryRepository.java
└── ApiLogRepository.java
```

**Features**:
- `Optional<KnowledgeMappingEntity> findByKnowledgeId(String knowledgeId)`
- `Optional<RagConfigEntity> findByProviderType(String providerType)`
- `List<ConfigHistoryEntity> findByConfigTypeAndConfigIdOrderByCreatedAtDesc(String configType, Long configId)`
- `Page<ApiLogEntity> findByConditions(...)` for filtered log queries

### 1.4 DatabaseConfigService
```java
@Singleton
@CacheConfig("config-cache")
public class DatabaseConfigService {
    // - Load configs from DB on startup (@PostConstruct)
    // - @Cacheable for getMapping(), getRagConfig()
    // - Fallback to YAML when DB unavailable
    // - @CacheInvalidate on config changes
}
```

---

## Phase 2: Backend Config Refactoring
**Goal**: 动态配置管理，支持完整的CRUD操作

### 2.1 ConfigController Full CRUD
**Current**: All mutations return `405 METHOD_NOT_ALLOWED`

**Target Endpoints**:
```
POST   /api/config/mappings          # Create mapping
PUT    /api/config/mappings/{id}     # Update mapping
DELETE /api/config/mappings/{id}     # Delete mapping
PUT    /api/config/rag/{providerType} # Update RAG config
POST   /api/config/rag/{providerType}/test # Test connection
```

### 2.2 Config History Tracking
```java
@Aspect
@Component
@Requires(property = "aspect.enabled", value = "true", defaultValue = "true")
public class ConfigChangeTrackingAspect {
    // @Around repository methods
    // Before update: store old value in config_history
    // After update: store new value
    // Record: config_type, config_id, operation_type (CREATE/UPDATE/DELETE), 
    //         old_value, new_value, operator, created_at
}
```

### 2.3 Rollback Functionality
```java
// POST /api/config/rollback
public Mono<RollbackResult> rollback(RollbackRequest request) {
    // 1. Fetch history record by history_id
    // 2. Validate history record belongs to the specified config
    // 3. Restore old_value to current config
    // 4. Record new history entry for the rollback
    // 5. Invalidate cache
    // 6. Return success with message
}
```

### 2.4 API Logging Interceptor
```java
@Filter("/dify/**")
public class ApiLoggingFilter implements HttpClientFilter {
    // Intercept all /dify/* requests
    // Log to api_log table:
    //   - request_id (UUID)
    //   - knowledge_id, provider_type
    //   - request_url, request_method, request_headers, request_body
    //   - response_status, response_body, response_time
    //   - error_message (if any)
}
```

**Async logging**: Use `Mono.defer()` to avoid blocking the main request.

### 2.5 Provider Connection Testing
```java
// POST /api/config/rag/{providerType}/test
public Mono<ConnectionTestResult> testConnection(String providerType, TestRequest request) {
    // 1. Load config from DB (or fallback)
    // 2. Build test query request
    // 3. Call external API with timeout (10s)
    // 4. Return: success (bool), response_time (ms), result_count, error_message
}
```

### 2.6 YAML Fallback Service
```java
@Singleton
public class YamlConfigFallback implements ConfigSource {
    // Load static config from application.yml
    // Provide same interface as DatabaseConfigService
    // Use when:
    //   - DatabaseConfigService initialization fails
    //   - Database connection unavailable
    //   - Explicit degraded mode flag
}
```

---

## Phase 3: Frontend Real-time & Charts
**Goal**: 完善数据可视化

### 3.1 ECharts Integration

**Dependencies** (already installed):
- `echarts: ^6.0.0`
- `vue-echarts: ^8.0.1`

**Setup**:
```javascript
// main.js or Dashboard.vue
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echats/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

Vue.component('VChart', VChart)
```

**Dashboard Charts**:
1. **Request Trends** (Line Chart): Daily/hourly request volume over time
2. **Provider Distribution** (Pie Chart): Bailian vs RAGflow request split
3. **Performance Metrics** (Bar Chart): Avg response time by provider
4. **Error Rate** (Gauge or Line): Success rate over time

### 3.2 SSE Real-time Logs

**Backend Endpoint**:
```java
@Controller("/api/logs")
public class LogStreamController {
    @Get("/stream")
    public HttpResponse<Publisher<LogEvent>> streamLogs(
        @QueryValue(defaultValue = "100") int bufferSize
    ) {
        // Return SSE stream of new log entries
        // Use Flux.interval() + log polling, or
        // Use Spring's SseEmitter equivalent in Micronaut
    }
}
```

**Frontend Store**:
```javascript
// store/log.js
startRealTimeLogs() {
    if (this.eventSource) return
    
    this.eventSource = new EventSource('/api/logs/stream')
    this.eventSource.onmessage = (event) => {
        const log = JSON.parse(event.data)
        this.logs.unshift(log)
        if (this.logs.length > this.bufferSize) {
            this.logs.pop()
        }
    }
    this.eventSource.onerror = () => {
        // Auto-reconnect after 3s
        setTimeout(() => this.startRealTimeLogs(), 3000)
    }
}

stopRealTimeLogs() {
    if (this.eventSource) {
        this.eventSource.close()
        this.eventSource = null
    }
}
```

### 3.3 Import/Export UI

**Import Dialog** (`ImportDialog.vue`):
```vue
<template>
  <el-dialog title="导入配置" v-model="visible">
    <el-upload drag accept=".json" :auto-upload="false" @change="handleFileChange">
      <el-icon><Upload /></el-icon>
      <div>将文件拖到此处，或<em>点击上传</em></div>
    </el-upload>
    <el-button type="primary" @click="submitImport" :loading="loading">
      确认导入
    </el-button>
  </el-dialog>
</template>
```

**Export Button**:
```javascript
// In KnowledgeMappings.vue
async exportConfig() {
    const response = await configApi.exportConfig()
    const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `config_export_${dayjs().format('YYYYMMDD')}.json`
    a.click()
}
```

### 3.4 Provider Health Monitoring

**Health Check Endpoint**:
```java
// GET /api/config/rag/{providerType}/health
public Mono<HealthStatus> checkHealth(String providerType) {
    // 1. Load config from DB
    // 2. Make lightweight health check call (e.g., /v1/health)
    // 3. Return: status (UP/DOWN), latency_ms, last_checked
}
```

**Frontend Indicator**:
```vue
<template>
  <div class="health-indicator">
    <span :class="['status-dot', status]"></span>
    <span>{{ statusText }}</span>
  </div>
</template>
```

**Periodic Polling**: Check every 30s via `setInterval`.

---

## Phase 4: Polish
**Goal**: 稳定性与可维护性

### 4.1 Error Handling
- Global exception handler for reactive streams (`@Error` annotation)
- Standardized error response format:
  ```json
  {
    "code": 1001,
    "message": "错误描述",
    "data": null
  }
  ```
- Error codes enum in `constant/dify/ErrorCode.java`
- Desensitize credentials in logs (mask API keys)

### 4.2 Unit Tests
```bash
# Run all tests
cd backend && mvn test

# Target coverage:
# - KnowledgeProviderFactory: provider selection logic
# - BailianKnowledgeProvider: request/response mapping
# - RAGflowKnowledgeProvider: request/response mapping
# - DifyController: request validation, auth
# - ConfigController: CRUD operations
```

### 4.3 API Documentation
- Add Swagger/OpenAPI annotations to ConfigController
- Document request/response schemas
- Generate API docs at `/swagger` endpoint

---

## File Changes Summary

### New Files (Backend)

```
backend/src/main/java/com/manleytech/
├── entity/db/
│   ├── KnowledgeMappingEntity.java
│   ├── RagConfigEntity.java
│   ├── ConfigHistoryEntity.java
│   └── ApiLogEntity.java
├── repository/
│   ├── KnowledgeMappingRepository.java
│   ├── RagConfigRepository.java
│   ├── ConfigHistoryRepository.java
│   └── ApiLogRepository.java
├── service/
│   ├── DatabaseConfigService.java
│   ├── YamlConfigFallback.java
│   ├── ConfigHistoryService.java
│   └── ApiLogService.java
├── aspect/
│   └── ConfigChangeTrackingAspect.java
├── filter/
│   └── ApiLoggingFilter.java
└── controller/
    └── LogStreamController.java        # SSE endpoint
```

### New Files (Frontend)

```
web/src/components/
├── charts/
│   ├── RequestTrendChart.vue
│   ├── ProviderDistChart.vue
│   └── PerformanceChart.vue
├── ImportDialog.vue
└── HealthIndicator.vue
```

### Modified Files

```
Backend:
├── Application.java
│   # Add: @EntityScan("com.manleytech.entity.db")
│   # Add: @EnableJpaRepositories("com.manleytech.repository")
├── web/ConfigController.java
│   # Enable POST/PUT/DELETE (remove 405 responses)
│   # Add @Valid validation
│   # Wire to DatabaseConfigService
├── provider/KnowledgeProviderFactory.java
│   # Load from DatabaseConfigService instead of static config
├── provider/config/KnowledgeProviderProperties.java
│   # Deprecate, fallback to DB
└── application.yml
    # Add JPA/Hibernate configuration

Frontend:
├── main.js
│   # Register ECharts components
├── store/log.js
│   # Implement SSE streaming methods
├── views/Dashboard.vue
│   # Replace placeholders with real ECharts components
├── views/KnowledgeMappings.vue
│   # Add Import/Export buttons and dialogs
└── views/RagConfig.vue
    # Add health indicator component
```

---

## Implementation Timeline (Parallel)

```
Week 1-2: Phase 1 (Database Foundation)
├── Schema design & migration scripts
├── Entity classes
└── Repository interfaces

Week 2-3: Phase 2 (Backend Config Refactoring)
├── ConfigController CRUD
├── Config history tracking
└── YAML fallback service

Week 3-4: Phase 2 continued
├── Rollback functionality
├── API logging filter
└── Connection testing

Week 3-5: Phase 3 (Frontend) [PARALLEL WITH PHASE 2]
├── ECharts integration
├── SSE real-time logs
└── Import/Export UI

Week 5-6: Phase 4 (Polish)
├── Error handling
├── Unit tests
└── Documentation
```

---

## Critical Path

```
1. Database Schema → Entity Classes → Repositories
   ↓
2. DatabaseConfigService (load from DB)
   ↓
3. ConfigController CRUD (wire to repos)
   ↓
4. Config History + Rollback
   ↓
5. API Logging Filter
   ↓
6. Frontend ECharts + SSE (parallel with 4-5)
```

---

## Dependencies to Add (pom.xml)

```xml
<!-- Micronaut Data JPA -->
<dependency>
    <groupId>io.micronaut.data</groupId>
    <artifactId>micronaut-data-hibernate-jpa</artifactId>
</dependency>
<dependency>
    <groupId>io.micronaut.sql</groupId>
    <artifactId>micronaut-jdbc-hikari</artifactId>
</dependency>

<!-- For migrations (optional, can use raw SQL) -->
<dependency>
    <groupId>io.micronaut.flyway</groupId>
    <artifactId>micronaut-flyway</artifactId>
</dependency>
```

---

## Verification Checklist

After each phase, verify:

- [ ] Phase 1: Tables created, entities persist/retrieve correctly
- [ ] Phase 2: Config changes via API reflected in DB, history recorded
- [ ] Phase 3: Charts render, logs stream in real-time
- [ ] Phase 4: `mvn test` passes, no credential leaks in logs
