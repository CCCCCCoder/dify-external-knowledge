# BACKEND KNOWLEDGE BASE

## OVERVIEW
Micronaut backend that accepts Dify retrieval requests, selects an external provider by `knowledge_id`, calls the provider API, and maps the result back to Dify's response format.

## STRUCTURE
```text
backend/
|- pom.xml
|- src/main/java/com/manleytech/
|  |- web/        # controllers
|  |- provider/   # routing, adapters, clients, config beans
|  |- entity/     # Dify/Bailian/RAGflow DTOs
|  `- constant/   # operator and provider constants
|- src/main/resources/
|  `- application.yml
`- src/test/java/com/manleytech/
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| App bootstrap | `src/main/java/com/manleytech/Application.java` | Micronaut entry |
| Dify HTTP entry | `src/main/java/com/manleytech/web/DifyController.java` | `/dify/retrieval` |
| Provider selection | `src/main/java/com/manleytech/provider/KnowledgeProviderFactory.java` | Reads mapping and resolves implementation |
| Add/change provider | `src/main/java/com/manleytech/provider/` | Interface, implementations, clients |
| Mapping config beans | `src/main/java/com/manleytech/provider/config/` | `KnowledgeProviderProperties`, `ProviderMapping`, API props |
| Bailian request/response DTOs | `src/main/java/com/manleytech/entity/bailian/` | External schema |
| RAGflow request/response DTOs | `src/main/java/com/manleytech/entity/ragflow/` | External schema |
| Canonical Dify DTOs | `src/main/java/com/manleytech/entity/dify/` | Internal request/response contract |
| Runtime config | `src/main/resources/application.yml` | Current live config source |
| Logging | `src/main/resources/logback.xml` | Console logger only |
| Tests | `src/test/java/com/manleytech/DifyApiTest.java` | Smoke test only |

## CONVENTIONS
- Main flow is `DifyController` -> `KnowledgeProviderFactory` -> concrete `KnowledgeProvider` -> Micronaut `@Client` -> map back to `DifyQueryResponse`.
- Keep provider-specific translation logic inside the provider implementation. Shared callers should stay on the `KnowledgeProvider` interface.
- DTOs are split by external system and direction: `entity/{provider}/query` and `entity/{provider}/resp`.
- Dify DTOs are the canonical internal contract. External provider DTOs should adapt to them rather than leaking provider shapes upward.
- Current runtime config comes from `application.yml` + `@ConfigurationProperties`, even though the design docs describe a future DB-backed model.
- Reactive return type is `Mono`; follow existing Micronaut/Reactor style instead of mixing in blocking helper layers.

## ANTI-PATTERNS
- Do not hardcode provider selection outside `KnowledgeProviderFactory`.
- Do not return provider-native response objects from controllers; always map back to Dify response DTOs.
- Do not expose raw provider credentials or authorization tokens in logs, exceptions, or serialized DTOs.
- Do not assume `TestController` is product logic; it is a simple hello/json test endpoint.
- Do not add new provider behavior without updating both mapping/config classes and the provider implementation path.

## NOTES
- `BailianKnowledgeProvider.java` is the densest backend hotspot: rerank, rewrite, and top-k splitting logic all live there.
- `RAGflowKnowledgeProvider.java` follows the same adapter pattern with a simpler mapping path.
- `constant/dify/ComparisonOperator.java` contains validation logic, not just string constants.
- There is only one backend test, so behavior changes should be verified carefully.
