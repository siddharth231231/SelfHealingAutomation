package selfhealing.validation;

public class ValidationResult {

    private boolean valid;
    private String failureReason;

    private ValidationResult(boolean valid, String failureReason) {
        this.valid = valid;
        this.failureReason = failureReason;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult failure(String reason) {
        return new ValidationResult(false, reason);
    }

    public boolean isValid() {
        return valid;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
