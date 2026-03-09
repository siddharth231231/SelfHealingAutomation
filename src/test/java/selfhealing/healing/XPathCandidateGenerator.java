package selfhealing.healing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates Healenium-style XPath candidates using multiple strategies.
 * Keeps logic lightweight and deterministic to stay close to EPAM behavior.
 */
public final class XPathCandidateGenerator {

    private XPathCandidateGenerator() {}

    public static List<String> generate(Map<String, Object> storedNodePath, Map<String, String> storedAttrs) {
        Set<String> out = new LinkedHashSet<>();
        String tag = val(storedNodePath, "tag");
        String text = val(storedNodePath, "innerText");
        Map<String, String> attrs = storedAttrs == null ? Map.of() : storedAttrs;

        // attribute based
        add(out, attrEquals(tag, "id", attrs.get("id")));
        add(out, attrEquals(tag, "name", attrs.get("name")));
        add(out, attrEquals(tag, "data-testid", attrs.get("data-testid")));
        add(out, attrEquals(tag, "aria-label", attrs.get("aria-label")));

        // contains based
        add(out, attrContains(tag, "id", attrs.get("id")));
        add(out, attrContains(tag, "class", attrs.get("class")));
        add(out, attrContains(tag, "aria-label", attrs.get("aria-label")));

        // text based
        if (notBlank(text)) {
            add(out, "//" + tag + "[text()='" + escape(text) + "']");
            add(out, "//" + tag + "[contains(normalize-space(text()), '" + escape(textFragment(text)) + "')]");
        }

        // parent based
        Map<String, Object> parent = map(storedNodePath.get("parent"));
        String parentTag = val(parent, "tag");
        String parentId = attr(parent, "id");
        String parentClass = attr(parent, "class");
        if (notBlank(parentTag)) {
            add(out, "//" + parentTag + "//" + tag);
            if (notBlank(parentId)) {
                add(out, "//" + parentTag + "[@id='" + escape(parentId) + "']//" + tag);
            }
            if (notBlank(parentClass)) {
                add(out, "//" + parentTag + "[contains(@class,'" + escape(firstClass(parentClass)) + "')]//" + tag);
            }
        }

        // sibling based
        Map<String, Object> siblings = map(parent.get("children"));
        // lightweight: rely on label-like precede/follow relation if text exists
        if (notBlank(text)) {
            add(out, "//label[normalize-space(text())='" + escape(text) + "']/following::" + tag + "[1]");
        }

        // fallback hierarchy reconstruction: tag path from nodePath
        List<String> path = new ArrayList<>();
        Map<String, Object> cursor = storedNodePath;
        int depth = 0;
        while (cursor != null && depth < 4) {
            String t = val(cursor, "tag");
            if (notBlank(t)) {
                path.add(0, t);
            }
            cursor = map(cursor.get("parent"));
            depth++;
        }
        if (!path.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String p : path) {
                sb.append("/").append(p);
            }
            add(out, sb.toString());
        }

        return new ArrayList<>(out);
    }

    private static void add(Set<String> out, String xpath) {
        if (notBlank(xpath)) {
            out.add(xpath.trim());
        }
    }

    private static String attrEquals(String tag, String attr, String value) {
        if (!notBlank(value)) return null;
        return "//" + tag + "[@" + attr + "='" + escape(value) + "']";
    }

    private static String attrContains(String tag, String attr, String value) {
        if (!notBlank(value)) return null;
        return "//" + tag + "[contains(@" + attr + ",'" + escape(valueFragment(value)) + "')]";
    }

    private static String escape(String value) { return value.replace("'", "\"" ); }

    private static String valueFragment(String v) {
        if (v == null) return "";
        return v.length() > 12 ? v.substring(0, 12) : v;
    }

    private static String textFragment(String v) {
        if (v == null) return "";
        return v.length() > 30 ? v.substring(0, 30) : v;
    }

    private static String val(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private static String attr(Map<String, Object> node, String attrName) {
        Map<String, Object> attrs = map(node.get("attributes"));
        Object v = attrs.get(attrName);
        return v == null ? null : v.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object o) {
        return o instanceof Map<?,?> m ? (Map<String, Object>) m : Map.of();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String firstClass(String classes) {
        return classes == null ? null : classes.trim().split("\\s+")[0];
    }
}
