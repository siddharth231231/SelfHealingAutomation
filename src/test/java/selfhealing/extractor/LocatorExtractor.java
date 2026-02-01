package selfhealing.extractor;

public class LocatorExtractor {

    public static String extract(Throwable exception) {

        if (exception == null || exception.getMessage() == null) {
            return null;
        }

        String message = exception.getMessage();

        // Temporary safe extraction (Phase 1)
        if (message.contains("xpath")) {
            return message;
        }

        return null;
    }
}
