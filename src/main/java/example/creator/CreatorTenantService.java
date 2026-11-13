package example.creator;

public final class CreatorTenantService {
    public enum State { READY, USER_OFFBOARDED, KEY_REVOKED }
    private final InfraiAccountClient client;

    public CreatorTenantService(InfraiAccountClient client) { this.client = client; }

    public TenantRecord provision(String tenant, String email) throws Exception {
        InfraiAccountClient.KeyCredentials credentials = client.createKey(tenant + " scoped key", "creator.delivery", tenant + "-key-create");
        String keyId = credentials.keyId();
        String plaintextKey = credentials.plaintextKey();
        String userId = client.createUser(plaintextKey, email, tenant + "-owner-create");
        return new TenantRecord(tenant, keyId, userId, plaintextKey, State.READY);
    }

    public TenantRecord offboard(TenantRecord tenant) throws Exception {
        if (tenant.state() != State.READY) throw new IllegalStateException("tenant is not ready for offboarding");
        client.deleteUser(tenant.plaintextKey(), tenant.userId());
        TenantRecord userGone = tenant.withState(State.USER_OFFBOARDED);
        client.revokeKey(userGone.keyId());
        return userGone.withState(State.KEY_REVOKED);
    }

    public record TenantRecord(String tenant, String keyId, String userId, String plaintextKey, State state) {
        TenantRecord withState(State next) { return new TenantRecord(tenant, keyId, userId, plaintextKey, next); }
    }
}
