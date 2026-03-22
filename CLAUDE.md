# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

dify-external-knowledge is an API forwarding service for Dify that routes external knowledge base retrieval requests to different RAG frameworks (e.g., Alibaba Bailian, RAGflow). All configuration (Bailian credentials, RAGflow credentials, Dify knowledge base mappings) is stored in MySQL, managed through a Vue 3 frontend.

## Tech Stack

**Frontend:** Vue 3 + Element Plus + Vite (pnpm)
**Backend:** Micronaut 4.x + Java 17 + MySQL 8.0
**Build:** Maven for backend

## Common Commands

### Frontend (web/)
```bash
cd web
pnpm install          # Install dependencies
pnpm run dev         # Start development server
pnpm run build       # Build for production
pnpm run preview     # Preview production build
```

### Backend (backend/)
```bash
cd backend
./mvnw clean package # Build the application
./mvnw mn:run        # Run in development mode
```

## Architecture

### Frontend Structure (web/src/)
- `views/` - Page components: Dashboard, KnowledgeMappings, RagConfig, Logs, ConfigHistory
- `components/` - Reusable Vue components
- `api/` - Axios API clients for backend communication
- `store/` - Pinia state management
- `router/` - Vue Router configuration

### Backend Structure (backend/src/main/java/com/manleytech/)
- `provider/` - KnowledgeProvider interface and implementations (BailianKnowledgeProvider, RAGflowKnowledgeProvider)
- `provider/client/` - API clients for external RAG services
- `entity/` - Request/response DTOs organized by provider (bailian, dify, ragflow)
- `constant/` - Enums and constants (ErrorCode, ComparisonOperator)
- `web/` - Controllers (DifyController, TestController)

### Core Pattern: Strategy + Factory
KnowledgeProviderFactory uses the Strategy pattern - it selects the appropriate KnowledgeProvider implementation based on `knowledge_id` mapping in the database. New RAG providers are added by implementing the `KnowledgeProvider` interface.

### Data Flow
1. Dify sends POST `/dify/retrieval` request
2. DifyController receives and validates the request
3. KnowledgeProviderFactory looks up the knowledge_id mapping in MySQL
4. Factory returns the appropriate provider (Bailian or RAGflow)
5. Provider forwards the request to the external RAG API
6. Provider transforms the response to Dify format
7. DifyController returns the standardized response to Dify

### Database Tables
- `knowledge_mapping` - Maps Dify knowledge_id to RAG provider + target_id
- `rag_config` - Stores API credentials and parameters per RAG provider
- `api_log` - Logs all API requests/responses
- `config_history` - Audit trail for configuration changes

## Key API Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /dify/retrieval` | Main entry point - Dify calls this for knowledge queries |
| `GET/POST/PUT/DELETE /api/config/mappings` | Manage knowledge base mappings |
| `GET/PUT /api/config/rag/{provider_type}` | Manage RAG provider credentials |
| `GET /api/logs` | Query API logs |
| `GET /api/config/history` | Query configuration change history |
