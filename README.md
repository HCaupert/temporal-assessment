# Order Processing Application (OMS)

## Disclaimer 
Done in two hours - better iteration coming next week 

Temporal-based order processing system handling the **Processing** phase of the order lifecycle.

## Architecture

```
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│  Commerce App   │────▶│ OrderProcessingWF    │────▶│  Kafka Output   │
│  (webhooks)     │     │  - validation        │     │  (fulfillment)  │
└─────────────────┘     │  - enrichment        │     └─────────────────┘
                        │  - allocation        │
┌─────────────────┐     └──────────────────────┘
│ Payment System  │────▶│ PaymentValidationWF  │
│  (webhooks)     │     └──────────────────────┘
└─────────────────┘
```

**Two workflows** handle the processing:
- `OrderProcessingWorkflow` - orchestrates order validation, enrichment, and fulfillment
- `PaymentValidationWorkflow` - validates payment status and signals the order workflow. 
  - Payment systems often do more than just waiting for webhooks (polling, user redirections) so the payment workflow is kept separated.

## Design Decisions

### Webhook Handling
All webhook endpoints use **signal-with-start** to handle out-of-order events. Payment can arrive before order creation - both cases are handled gracefully.

### Rate Limiting (Commerce App API)
The StoreFront API is rate-limited at 150 RPS. Worker concurrency is configured based on deployed instances:
```
allowedConcurrency = rateLimitPerSec / instancesDeployed
```

### Data Flow
Activities access the datastore directly - no need to pass enriched data through workflow state. The datastore serves as source of truth.

### Support Team Corrections
Invalid orders are written to the customer dashboard. Support team can retrigger order processing via the storefront webhook endpoint (supposed)


## PII Protection (Future)

A `PayloadCodec` implementation is ready for encrypting sensitive data:
- Uses Spring Security's `BytesEncryptor`
- Requires a codec server behind VPC for UI visibility

See `TemporalEncryptionCodec.java` for implementation.

## vNext: Risk Collection

For adding risk input collection after go-live, versioning strategy is covered here by *me* during an official temporal webinar:  
📺 [Temporal Versioning Webinar](https://youtu.be/FIJC82-eUNQ)

## Simplifications

- Activity implementations are mocked (logging only)
- Kafka publishing abstracted in `allocateOrder()` activity
- Hardcoded values for demo purposes
- Test setup present but not implemented (time-boxed)
- Temporal local configuration is used and not configurable
- temporal spring boot starter is not used

## Running

```bash
mvn spring-boot:run
```
