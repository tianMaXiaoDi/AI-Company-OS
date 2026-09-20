# Refund Status Tool Calling

`RefundStatusTool` is a read-only, deterministic Java tool that demonstrates the data boundary for a customer-service agent. Its input schema is `{"orderId":"ORD-..."}`. Its response contains only tool-produced facts: `orderFound`, `refundStatus`, `refundAmount`, and `expectedArrivalAt`.

## Calling flow

```text
Customer question
  -> deterministic router confirms a refund-status question and extracts the customer's order ID
  -> Spring AI ChatClient sends the getRefundStatus(orderId) schema to Ollama
  -> model requests the Java tool; Spring AI executes the @Tool callback
  -> callback binds the call to the authenticated customer and original order ID
  -> Java result is added to the model conversation
  -> model returns fact-cited JSON; server validates it before replying
```

The active agent follows this pattern through `CustomerSupportAgent`, `SpringAiRefundStatusToolCalling`, and the server-owned `CustomerSupportTools` boundary. The model is never given a repository, a customer ID, or permission to select another order ID. If it omits the tool call, supplies a different order ID, returns invalid JSON, or Ollama is unavailable, `CustomerSupportAgent` falls back to the existing deterministic read path.

The standalone `RefundStatusTool` uses fixed Mock data so it can be demoed without a database:

| orderId | result |
| --- | --- |
| `ORD-REFUND-PROCESSING` | `PROCESSING`, 88.50, expected 2026-09-23T10:00:00Z |
| `ORD-REFUND-COMPLETED` | `COMPLETED`, 129.00, arrived 2026-09-18T10:00:00Z |
| any other ID | explicit `orderFound=false`; no status, amount, or date is fabricated |

If an order ID is absent, the agent asks for it and does not call the tool. Production lookups additionally verify customer ownership before returning any refund data. If a tool times out or fails, the deterministic fallback handles the request rather than inventing refund state.

## Run the Spring AI path

Install Ollama 0.2.8+ and pull a tool-capable chat model, then start the application with the `llm` profile:

```powershell
ollama pull qwen3:4b
$env:SPRING_PROFILES_ACTIVE = 'dev,semantic,llm'
mvn spring-boot:run
```

`legacy-llm` keeps the earlier direct-HTTP Ollama adapters for comparison; it is not part of the default Spring AI path.
