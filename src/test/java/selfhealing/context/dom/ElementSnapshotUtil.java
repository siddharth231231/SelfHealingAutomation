package selfhealing.context.dom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.core.PageUrlUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

public final class ElementSnapshotUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> TIER1_ALLOWED = Set.of(
            "data-testid",
            "data-cy",
            "aria-label",
            "name",
            "type",
            "placeholder",
            "role",
            "href"
    );
    private static final Pattern DYNAMIC_NUMERIC_PATTERN =
            Pattern.compile(".*[0-9]{3,}.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern DYNAMIC_HEX_PATTERN =
            Pattern.compile(".*[a-f0-9]{6,}.*", Pattern.CASE_INSENSITIVE);

    private ElementSnapshotUtil() {
    }

    public static LocatorMetaEntity buildEntity(
            WebDriver driver,
            WebElement element,
            String locatorName,
            String originalLocator) {

        JavascriptExecutor js = (JavascriptExecutor) driver;
        LocatorMetaEntity entity = new LocatorMetaEntity();

        entity.setLocatorName(locatorName);
        entity.setOriginalLocator(originalLocator);
        entity.setPageUrl(PageUrlUtil.normalize(driver.getCurrentUrl()));
        entity.setPageTitle(safe(driver.getTitle()));

        String tag = lower(exec(js, "return arguments[0].tagName;", element));
        String text = normalizeText(exec(js, "return (arguments[0].innerText || '').trim();", element));
        Map<String, String> rawAttributes = extractAttributes(js, element);

        Map<String, String> tier1 = buildTier1Fingerprint(tag, text, rawAttributes);
        entity.setElementFingerprintJson(toJson(tier1));
        entity.setStableAttributeJson(toJson(tier1));
        entity.setVolatileAttributeJson(null);
        entity.setElementTag(tag);
        entity.setElementRole(blankToNull(tier1.get("role")));
        entity.setElementType(blankToNull(tier1.get("type")));
        entity.setNormalizedVisibleText(text);
        entity.setDataTestId(blankToNull(tier1.get("data-testid")));

        String absoluteXpath = exec(js, absoluteXpathScript(), element);
        entity.setAbsoluteXpath(blankToNull(absoluteXpath));
        entity.setRelativeXpath(buildRelativeXpath(tag, text, tier1, rawAttributes));
        entity.setCssSelector(buildCssSelector(tier1, rawAttributes, tag));

        Map<String, Object> tier2 = buildStructuralFingerprint(js, element, rawAttributes);
        entity.setStructuralFingerprintJson(toJson(tier2));
        entity.setAnchorHierarchyJson(toJson(tier2));
        entity.setSiblingSignatureJson(toJson(tier2.get("siblings")));
        entity.setSemanticPath(stringValue(tier2.get("semanticPath")));
        Map<String, Object> parent = castMap(tier2.get("primaryParent"));
        entity.setParentTag(stringValue(parent.get("tag")));
        entity.setParentId(stringValue(parent.get("id")));
        entity.setParentClass(stringValue(parent.get("stableClass")));
        entity.setParentXpath(parentXpath(absoluteXpath));
        entity.setSiblingXpaths(siblingXpath(parentXpath(absoluteXpath)));
        entity.setParentXpathChain(toJson(castList(tier2.get("parents"))));
        entity.setSiblingXpathCluster(toJson(castMap(tier2.get("siblings"))));

        Map<String, Object> nodePath = buildNodePath(js, element);
        entity.setNodePathJson(toJson(nodePath));
        entity.setDomSnapshot(null);
        entity.setDomHash(hash(entity.getStructuralFingerprintJson()));

        return entity;
    }

    private static Map<String, String> buildTier1Fingerprint(
            String tag,
            String text,
            Map<String, String> rawAttributes) {
        Map<String, String> tier1 = new LinkedHashMap<>();
        putIfStable(tier1, "tag", tag);
        putIfStable(tier1, "text", text);
        for (String attrName : TIER1_ALLOWED) {
            putIfStable(tier1, attrName, rawAttributes.get(attrName));
        }
        return tier1;
    }

    private static Map<String, Object> buildStructuralFingerprint(
            JavascriptExecutor js,
            WebElement element,
            Map<String, String> rawAttributes) {
        Map<String, Object> structural = new LinkedHashMap<>();

        String semanticPath = exec(js, semanticPathScript(), element);
        structural.put("semanticPath", blankToNull(semanticPath));

        Object parentRaw = js.executeScript(parentInfoScript(), element);
        List<Map<String, Object>> parents = castMapList(parentRaw);
        for (Map<String, Object> parent : parents) {
            parent.put("stableClass", stableClass(stringValue(parent.get("class"))));
        }
        structural.put("parents", parents);
        structural.put("primaryParent", parents.isEmpty() ? new LinkedHashMap<>() : parents.get(0));

        Object siblingsRaw = js.executeScript(siblingInfoScript(), element);
        Map<String, Object> siblings = castMap(siblingsRaw);
        structural.put("siblings", sanitizeSiblingInfo(siblings));

        if (rawAttributes.containsKey("data-testid")) {
            structural.put("dataTestId", blankToNull(rawAttributes.get("data-testid")));
        }
        return structural;
    }

    private static Map<String, Object> sanitizeSiblingInfo(Map<String, Object> siblings) {
        Map<String, Object> clean = new LinkedHashMap<>();
        clean.put("prev", sanitizeSibling(castMap(siblings.get("prev"))));
        clean.put("next", sanitizeSibling(castMap(siblings.get("next"))));
        return clean;
    }

    private static Map<String, Object> sanitizeSibling(Map<String, Object> sibling) {
        Map<String, Object> clean = new LinkedHashMap<>();
        clean.put("tag", stringValue(sibling.get("tag")));
        clean.put("text", normalizeText(stringValue(sibling.get("text"))));
        clean.put("href", stringValue(sibling.get("href")));
        return clean;
    }

    private static Map<String, Object> buildNodePath(JavascriptExecutor js, WebElement element) {
        Object raw = js.executeScript(nodePathScript(), element);
        return sanitizeNode(castMap(raw), 3);
    }

    private static Map<String, Object> sanitizeNode(Map<String, Object> node, int depth) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tag", lower(stringValue(node.get("tag"))));
        out.put("attributes", filterStableAttributes(castStringMap(node.get("attributes"))));
        out.put("innerText", normalizeText(stringValue(node.get("innerText"))));

        List<Object> childrenRaw = castList(node.get("children"));
        List<Map<String, Object>> children = new ArrayList<>();
        if (depth > 0) {
            for (Object child : childrenRaw) {
                children.add(sanitizeNode(castMap(child), depth - 1));
            }
        }
        out.put("children", children);

        if (depth > 0 && node.get("parent") != null) {
            out.put("parent", sanitizeNode(castMap(node.get("parent")), depth - 1));
        }
        return out;
    }

    private static Map<String, String> filterStableAttributes(Map<String, String> attributes) {
        Map<String, String> stable = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (isStableAttribute(key, value)) {
                if ("class".equalsIgnoreCase(key)) {
                    value = stableClass(value);
                }
                if (!isBlank(value)) {
                    stable.put(key, value.trim());
                }
            }
        }
        return stable;
    }

    private static String buildRelativeXpath(
            String tag,
            String text,
            Map<String, String> tier1,
            Map<String, String> rawAttributes) {
        String dataTestId = tier1.get("data-testid");
        if (!isBlank(dataTestId)) {
            return "//*[@data-testid='" + escape(dataTestId) + "']";
        }
        String dataCy = tier1.get("data-cy");
        if (!isBlank(dataCy)) {
            return "//*[@data-cy='" + escape(dataCy) + "']";
        }
        String name = tier1.get("name");
        if (!isBlank(name)) {
            return "//*[@name='" + escape(name) + "']";
        }
        String id = rawAttributes.get("id");
        if (isStableAttribute("id", id)) {
            return "//*[@id='" + escape(id) + "']";
        }
        if (!isBlank(tag) && !isBlank(text)) {
            return "//" + tag + "[normalize-space()='" + escape(text) + "']";
        }
        return null;
    }

    private static String buildCssSelector(Map<String, String> tier1, Map<String, String> rawAttributes, String tag) {
        if (!isBlank(tier1.get("data-testid"))) {
            return "[data-testid='" + escape(tier1.get("data-testid")) + "']";
        }
        if (!isBlank(tier1.get("data-cy"))) {
            return "[data-cy='" + escape(tier1.get("data-cy")) + "']";
        }
        String id = rawAttributes.get("id");
        if (isStableAttribute("id", id)) {
            return "#" + id.trim();
        }
        String stableClass = stableClass(rawAttributes.get("class"));
        if (!isBlank(tag) && !isBlank(stableClass)) {
            return tag + "." + stableClass.split("\\s+")[0];
        }
        return blankToNull(tag);
    }

    private static Map<String, String> extractAttributes(JavascriptExecutor js, WebElement element) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            Object raw = js.executeScript(
                    "var el=arguments[0];" +
                            "var out={};" +
                            "if(!el||!el.attributes){return out;}" +
                            "for(var i=0;i<el.attributes.length;i++){" +
                            " var a=el.attributes[i];" +
                            " if(a && a.name){out[a.name]=a.value;}" +
                            "}" +
                            "return out;",
                    element
            );
            if (raw instanceof Map<?, ?> rawMap) {
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() != null) {
                        map.put(entry.getKey().toString(), entry.getValue() == null ? null : entry.getValue().toString());
                    }
                }
            }
        } catch (Exception ignored) {
            // capture is best effort and must never fail test flow.
        }
        return map;
    }

    private static String semanticPathScript() {
        return "var el=arguments[0];" +
                "if(!el) return null;" +
                "var out=[];" +
                "while(el && el.nodeType===1 && out.length<8){" +
                " var t=(el.tagName||'').toLowerCase();" +
                " var p=t;" +
                " if(el.id){p+='#'+el.id;}" +
                " else if(el.className){var c=(''+el.className).trim().split(/\\s+/)[0]; if(c){p+='.'+c;}}" +
                " out.unshift(p);" +
                " el=el.parentElement;" +
                "}" +
                "return out.join(' > ');";
    }

    private static String parentInfoScript() {
        return "var el=arguments[0];" +
                "var out=[];" +
                "var p=el?el.parentElement:null;" +
                "for(var i=0;i<3 && p;i++){" +
                " out.push({" +
                "   tag:(p.tagName||'').toLowerCase()," +
                "   id:p.id||null," +
                "   class:p.className||null" +
                " });" +
                " p=p.parentElement;" +
                "}" +
                "return out;";
    }

    private static String siblingInfoScript() {
        return "var el=arguments[0];" +
                "function toData(n){" +
                " if(!n) return null;" +
                " return {" +
                "   tag:(n.tagName||'').toLowerCase()," +
                "   text:(n.innerText||'').trim()," +
                "   href:n.getAttribute?n.getAttribute('href'):null" +
                " };" +
                "}" +
                "return {prev:toData(el?el.previousElementSibling:null),next:toData(el?el.nextElementSibling:null)};";
    }

    private static String nodePathScript() {
        return "var root=arguments[0];" +
                "function attrs(node){" +
                " var out={};" +
                " if(!node||!node.attributes) return out;" +
                " for(var i=0;i<node.attributes.length;i++){" +
                "  var a=node.attributes[i]; if(a&&a.name){out[a.name]=a.value;}" +
                " }" +
                " return out;" +
                "}" +
                "function build(node, depth, includeParent){" +
                " if(!node || depth<0){return null;}" +
                " var item={" +
                "   tag:(node.tagName||'').toLowerCase()," +
                "   attributes:attrs(node)," +
                "   innerText:(node.innerText||'').trim()," +
                "   children:[]" +
                " };" +
                " if(depth>0){" +
                "  var kids=Array.from(node.children||[]).slice(0,6);" +
                "  for(var i=0;i<kids.length;i++){item.children.push(build(kids[i], depth-1, false));}" +
                " }" +
                " if(includeParent && depth>0 && node.parentElement){" +
                "   item.parent=build(node.parentElement, depth-1, true);" +
                " }" +
                " return item;" +
                "}" +
                "return build(root,3,true);";
    }

    private static String absoluteXpathScript() {
        return "var el=arguments[0];" +
                "if(!el||el.nodeType!==1)return null;" +
                "function idx(n){var i=1;var sib=n.previousElementSibling;while(sib){if(sib.tagName===n.tagName)i++;sib=sib.previousElementSibling;}return i;}" +
                "var seg=[];var n=el;" +
                "while(n&&n.nodeType===1&&seg.length<30){seg.unshift((n.tagName||'').toLowerCase()+'['+idx(n)+']');n=n.parentElement;}" +
                "return '/'+seg.join('/');";
    }

    private static String parentXpath(String absoluteXpath) {
        if (isBlank(absoluteXpath)) {
            return null;
        }
        int idx = absoluteXpath.lastIndexOf('/');
        return idx > 0 ? absoluteXpath.substring(0, idx) : null;
    }

    private static String siblingXpath(String parentXpath) {
        return isBlank(parentXpath) ? null : parentXpath + "/*";
    }

    private static boolean isStableAttribute(String key, String value) {
        if (isBlank(key) || isBlank(value)) {
            return false;
        }
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        if ("style".equals(normalizedKey) || normalizedKey.startsWith("on")) {
            return false;
        }
        if ("class".equals(normalizedKey)) {
            return !isBlank(stableClass(value));
        }
        return !isDynamic(value);
    }

    private static boolean isDynamic(String value) {
        if (isBlank(value)) {
            return true;
        }
        String v = value.trim();
        return DYNAMIC_NUMERIC_PATTERN.matcher(v).matches()
                || DYNAMIC_HEX_PATTERN.matcher(v).matches();
    }

    private static String stableClass(String classValue) {
        if (isBlank(classValue)) {
            return null;
        }
        String[] classes = classValue.trim().split("\\s+");
        List<String> stable = new ArrayList<>();
        for (String token : classes) {
            if (!isDynamic(token)) {
                stable.add(token);
            }
            if (stable.size() == 2) {
                break;
            }
        }
        return stable.isEmpty() ? null : String.join(" ", stable);
    }

    private static void putIfStable(Map<String, String> target, String key, String value) {
        if (!isBlank(value) && !isDynamic(value)) {
            target.put(key, value.trim());
        }
    }

    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input == null ? new byte[0] : input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static String exec(JavascriptExecutor js, String script, Object... args) {
        try {
            Object result = js.executeScript(script, args);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Object> castMap(Object input) {
        if (input instanceof Map<?, ?> raw) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                if (entry.getKey() != null) {
                    out.put(entry.getKey().toString(), entry.getValue());
                }
            }
            return out;
        }
        return new LinkedHashMap<>();
    }

    private static Map<String, String> castStringMap(Object input) {
        Map<String, Object> generic = castMap(input);
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : generic.entrySet()) {
            out.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().toString());
        }
        return out;
    }

    private static List<Object> castList(Object input) {
        if (input instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        return new ArrayList<>();
    }

    private static List<Map<String, Object>> castMapList(Object input) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : castList(input)) {
            out.add(castMap(item));
        }
        return out;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String asString = value.toString().trim();
        return asString.isEmpty() ? null : asString;
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String escape(String input) {
        return input == null ? "" : input.replace("'", "\\'");
    }

    private static String blankToNull(String input) {
        return isBlank(input) ? null : input.trim();
    }

    private static String safe(String input) {
        return input == null ? "" : input;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
