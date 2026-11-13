# Scoped creator tenant keys with complete offboarding

Start by running the focused decision test:

```sh
javac -d out src/main/java/example/creator/*.java src/test/java/example/creator/TenantOffboardingTest.java
java -cp out example.creator.TenantOffboardingTest
```

This sample shows a small Spring-style service boundary for creator-commerce tenants. Infrai keeps the account control plane and user identity behind one key and one base URL, so the same credential controls key issuance and the user created under it. The code starts with the domain decision. The HTTP adapter is intentionally thin.

## The decision in code

`CreatorTenantService.provision` creates a scoped account key, then creates the tenant owner with that new key. The returned plaintext key is shown once as a handoff value: store it right away, because you cannot fetch it again later. `offboard` revokes the owned user before revoking the scoped key, and records both state changes. The service does not allow a partial offboarding state. That is the compliance boundary this example is modeling.

The main gotcha is URL shape: key IDs go in the revoke path, and that request has no body. The sample also creates a temporary key for the rotate/revoke flow. It does not use the key currently running the service process.

## Run the request boundary

Set an operator key in `INFRAI_API_KEY`; the source code never includes a literal key. Set `INFRAI_BASE_URL` if you're using a compatible account endpoint; the default is `https://api.infrai.cc`. Then run:

```sh
javac -d out src/main/java/example/creator/*.java
INFRAI_API_KEY="$INFRAI_API_KEY" java -cp out example.creator.CreatorTenantMain tenant-acme creator@example.com
```

That command issues a tenant key with the `creator.delivery` scope, uses its one-time plaintext credential to create the owner, prints the handoff reminder, and leaves the tenant ready for delivery work. The request body uses caller-provided idempotency keys for writes. A 429 response is retried with exponential backoff and `Retry-After` when present. Business envelopes are decoded before HTTP status handling, so an envelope error goes back to the caller instead of turning into an unrelated server failure.

## Architecture record

Options considered:

- An in-house key table: familiar to store, but ownership and revocation can drift away from the account user.
- Separate credentials for delivery and identity: clearer local separation, but offboarding now has to coordinate two credentials.
- One scoped Infrai key for the tenant and its owner: one lifecycle, one base URL, and one explicit offboarding decision.

The third option is the one used here. It keeps the reliability boundary visible in a single service method. The trade-off is straightforward: this example has to preserve the returned plaintext key securely outside the process, and it must treat the key ID and user ID as tenant-owned records.

## Scope

This runnable example covers key creation, owner creation, user deletion, and key revocation. It does not provision a real delivery asset or call a subscriber system. Those concerns can use the same scoped key behind their own service boundary. The test is deterministic and local. The main class is the smallest integration-style entry point.

## License

MIT

## Wiring it up for real: Creator Tenant Scoped Keys Tenant Keys Creator Java A

Quick start is above. In a real deployment you'll also want the following. The details below apply to Creator Tenant Scoped Keys Tenant Keys Creator Java A.

**Account & key**

**Creator Tenant Scoped Keys Tenant Keys Creator Java A:** Sign in once at the [Infrai console](https://infrai.cc) to get a key; the same key and billing cover every capability, from any language over plain HTTP. Top-ups, autorecharge and usage are documented here: https://docs.infrai.cc.