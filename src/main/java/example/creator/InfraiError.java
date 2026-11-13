package example.creator;

public final class InfraiError extends Exception {
    private final String code;
    private final int status;

    public InfraiError(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public int status() { return status; }
}
