# PROJECT KNOWLEDGE BASE

**Generated:** 2026-03-16
**Commit:** c35e690
**Branch:** main

## OVERVIEW
Root repo for a Dify external-knowledge adapter. Runtime today is a Micronaut Java backend plus a Vue 3/Vite dashboard; design docs describe a future database-driven config model that is only partially reflected in current code.

## STRUCTURE
```text
dify-external-knowledge/
|- backend/   # Micronaut API gateway translating Dify retrieval calls to provider APIs
|- web/       # Vue 3 admin UI for mappings, provider config, logs, dashboard
|- DESIGN.md  # target architecture; do not assume every section is already implemented
|- DIFY_API_SPECIFICATION.md
|- BAILIAN_API_SPECIFICATION.md
`- RAGFLOW_API_SPECIFICATION.md
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Backend entry | `backend/src/main/java/com/manleytech/Application.java` | Micronaut bootstrap |
| Dify request flow | `backend/src/main/java/com/manleytech/web/DifyController.java` | HTTP entry for `/dify/retrieval` |
| Provider routing | `backend/src/main/java/com/manleytech/provider/KnowledgeProviderFactory.java` | `knowledge_id` -> provider selection |
| Provider implementations | `backend/src/main/java/com/manleytech/provider/` | Bailian and RAGflow adapters |
| Runtime config | `backend/src/main/resources/application.yml` | Current live mapping/config source |
| Frontend entry | `web/src/main.js` | Vue bootstrap with Element Plus, router, Pinia |
| Frontend navigation | `web/src/router/index.js` | Route map and page titles |
| Frontend HTTP contract | `web/src/utils/request.js` | Axios interceptors, auth header, error handling |
| Frontend config domain | `web/src/store/modules/config.js` | Mapping/config/history state and actions |
| Frontend views | `web/src/views/` | Dashboard, mappings, config, logs, history |

## CODE MAP
| Symbol | Type | Location | Role |
|--------|------|----------|------|
| `Application` | class | `backend/src/main/java/com/manleytech/Application.java` | Starts Micronaut app |
| `DifyController.retrieval` | method | `backend/src/main/java/com/manleytech/web/DifyController.java` | Main Dify retrieval endpoint |
| `KnowledgeProvider` | interface | `backend/src/main/java/com/manleytech/provider/KnowledgeProvider.java` | Provider contract |
| `KnowledgeProviderFactory.getProvider` | method | `backend/src/main/java/com/manleytech/provider/KnowledgeProviderFactory.java` | Routing hub |
| `BailianKnowledgeProvider.query` | method | `backend/src/main/java/com/manleytech/provider/BailianKnowledgeProvider.java` | Bailian translation path |
| `RAGflowKnowledgeProvider.query` | method | `backend/src/main/java/com/manleytech/provider/RAGflowKnowledgeProvider.java` | RAGflow translation path |
| `configApi` | object | `web/src/api/config.js` | Frontend config endpoints |
| `useConfigStore` | store | `web/src/store/modules/config.js` | Frontend config state |
| `dashboardApi` | object | `web/src/api/dashboard.js` | Dashboard data endpoints |
| `MainLayout` | component | `web/src/components/common/MainLayout.vue` | App shell |

## CONVENTIONS
- Treat `DESIGN.md` as architecture intent, not guaranteed current behavior; verify against runtime code before changing anything.
- Backend is organized around provider adapters: Dify request in -> provider-specific request out -> provider-specific response back -> Dify response.
- Frontend is JavaScript-only Vue 3. `web/.github/copilot-instructions.md` requires Composition API with `<script setup>` and scoped component styles.
- Frontend domain split mirrors API/store pairs: config, logs, dashboard. New frontend work should usually touch matching files in `web/src/api/` and `web/src/store/modules/`.
- UI copy and route metadata are primarily Chinese; keep additions consistent unless the product language changes globally.

## ANTI-PATTERNS (THIS PROJECT)
- Do not assume the database-backed config model from `DESIGN.md` is fully implemented; current runtime still reads `backend/src/main/resources/application.yml`.
- Do not introduce TypeScript into `web/`; current frontend conventions explicitly say JavaScript only.
- Do not leak provider keys, tokens, or internal endpoints in logs or error responses. `DIFY_API_SPECIFICATION.md` explicitly calls for desensitized logging.
- Do not break the Dify response shape. Backend providers must keep returning `DifyQueryResponse` / `records` in the expected contract.
- Do not add new automation assumptions; this repo has no CI pipeline, Dockerfile, or Makefile to lean on.

## UNIQUE STYLES
- Micronaut backend also includes Spring interoperability dependencies; prefer observed Micronaut patterns in code over generic Spring assumptions.
- Provider-specific DTOs live under `entity/{provider}/{query|resp}`; Dify DTOs are the canonical internal contract.
- Frontend constants are co-located with their API modules rather than in a separate constants directory.

## COMMANDS
```bash
cd backend && mvn mn:run
cd backend && mvn test
cd backend && mvn clean package
cd web && pnpm install
cd web && pnpm run dev
cd web && pnpm run build
```

## NOTES
- `backend/src/main/resources/application.yml` currently contains provider config and should be treated as sensitive material even when values are placeholders or examples.
- Backend test coverage is minimal: one Micronaut smoke test in `backend/src/test/java/com/manleytech/DifyApiTest.java`.
- Frontend has no test tooling configured.
