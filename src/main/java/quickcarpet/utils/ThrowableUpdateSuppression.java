package quickcarpet.utils;

public class ThrowableUpdateSuppression extends RuntimeException {
    public ThrowableUpdateSuppression(String message) {
        super(message);
    }

    public ThrowableUpdateSuppression(String message, StackOverflowError cause) {
        super(message, cause);
    }
}
