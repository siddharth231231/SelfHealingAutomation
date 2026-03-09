package selfhealing.core;

public final class PageUrlUtil {

    private static final int PAGE_URL_MAX = 500;

    private PageUrlUtil() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.length() <= PAGE_URL_MAX) {
            return trimmed;
        }
        return trimmed.substring(0, PAGE_URL_MAX);
    }
}
