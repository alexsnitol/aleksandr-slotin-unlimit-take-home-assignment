# Issue Analyzer

An AI-powered service that analyzes incoming incident/issue reports for services and infrastructure. Built with **Spring
AI**, **Ollama (gemma4:e4b)**, and **MongoDB** for persistent incident history.

***

## Tech Stack

| Component       | Technology                         |
|-----------------|------------------------------------|
| Framework       | Spring Boot 4.0.6                  |
| AI Integration  | Spring AI 2.0.0-M6 + Ollama        |
| LLM Model       | `gemma4:e4b` (via Ollama)          |
| Database        | MongoDB (incident history storage) |
| Circuit Breaker | Resilience4j (via Spring Cloud)    |
| API Docs        | SpringDoc OpenAPI (Swagger UI)     |
| Mapping         | MapStruct                          |
| Monitoring      | Spring Boot Actuator               |

***

## How to Run

### Prerequisites

- Java 21+
- Maven 3.8+
- [Ollama](https://ollama.com/) installed and running locally
- MongoDB instance available

### Steps

1. **Pull the required Ollama model:**
   ```bash
   ollama pull gemma4:e4b
   ```

2. **Start MongoDB** (using Docker Compose):
   ```bash
   docker-compose up -d
   ```

3. **Configure connection settings** in `src/main/resources/application.yaml` (defaults shown below):
   ```yaml
   spring:
     ai:
       ollama:
         base-url: http://localhost:11434
         chat:
           options:
             model: gemma4:e4b
             temperature: 0.4
     data:
       mongodb:
         uri: mongodb://localhost:27017/issue_analyzer
   ```

4. **Build and run the application:**
   ```bash
   mvn spring-boot:run
   ```

5. **Open Swagger UI** to interact with the API:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

***

## Agent Architecture

The core agent workflow is implemented in `AnalyzerIssueService` and `AnalyseIssueAiService`.

**When a new incident text is submitted, the workflow proceeds as follows:**

1. **Input validation** - the service checks that the input length is within the allowed range (10–8000 characters) and
   filters out suspicious prompt injection patterns via `InjectionPatternFilter`.
2. **History retrieval** - the last 10 resolved incidents are loaded from MongoDB (ordered by `createdAt` descending)
   and passed to the AI as context, enabling the model to detect recurring patterns or related past incidents.
3. **AI analysis** - `AnalyseIssueAiService` sends the current incident text along with the history to the Ollama-hosted
   `gemma4:e4b` model. The model produces a structured analysis response (`AnalyzeIssueResponse`) containing diagnosis,
   severity assessment, hypotheses and suggested next steps.
4. **Persistence** - the analysis result is saved back to MongoDB, so it becomes part of the history for future
   requests.
5. **Response** - the structured response is returned to the caller.

This design creates a **feedback loop**: each new incident becomes part of the rolling history window, so the agent
continuously improves its contextual awareness of recurring issues without requiring a full vector database or RAG
pipeline.

```
Incoming Issue Text
        │
        ▼
  Input Validation
  (length + injection filter)
        │
        ▼
 Load Last 10 Incidents
    from MongoDB
        │
        ▼
  Send to LLM (gemma4:e4b)
  [issue + history as context]
        │
        ▼
  Structured AI Response
  (diagnosis, severity, hypotheses)
        │
        ▼
   Save to MongoDB
        │
        ▼
  Return Response to Caller
```

***

## Trade-offs

### Simplified for ~3 hours

- Used a simple rolling window of 10 recent incidents as context instead of a proper vector store + semantic search (
  RAG). This is less precise for large incident histories but requires zero additional infrastructure.
- No authentication or multi-tenancy - the API is open and all incidents share a single namespace.
- Minimal error handling on the AI layer beyond Retry via Resilience4j.
- Minimal prompt injection protection via a simple regex filter, which is not comprehensive but serves as a basic
  safeguard.

### With more time

- Replace the rolling history window with a vector database and semantic
  similarity search for truly relevant past incident retrieval.
- Implement authentication and multi-tenant incident namespaces.
- Additional LLM-based prompt injection checks beyond the current pattern filter.
- Hallucination detection via LLM after receiving the model's analysis response (e.g., verify response alignment with
  the original incident and `service context`).
- Add code-level JSON parsing recovery to handle malformed responses from smaller models.
- Evaluate and switch to larger model sizes.
- Write proper integration tests with a containerized Ollama stub or WireMock.

***

## Testing Behavior

Testing was performed manually by submitting incident descriptions via API and observing how the agent's
response changed as incident history accumulated in MongoDB.

The key aspect under test was that, as incident history accumulated, the agent's hypotheses became more specific and its
next steps more substantive, demonstrating that the rolling history context window works as intended.