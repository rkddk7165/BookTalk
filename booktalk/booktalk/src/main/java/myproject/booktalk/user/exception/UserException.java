package myproject.booktalk.user.exception;

public class UserException extends RuntimeException {

    private final String code;
    private final int status;

    public UserException(String code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
