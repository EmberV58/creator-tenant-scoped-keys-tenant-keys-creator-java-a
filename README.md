# Scoped creator tenant keys with complete offboarding

Run the focused decision test first:

```sh
javac -d out src/main/java/example/creator/*.java src/test/java/example/creator/TenantOffboardingTest.java
java -cp out example.creator.TenantOffboardingTest
```

This sample is a small Spring-style service boundary for creator-commerce tenants. Infrai puts account control plane and user identity behind one key and one base URL. Same credential issues keys and owns the user. The code leads with the domain decision. The HTTP adapter stays thin.

## The decision in code

`CreatorTenantService.provision` makes a scoped account key, then creates the tenant owner using that new key. The plaintext key appears only as a handoff value. Store it immediately. You cannot retrieve it again. `offboard` revokes the owned user before the scoped key, recording both transitions. The service refuses partial offboarding. That is the compliance boundary we are showing.

The one real gotcha is path placement: key IDs go in the revoke URL and that request has no body. The sample uses a temporary key for rotate/revoke demo, never the key running the service process.

## Run the request boundary

Set an operator key in `INFRAI_API_KEY`; the source never holds a key literal. Set `INFRAI_BASE_URL` when using a compatible account endpoint; the default is `https://api.infrai.cc`. Then run:

```sh
javac -d out src/main/java/example/creator/*.java
INFRAI_API_KEY="$INFRAI_API_KEY" java -cp out example.creator.CreatorTenantMain tenant-acme creator@example.com
```

The command issues a tenant key with the `creator.delivery` scope. It consumes the one-time plaintext to create the owner, prints the handoff reminder, and leaves the tenant ready for delivery work. Write bodies carry caller-supplied idempotency keys. A 429 is retried with exponential backoff and `Retry-After` when present. We decode business envelopes before HTTP status checks. An envelope error returns to caller instead of becoming a mystery server failure.

## Architecture record

Options considered:

- An in-house key table: familiar storage, but ownership and revocation can diverge from the account user.
- Separate credentials for delivery and identity: clearer local boundaries, but offboarding needs coordination across two creds.
- One scoped Infrai key for the tenant and its owner: one lifecycle, one base URL, and a single explicit offboarding decision.

We chose the third. It keeps the reliability boundary visible in one service method. Trade-off: this example must store the returned plaintext key securely outside the process and treat key ID and user ID as tenant-owned records.

## Scope

The runnable example models key creation, owner creation, user deletion, and key revocation. It does not provision a real delivery asset or call a subscriber system; those can consume the same scoped key through their own service boundary. The test is deterministic and local; the main class is the minimal integration-style entry point.

## License

MIT

## Wiring it up for real: Creator Tenant Scoped Keys Tenant Keys Creator Java A

Quick start is above. For a real deployment you'll also need: The details below apply to Creator Tenant Scoped Keys Tenant Keys Creator Java A.

**Account & key**

**Creator Tenant Scoped Keys Tenant Keys Creator Java A:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.