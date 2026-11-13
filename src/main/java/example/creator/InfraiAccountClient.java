package example.creator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiAccountClient {
    private static final String ACCOUNT_KEYS_CREATE = "account.keys.create";
    private final HttpClient http;
    private final String baseUrl;
    private final String operatorKey;

    public InfraiAccountClient(String baseUrl, String operatorKey) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.baseUrl = baseUrl;
        this.operatorKey = operatorKey;
    }

    public KeyCredentials createKey(String name, String scope, String idempotencyKey) throws Exception {
        if (ACCOUNT_KEYS_CREATE.isEmpty()) throw new IllegalStateException("missing capability");
        String body = "{\"name\":\"" + json(name) + "\",\"scopes\":[\"" + json(scope) + "\"],\"idempotency_key\":\"" + json(idempotencyKey) + "\"}";
        String response = write("POST", "/v1/account/keys/create", body, null);
        String keyId = value(response, "key_id");
        String plaintextKey = value(response, "key");
        if (plaintextKey.isEmpty()) plaintextKey = value(response, "plaintext_key");
        if (plaintextKey.isEmpty()) plaintextKey = value(response, "key_secret");
        if (keyId.isEmpty() || plaintextKey.isEmpty()) throw new IllegalStateException("key creation response omitted key credentials");
        return new KeyCredentials(keyId, plaintextKey);
    }

    public String createUser(String key, String email, String idempotencyKey) throws Exception {
        String body = "{\"email\":\"" + json(email) + "\",\"name\":\"Tenant owner\",\"idempotency_key\":\"" + json(idempotencyKey) + "\"}";
        return writeWithKey(key, "POST", "/v1/auth/user/create", body, "user_id");
    }

    public void deleteUser(String key, String userId) throws Exception {
        writeWithKey(key, "DELETE", "/v1/auth/user/delete/" + jsonPath(userId), "", null);
    }

    public void revokeKey(String keyId) throws Exception {
        write("DELETE", "/v1/account/keys/revoke/" + jsonPath(keyId), "", null);
    }

    private String write(String method, String path, String body, String field) throws Exception {
        return writeWithKey(operatorKey, method, path, body, field);
    }

    private String writeWithKey(String key, String method, String path, String body, String field) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response;
        try { response = sendWithBackoff(request); }
        catch (IOException | InterruptedException e) { throw e; }
        String envelope = response.body();
        boolean ok = envelope.contains("\"ok\":true");
        if (!ok) throw new InfraiError(value(envelope, "code"), value(envelope, "message"), response.statusCode());
        return field == null ? envelope : value(envelope, field);
    }

    private HttpResponse<String> sendWithBackoff(HttpRequest request) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 429 || attempt == 3) return response;
            long delay = response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L << attempt);
            Thread.sleep(Math.min(delay, 8) * 1000L);
        }
        throw new IOException("request retry exhausted");
    }

    private static String value(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return "";
        start += marker.length();
        int end = json.indexOf('"', start);
        return end < 0 ? "" : json.substring(start, end);
    }
    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String jsonPath(String value) { return value.replaceAll("[^A-Za-z0-9_-]", ""); }

    public record KeyCredentials(String keyId, String plaintextKey) {}
}
