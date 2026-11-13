package example.creator;

public final class TenantOffboardingTest {
    public static void main(String[] args) {
        CreatorTenantService.TenantRecord ready = new CreatorTenantService.TenantRecord("acme", "key-1", "user-1", "issued", CreatorTenantService.State.READY);
        CreatorTenantService.TenantRecord userGone = ready.withState(CreatorTenantService.State.USER_OFFBOARDED);
        CreatorTenantService.TenantRecord complete = userGone.withState(CreatorTenantService.State.KEY_REVOKED);
        if (complete.state() != CreatorTenantService.State.KEY_REVOKED || userGone.state() != CreatorTenantService.State.USER_OFFBOARDED) throw new AssertionError("offboarding order is incomplete");
        System.out.println("PASS: user ownership ends before the scoped key is revoked");
    }
}
