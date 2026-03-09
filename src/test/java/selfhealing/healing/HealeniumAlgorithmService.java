package selfhealing.healing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public final class HealeniumAlgorithmService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HealeniumAlgorithmService() {
    }

    public static HealeniumSuggestion suggest(
            String storedNodePathJson,
            List<LiveNodeCandidate> liveCandidates) {

        Node stored = parseNode(storedNodePathJson);
        if (stored == null) {
            return new HealeniumSuggestion(null, 0);
        }

        // Fallback: if no live candidates, still emit heuristic xpaths from stored node
        if (liveCandidates == null || liveCandidates.isEmpty()) {
            return bestFromGenerated(stored);
        }

        double maxScore = -1;
        String bestXpath = null;
        List<HealeniumSuggestion> ranked = new ArrayList<>();

        for (LiveNodeCandidate candidate : liveCandidates) {
            Node live = toNode(candidate.getNodePath());
            if (live == null) {
                continue;
            }

            double score = similarity(stored, live);

            String candidateXpath = candidate.getXpath();
            if (candidateXpath == null || candidateXpath.isBlank()) {
                candidateXpath = generateXpathFromNode(live);
            }

            ranked.add(new HealeniumSuggestion(candidateXpath, round(score)));
            if (score > maxScore) {
                maxScore = score;
                bestXpath = candidateXpath;
            }
        }

        ranked.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // If no ranked suggestions, fallback to stored generation
        if (ranked.isEmpty()) {
            return bestFromGenerated(stored);
        }

        return new HealeniumSuggestion(bestXpath, round(maxScore));
    }

    private static HealeniumSuggestion bestFromGenerated(Node stored) {
        List<String> generated = generateHeuristicXpaths(stored);
        if (generated.isEmpty()) {
            return new HealeniumSuggestion(null, 0);
        }
        return new HealeniumSuggestion(generated.get(0), 0.55); // baseline confidence
    }

    private static Node parseNode(String nodeJson) {
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(nodeJson, new TypeReference<Map<String, Object>>() {});
            return toNode(map);
        } catch (Exception e) {
            return null;
        }
    }

    private static Node toNode(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Node node = new Node();
        node.tag = lower(text(map.get("tag")));
        node.innerText = normalizeText(text(map.get("innerText")));
        node.attributes = stringMap(map.get("attributes"));
        node.children = new ArrayList<>();
        for (Object child : list(map.get("children"))) {
            Node childNode = toNode(map(child));
            if (childNode != null) {
                node.children.add(childNode);
            }
        }
        node.parent = toNode(map(map.get("parent")));
        return node;
    }

    private static double similarity(Node stored, Node live) {
        double tagScore = equals(stored.tag, live.tag) ? 1.0 : 0.0;
        double attrScore = attributeSimilarity(stored.attributes, live.attributes);
        double textScore = textSimilarity(stored.innerText, live.innerText);
        double childScore = childSimilarity(stored.children, live.children);
        double parentScore = parentSimilarity(stored.parent, live.parent);
        double pathScore = pathSimilarity(stored, live);

        return (tagScore * 0.30)
                + (attrScore * 0.25)
                + (textScore * 0.15)
                + (childScore * 0.12)
                + (parentScore * 0.08)
                + (pathScore * 0.10);
    }

    /**
     * Builds heuristic XPaths similar to Healenium's reconstruction strategy.
     */
    private static List<String> generateHeuristicXpaths(Node node) {
        List<String> out = new ArrayList<>();
        String tag = node == null ? null : node.tag;
        String id = attr(node, "id");
        String name = attr(node, "name");
        String dataTestId = attr(node, "data-testid");
        String cls = firstClass(attr(node, "class"));
        String text = node == null ? null : node.innerText;

        add(out, eq(tag, "id", id));
        add(out, eq(tag, "name", name));
        add(out, eq(tag, "data-testid", dataTestId));
        add(out, contains(tag, "id", id));
        add(out, contains(tag, "class", cls));
        add(out, text(tag, text));

        // parent chain reconstruction
        Node parent = node == null ? null : node.parent;
        if (parent != null && parent.tag != null) {
            String pId = attr(parent, "id");
        add(out, "//" + parent.tag + (pId != null ? "[@id='" + escape(pId) + "']" : "") + "//" + tag);
        }

        // sibling relation using label
        if (text != null && text.length() <= 40) {
            add(out, "//label[normalize-space(text())='" + escape(text) + "']/following::" + tag + "[1]");
        }

        // hierarchy path
        List<String> path = tagPath(node);
        if (!path.isEmpty()) {
            add(out, "/" + String.join("/", path));
        }
        return out;
    }

    private static void add(List<String> out, String xpath) {
        if (out == null || xpath == null) {
            return;
        }
        String trimmed = xpath.trim();
        if (!trimmed.isEmpty()) {
            out.add(trimmed);
        }
    }

    private static String eq(String tag, String attr, String value) {
        if (!notBlank(tag) || !notBlank(value)) {
            return null;
        }
        return "//" + tag + "[@" + attr + "='" + escape(value) + "']";
    }

    private static String contains(String tag, String attr, String value) {
        if (!notBlank(tag) || !notBlank(value)) {
            return null;
        }
        String fragment = value.length() > 18 ? value.substring(0, 18) : value;
        return "//" + tag + "[contains(@" + attr + ",'" + escape(fragment) + "')]";
    }

    private static String text(String tag, String value) {
        if (!notBlank(tag) || !notBlank(value)) {
            return null;
        }
        return "//" + tag + "[normalize-space(text())='" + escape(value) + "']";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "\\'");
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String generateXpathFromNode(Node node) {
        List<String> generated = generateHeuristicXpaths(node);
        return generated.isEmpty() ? null : generated.get(0);
    }

    private static double attributeSimilarity(Map<String, String> left, Map<String, String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        int hits = 0;
        int total = 0;

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(left.keySet());
        keys.retainAll(Set.of(
                "data-testid", "data-cy", "aria-label", "name", "type",
                "placeholder", "role", "href", "id", "class"
        ));
        for (String key : keys) {
            total++;
            String lv = normalizeText(left.get(key));
            String rv = normalizeText(right.get(key));
            if (equals(lv, rv) || (lv != null && rv != null && rv.contains(lv))) {
                hits++;
            }
        }
        return total == 0 ? 0 : (double) hits / total;
    }

    private static double textSimilarity(String left, String right) {
        if (left == null || right == null) {
            return 0;
        }
        if (left.equalsIgnoreCase(right)) {
            return 1;
        }
        if (right.toLowerCase(Locale.ROOT).contains(left.toLowerCase(Locale.ROOT))) {
            return 0.7;
        }

        Set<String> leftTokens = new HashSet<>(Arrays.asList(left.toLowerCase(Locale.ROOT).split("\\s+")));
        Set<String> rightTokens = new HashSet<>(Arrays.asList(right.toLowerCase(Locale.ROOT).split("\\s+")));
        leftTokens.removeIf(String::isBlank);
        rightTokens.removeIf(String::isBlank);
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0;
        }
        Set<String> intersection = new HashSet<>(leftTokens);
        intersection.retainAll(rightTokens);
        Set<String> union = new HashSet<>(leftTokens);
        union.addAll(rightTokens);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private static double childSimilarity(List<Node> left, List<Node> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        Set<String> leftTags = new LinkedHashSet<>();
        Set<String> rightTags = new LinkedHashSet<>();
        for (Node node : left) {
            if (node != null && node.tag != null) {
                leftTags.add(node.tag);
            }
        }
        for (Node node : right) {
            if (node != null && node.tag != null) {
                rightTags.add(node.tag);
            }
        }
        if (leftTags.isEmpty() || rightTags.isEmpty()) {
            return 0;
        }
        Set<String> intersection = new HashSet<>(leftTags);
        intersection.retainAll(rightTags);
        Set<String> union = new HashSet<>(leftTags);
        union.addAll(rightTags);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private static String attr(Node node, String name) {
        if (node == null || node.attributes == null) {
            return null;
        }
        return node.attributes.get(name);
    }

    private static String firstClass(String classes) {
        if (classes == null || classes.isBlank()) {
            return null;
        }
        return classes.trim().split("\\s+")[0];
    }

    private static double parentSimilarity(Node left, Node right) {
        if (left == null || right == null) {
            return 0;
        }
        double tag = equals(left.tag, right.tag) ? 1 : 0;
        double attrs = attributeSimilarity(left.attributes, right.attributes);
        return (tag * 0.6) + (attrs * 0.4);
    }

    private static double pathSimilarity(Node stored, Node live) {
        List<String> storedPath = tagPath(stored);
        List<String> livePath = tagPath(live);
        if (storedPath.isEmpty() || livePath.isEmpty()) {
            return 0;
        }
        int lcsLength = longestCommonSubsequence(storedPath, livePath);
        int maxLen = Math.max(storedPath.size(), livePath.size());
        return maxLen == 0 ? 0 : (double) lcsLength / maxLen;
    }

    private static int longestCommonSubsequence(List<String> left, List<String> right) {
        int m = left.size();
        int n = right.size();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (equals(left.get(i - 1), right.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    private static List<String> tagPath(Node node) {
        List<String> path = new ArrayList<>();
        Node current = node;
        int depth = 0;
        while (current != null && depth < 50) {
            if (current.tag != null) {
                path.add(current.tag);
            }
            current = current.parent;
            depth++;
        }
        Collections.reverse(path);
        return path;
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static boolean equals(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object input) {
        return input instanceof Map<?, ?> ? (Map<String, Object>) input : new LinkedHashMap<>();
    }

    private static List<Object> list(Object input) {
        if (input instanceof List<?> items) {
            return new ArrayList<>(items);
        }
        return new ArrayList<>();
    }

    private static Map<String, String> stringMap(Object input) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map(input).entrySet()) {
            if (entry.getValue() != null) {
                out.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return out;
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String v = value.toString().trim();
        return v.isEmpty() ? null : v;
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static final class Node {
        private String tag;
        private Map<String, String> attributes = new LinkedHashMap<>();
        private String innerText;
        private List<Node> children = new ArrayList<>();
        private Node parent;
    }
}
