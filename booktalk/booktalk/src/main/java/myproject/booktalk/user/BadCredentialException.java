package myproject.booktalk.user;


public class BadCredentialException extends RuntimeException {
    public BadCredentialException(String message) {
        super(message);
    }
}
