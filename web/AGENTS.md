# FRONTEND KNOWLEDGE BASE

## OVERVIEW
Vue 3 + Vite + Element Plus admin UI for dashboard metrics, knowledge mappings, provider configuration, logs, and configuration history.

## STRUCTURE
```text
web/
|- package.json
|- vite.config.js
|- src/
|  |- views/              # route-level pages
|  |- components/common/  # app shell pieces
|  |- api/                # domain API wrappers + enums
|  |- store/modules/      # Pinia stores matching API domains
|  |- router/
|  `- utils/request.js    # Axios instance and interceptors
`- .github/copilot-instructions.md
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| App bootstrap | `src/main.js` | Registers Element Plus, router, Pinia |
| Root shell | `src/App.vue` | Global layout mount and global styles |
| Route map | `src/router/index.js` | 5 top-level routes, lazy loaded |
| Layout/navigation | `src/components/common/` | Header, sidebar, breadcrumb, layout |
| HTTP behavior | `src/utils/request.js` | Base URL, auth header, interceptors, upload/download |
| Config domain | `src/api/config.js` + `src/store/modules/config.js` | Mappings, provider config, history, rollback |
| Dashboard domain | `src/api/dashboard.js` | Metrics endpoints |
| Logs domain | `src/api/log.js` + `src/store/modules/log.js` | Log state and filtering |
| Main page complexity | `src/views/Dashboard.vue` | Largest frontend hotspot |
| Frontend coding rules | `.github/copilot-instructions.md` | JS-only, `<script setup>`, scoped styles |

## CONVENTIONS
- Frontend is JavaScript-only. Do not introduce TypeScript files or TS-specific tooling.
- Use Vue 3 Composition API with `<script setup>`; this is explicitly documented in `.github/copilot-instructions.md`.
- Keep component styles scoped by default. Global resets/utilities live in `src/App.vue` and `src/style.css`.
- API/store modules mirror each other by domain. When adding a new feature area, start with a matching pair in `src/api/` and `src/store/modules/`.
- API enums/constants live beside their API module, not in a shared constants tree.
- Route titles, labels, and most UI copy are Chinese; preserve that local convention.

## ANTI-PATTERNS
- Do not bypass `src/utils/request.js` for normal HTTP work; that file owns auth header injection and shared error handling.
- Do not scatter config-domain logic across unrelated stores; mappings, provider config, and history already live in `src/store/modules/config.js`.
- Do not assume frontend tests exist; no Vitest/Jest setup or test scripts are present.
- Do not keep unused scaffolding alive unless it is intentional; `src/components/HelloWorld.vue` and `src/components/DemoCard.vue` look like starter/demo files.
- Do not rely on undocumented response envelopes; existing code expects backend responses to surface `data` consistently through the request wrapper.

## NOTES
- `src/utils/request.js` is a major hotspot: request metadata, token handling, error mapping, file upload/download all live there.
- `src/views/Dashboard.vue` is the largest view and currently contains placeholder chart handling rather than a full ECharts integration.
- `src/store/modules/config.js` is the densest state module and the best reference for CRUD-style flows.
- `vite.config.js` is minimal; most frontend behavior is encoded in code structure, not build tooling.
