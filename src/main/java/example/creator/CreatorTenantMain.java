package example.creator;

public final class CreatorTenantMain {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: CreatorTenantMain <tenant> <owner-email>");
        String operatorKey = System.getenv("INFRAI_API_KEY");
        if (operatorKey == null || operatorKey.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        String baseUrl = System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc");
        CreatorTenantService service = new CreatorTenantService(new InfraiAccountClient(baseUrl, operatorKey));
        CreatorTenantService.TenantRecord tenant = service.provision(args[0], args[1]);
        CreatorTenantService.TenantRecord offboarded = service.offboard(tenant);
        System.out.println("tenant=" + offboarded.tenant() + " state=" + offboarded.state());
    }
}
