package selfhealing.context.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BrokenXpathAnalyzer {

    private static final Pattern TAG_PATTERN = Pattern.compile("^/*([a-zA-Z*][a-zA-Z0-9_-]*)");
    private static final Pattern TEXT_EQ_PATTERN = Pattern.compile("text\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern CONTAINS_TEXT_PATTERN = Pattern.compile("contains\\(\\s*text\\(\\)\\s*,\\s*['\"]([^'\"]+)['\"]\\s*\\)");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("@([a-zA-Z_:][-a-zA-Z0-9_:.]*)\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern CONTAINS_ATTRIBUTE_PATTERN = Pattern.compile("contains\\(\\s*@([a-zA-Z_:][-a-zA-Z0-9_:.]*)\\s*,\\s*['\"]([^'\"]+)['\"]\\s*\\)");

    private BrokenXpathAnalyzer() {
    }

    public static BrokenXpathAnalysis analyze(String xpath) {
        BrokenXpathAnalysis out = new BrokenXpathAnalysis();
        out.setRawXpath(xpath);
        String sanitized = sanitize(xpath);
        if (isBlank(sanitized)) {
            return out;
        }

        String normalized = normalize(sanitized);
        out.setNormalizedXpath(normalized);
        out.setDepth(estimateDepth(normalized));
        out.setExpectedTag(extractTag(normalized));
        out.setExpectedText(extractText(normalized));
        out.setExpectedAttributes(extractAttributes(normalized));

        int risk = 0;
        List<String> reasons = new ArrayList<>();
        String low = normalized.toLowerCase();
        if (low.contains("[") && low.contains("]") && low.matches(".*\\[\\d+\\].*")) {
            risk += 2;
            reasons.add("Uses positional index predicates.");
        }
        if (low.contains("@id") && low.matches(".*@id\\s*=\\s*['\"][^'\"]*\\d{3,}[^'\"]*['\"].*")) {
            risk += 3;
            reasons.add("ID predicate looks dynamic (numeric suffix).");
        }
        if (low.contains("@class") && low.matches(".*@class\\s*=\\s*['\"][^'\"]*\\d{3,}[^'\"]*['\"].*")) {
            risk += 2;
            reasons.add("Class predicate looks dynamic (numeric token).");
        }
        if (low.contains("contains(@class")) {
            risk += 1;
            reasons.add("contains(@class,...) is often unstable in component frameworks.");
        }
        out.setDynamicRiskScore(Math.min(risk, 10));
        out.setRiskReasons(reasons);
        return out;
    }

    public static String sanitize(String xpath) {
        if (isBlank(xpath)) {
            return null;
        }
        String trimmed = xpath.trim();

        // Extract between "The string '...'" if present in exception text.
        String marker = "The string '";
        int markerIdx = trimmed.indexOf(marker);
        if (markerIdx >= 0) {
            int start = markerIdx + marker.length();
            int end = trimmed.indexOf("'", start);
            if (end > start) {
                trimmed = trimmed.substring(start, end);
            }
        }

        // Strip surrounding quotes
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
                (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        // Strip surrounding [ ] if it looks like "[//...]" wrapper.
        if (trimmed.startsWith("[") && trimmed.endsWith("]") && trimmed.contains("//")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        return trimmed;
    }

    private static String normalize(String xpath) {
        return xpath.replaceAll("\\s+", " ").trim();
    }

    private static Integer estimateDepth(String xpath) {
        int depth = 0;
        for (int i = 0; i < xpath.length(); i++) {
            if (xpath.charAt(i) == '/') {
                depth++;
            }
        }
        return Math.max(depth - 1, 1);
    }

    private static String extractTag(String xpath) {
        Matcher matcher = TAG_PATTERN.matcher(xpath);
        return matcher.find() ? matcher.group(1) : "*";
    }

    private static String extractText(String xpath) {
        Matcher textEq = TEXT_EQ_PATTERN.matcher(xpath);
        if (textEq.find()) {
            return textEq.group(1).trim();
        }
        Matcher containsText = CONTAINS_TEXT_PATTERN.matcher(xpath);
        if (containsText.find()) {
            return containsText.group(1).trim();
        }
        return null;
    }

    private static Map<String, String> extractAttributes(String xpath) {
        Map<String, String> attrs = new LinkedHashMap<>();

        Matcher exactMatcher = ATTRIBUTE_PATTERN.matcher(xpath);
        while (exactMatcher.find()) {
            attrs.put(exactMatcher.group(1), exactMatcher.group(2));
        }

        Matcher containsMatcher = CONTAINS_ATTRIBUTE_PATTERN.matcher(xpath);
        while (containsMatcher.find()) {
            attrs.put(containsMatcher.group(1), containsMatcher.group(2));
        }

        return attrs;
    }

    private static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
