# AGENTS.md - Agentic Coding Guidelines

**Generated:** 2026-03-23
**Repository:** dify-external-knowledge

## PROJECT OVERVIEW

Dify external-knowledge adapter. Micronaut Java backend + Vue 3/Vite dashboard. Routes Dify retrieval requests to external RAG providers (Bailian, RAGflow).

## STRUCTURE
```
dify-external-knowledge/
|- backend/   # Micronaut 4.x + Java 17 API gateway
|- web/       # Vue 3 + Element Plus admin UI
|- DESIGN.md  # Target architecture (verify against runtime)
|- DIFY_API_SPECIFICATION.md
|- BAILIAN_API_SPECIFICATION.md
`- RAGFLOW_API_SPECIFICATION.md
```

---

## COMMANDS

### Backend (Java 17 / Maven / Micronaut 4.x)

```bash
# Run development server
cd backend && mvn mn:run

# Run all tests
cd backend && mvn test

# Run a single test class
cd backend && mvn test -Dtest=DifyApiTest

# Run a single test method
cd backend && mvn test -Dtest=DifyApiTest#testItWorks

# Build package
cd backend && mvn clean package

# Compile only (no tests)
cd backend && mvn compile
```

### Frontend (Node / pnpm / Vite)

```bash
# Install dependencies
cd web && pnpm install

# Development server
cd web && pnpm run dev

# Production build
cd web && pnpm run build

# Preview production build
cd web && pnpm run preview
```

**Note:** Frontend has no lint or test tooling configured.

---

## BACKEND CONVENTIONS (Java / Micronaut)

### Code Style
- 4-space indentation, no tabs
- No line length limit enforced, but prefer ~120 chars
- No comments unless explaining "why", not "what"
- Package-private visibility preferred for classes; `public` only when necessary
- Field injection via constructor (no `@Inject` on fields)

### Imports
- Static imports first, then alphabetical by package
- Group order: `java.*` → `javax.*` → third-party (`io.micronaut`, `reactor`, `org.slf4j`) → `com.manleytech.*`
- Avoid wildcard imports except for static constants

### Naming
| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `BailianKnowledgeProvider` |
| Methods | camelCase | `getProviderName`, `mapToDifyResponse` |
| Constants | UPPER_SNAKE_CASE | `EXPECTED_API_KEY` |
| Packages | lowercase | `com.manleytech.provider` |
| DTO subdirs | `{provider}/{query\|resp}` | `entity/bailian/query` |

### Annotations
- Use Micronaut annotations (`@Controller`, `@Singleton`, `@Body`, `@Header`)
- Use `@Serdeable` on all DTOs (required for Micronaut serialization)
- Use `@Data` (Lombok) for DTOs; avoid手动 getters/setters
- Use `@Valid` on controller method parameters for validation

### Error Handling
- Return `Mono.error()` for reactive flows; never throw inside a `map()` operator
- Use `doOnError()` for logging before returning error
- Never expose raw credentials or tokens in error messages or logs
- Log errors with `LOG.error("...", error)` including the exception

### Reactive Patterns
- Use `Mono<T>` for all controller return types (not `Publisher<T>` or raw `Flux`)
- Chain operators: `apiClient.query().map().doOnSuccess().doOnError()`
- Never block on reactive streams

### DTO Structure
- Dify DTOs are the canonical internal contract
- External provider DTOs live in `entity/{provider}/query` and `entity/{provider}/resp`
- Always map external responses to Dify DTOs before returning

---

## FRONTEND CONVENTIONS (Vue 3 / JavaScript)

### Code Style
- 2-space indentation, no tabs
- No semicolons at line ends
- Use `const` and `let`; never `var`
- No TypeScript files or TS-specific tooling
- No comments unless explaining complex logic

### Script Setup
- Use `<script setup>` Composition API syntax for all components
- Import Vue primitives: `import { ref, computed, onMounted, watch } from 'vue'`
- Element Plus components: `import { ElMessage, ElButton } from 'element-plus'`
- Component-local icons: `import { Refresh } from '@element-plus/icons-vue'`

### Naming
| Element | Convention | Example |
|---------|------------|---------|
| Components | PascalCase | `Dashboard.vue`, `MainLayout.vue` |
| Variables | camelCase | `loading`, `timeRange`, `overviewData` |
| Constants | UPPER_SNAKE_CASE | `TIME_RANGES.LAST_7_DAYS` |
| CSS classes | kebab-case | `.dashboard-header`, `.card-content` |
| Store modules | camelCase | `useConfigStore`, `useAppStore` |

### Component Styles
- Always use scoped styles: `<style scoped>`
- Global styles in `src/App.vue` and `src/style.css`
- Follow Element Plus theming variables (`#409eff`, `#67c23a`, etc.)
- Use Element Plus grid (`el-row`, `el-col`) for layouts
- Responsive breakpoints: `:xs`, `:sm`, `:md`, `:lg`

### Pinia Stores
- Use `defineStore` with Options API style (`state`, `getters`, `actions`)
- Actions are `async` functions using `await` for API calls
- Error handling: `try/catch` with `console.error` and re-throw
- State should be initialized with sensible defaults

### API Layer
- All HTTP calls go through `src/utils/request.js` (axios instance)
- API modules live in `src/api/` and mirror store domains
- API response envelope: expect `response.data` structure
- Constants/enums live beside their API module, not in shared tree

### UI Copy
- Route titles, labels, and most UI copy are Chinese
- Preserve this convention for consistency

---

## ARCHITECTURE FLOW

```
Dify → DifyController (/dify/retrieval)
     → KnowledgeProviderFactory.getProvider(knowledge_id)
     → BailianKnowledgeProvider or RAGflowKnowledgeProvider
     → External RAG API call
     → Map response to DifyQueryResponse
     → Return to Dify
```

---

## KEY HOTSPOTS

| File | Role |
|------|------|
| `backend/.../web/DifyController.java` | HTTP entry, auth validation |
| `backend/.../provider/KnowledgeProviderFactory.java` | Provider selection by knowledge_id |
| `backend/.../provider/BailianKnowledgeProvider.java` | Rerank/rewrite/top-k logic |
| `backend/.../provider/RAGflowKnowledgeProvider.java` | RAGflow adapter |
| `web/src/utils/request.js` | Axios interceptors, auth, errors |
| `web/src/store/modules/config.js` | Mappings, config, history CRUD |
| `web/src/views/Dashboard.vue` | Largest view (630 lines) |

---

## ANTI-PATTERNS

- **Backend**: Do not hardcode provider selection outside `KnowledgeProviderFactory`. Do not return provider-native responses from controllers. Do not leak credentials in logs.
- **Frontend**: Do not bypass `request.js` for HTTP. Do not add TypeScript. Do not assume frontend tests exist.
- **Both**: Do not assume DB-backed config from DESIGN.md is implemented (currently uses `application.yml`).

---

## SENSITIVE MATERIAL

- `backend/src/main/resources/application.yml` contains provider credentials
- Never commit real API keys or tokens
- Log desensitized information only (see `DIFY_API_SPECIFICATION.md`)

---

## COPOLOT/AGENT INSTRUCTIONS

Additional frontend rules in `web/.github/copilot-instructions.md`:
- Vue 3 Composition API with `<script setup>`
- Element Plus component patterns
- Scoped styles only
- JS-only (no TypeScript)
