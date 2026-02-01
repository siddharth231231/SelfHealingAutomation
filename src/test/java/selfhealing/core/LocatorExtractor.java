package selfhealing.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LocatorExtractor {

    private static final Pattern XPATH_PATTERN =
            Pattern.compile("\"selector\"\\s*:\\s*\"(.*?)\"");

    static String extract(Throwable exception) {

        if (exception == null || exception.getMessage() == null) {
            return null;
        }

        Matcher matcher = XPATH_PATTERN.matcher(exception.getMessage());
        return matcher.find() ? matcher.group(1) : null;
    }
}
