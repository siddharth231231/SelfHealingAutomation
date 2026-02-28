package selfhealing.context.dom;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ElementSnapshotUtil {

    private ElementSnapshotUtil() {
    }

    public static LocatorMetaEntity buildEntity(
            WebDriver driver,
            WebElement element,
            String locatorName,
            String originalLocator) {

        LocatorMetaEntity entity = new LocatorMetaEntity();
        entity.setLocatorName(locatorName);
        entity.setOriginalLocator(originalLocator);
        entity.setPageUrl(safe(driver.getCurrentUrl()));
        entity.setPageTitle(safe(driver.getTitle()));

        JavascriptExecutor js = (JavascriptExecutor) driver;

        String tag = exec(js, "return arguments[0].tagName ? arguments[0].tagName.toLowerCase() : null;", element);
        String role = exec(js, "return arguments[0].getAttribute('role');", element);
        String type = exec(js, "return arguments[0].getAttribute('type');", element);
        String text = exec(js, "return (arguments[0].innerText || '').trim();", element);
        String id = exec(js, "return arguments[0].id || '';", element);
        String name = exec(js, "return arguments[0].getAttribute('name') || '';", element);
        String aria = exec(js, "return arguments[0].getAttribute('aria-label') || '';", element);
        String placeholder = exec(js, "return arguments[0].getAttribute('placeholder') || '';", element);

        entity.setElementTag(tag);
        entity.setElementRole(blankToNull(role));
        entity.setElementType(blankToNull(type));
        entity.setNormalizedVisibleText(normalizeText(text));

        String absoluteXpath = exec(js, buildXpathScript(), element);
        entity.setAbsoluteXpath(blankToNull(absoluteXpath));
        entity.setRelativeXpath(buildRelativeXpath(tag, id, name, text));
        entity.setCssSelector(buildCssSelector(tag, id, exec(js, "return arguments[0].className || '';", element)));

        if (absoluteXpath != null && absoluteXpath.contains("/")) {
            entity.setParentXpath(parentXpath(absoluteXpath));
            entity.setSiblingXpaths(parentXpath(absoluteXpath) + "/*");
            entity.setParentXpathChain(toJson(chainFromXpath(absoluteXpath)));
        }

        Map<String, String> attributes = execAttributes(js, element);
        Map<String, String> stable = new LinkedHashMap<>();
        Map<String, String> volatileAttrs = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (isVolatile(entry.getKey(), entry.getValue())) {
                volatileAttrs.put(entry.getKey(), entry.getValue());
            } else {
                stable.put(entry.getKey(), entry.getValue());
            }
        }
        entity.setStableAttributeJson(toJson(stable));
        entity.setVolatileAttributeJson(toJson(volatileAttrs));

        Map<String, String> anchor = new LinkedHashMap<>();
        anchor.put("tag", safe(tag));
        anchor.put("id", safe(id));
        anchor.put("name", safe(name));
        anchor.put("role", safe(role));
        anchor.put("aria-label", safe(aria));
        anchor.put("placeholder", safe(placeholder));
        entity.setAnchorHierarchyJson(toJson(anchor));

        String siblings = exec(js,
                "var el=arguments[0];" +
                        "if(!el||!el.parentElement) return '';" +
                        "var nodes=el.parentElement.children;" +
                        "var r=[];" +
                        "for(var i=0;i<nodes.length && i<10;i++){" +
                        " var n=nodes[i];" +
                        " r.push(n.tagName.toLowerCase());" +
                        "}" +
                        "return JSON.stringify(r);",
                element);
        entity.setSiblingSignatureJson(blankToNull(siblings));

        String dom = exec(js,
                "return document.documentElement ? document.documentElement.outerHTML : '';");
        entity.setDomSnapshot(dom);
        entity.setDomHash(hash(dom));

        return entity;
    }

    private static String buildXpathScript() {
        return "var el=arguments[0];" +
                "if(!el||el.nodeType!==1) return null;" +
                "function idx(n){" +
                " var i=1; var sib=n.previousElementSibling;" +
                " while(sib){ if(sib.tagName===n.tagName) i++; sib=sib.previousElementSibling; }" +
                " return i;" +
                "}" +
                "var seg=[]; var n=el;" +
                "while(n && n.nodeType===1 && seg.length<20){" +
                " seg.unshift(n.tagName.toLowerCase()+'['+idx(n)+']');" +
                " n=n.parentElement;" +
                "}" +
                "return '/' + seg.join('/');";
    }

    private static Map<String, String> execAttributes(JavascriptExecutor js, WebElement element) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            Object raw = js.executeScript(
                    "var el=arguments[0];" +
                            "var r={};" +
                            "if(!el||!el.attributes) return r;" +
                            "for(var i=0;i<el.attributes.length;i++){" +
                            " var a=el.attributes[i];" +
                            " if(a && a.name) r[a.name]=a.value;" +
                            "}" +
                            "return r;",
                    element);
            if (raw instanceof Map<?, ?> rawMap) {
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        map.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        } catch (Exception ignored) {
            // best-effort only
        }
        return map;
    }

    private static String buildRelativeXpath(String tag, String id, String name, String text) {
        if (!isBlank(id)) {
            return "//*[@id='" + escapeQuotes(id) + "']";
        }
        if (!isBlank(name)) {
            return "//*[@name='" + escapeQuotes(name) + "']";
        }
        if (!isBlank(text) && !isBlank(tag)) {
            return "//" + tag + "[normalize-space()='" + escapeQuotes(normalizeText(text)) + "']";
        }
        return null;
    }

    private static String buildCssSelector(String tag, String id, String className) {
        if (!isBlank(id)) {
            return "#" + id.trim();
        }
        if (!isBlank(tag) && !isBlank(className)) {
            String[] parts = className.trim().split("\\s+");
            StringBuilder sb = new StringBuilder(tag);
            for (String p : parts) {
                if (!isBlank(p) && sb.length() < 120) {
                    sb.append('.').append(p);
                }
            }
            return sb.toString();
        }
        return blankToNull(tag);
    }

    private static String parentXpath(String absoluteXpath) {
        int lastSlash = absoluteXpath.lastIndexOf('/');
        return lastSlash > 0 ? absoluteXpath.substring(0, lastSlash) : null;
    }

    private static Map<String, String> chainFromXpath(String absoluteXpath) {
        Map<String, String> chain = new LinkedHashMap<>();
        if (absoluteXpath == null) {
            return chain;
        }
        String[] parts = absoluteXpath.split("/");
        StringBuilder path = new StringBuilder();
        int idx = 0;
        for (String p : parts) {
            if (p == null || p.isEmpty()) {
                continue;
            }
            path.append('/').append(p);
            chain.put("level_" + idx, path.toString());
            idx++;
        }
        return chain;
    }

    private static boolean isVolatile(String name, String value) {
        if (isBlank(name) || isBlank(value)) {
            return false;
        }
        String lower = value.toLowerCase();
        if (name.startsWith("data-")) {
            return lower.matches(".*\\d{3,}.*");
        }
        if ("id".equalsIgnoreCase(name) || "class".equalsIgnoreCase(name)) {
            return lower.matches(".*\\d{3,}.*") || lower.length() > 64;
        }
        return false;
    }

    private static String toJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append('"').append(escapeQuotes(entry.getKey())).append('"')
                    .append(":")
                    .append('"').append(escapeQuotes(entry.getValue())).append('"');
        }
        sb.append("}");
        return sb.toString();
    }

    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input != null ? input.getBytes(StandardCharsets.UTF_8) : new byte[0]);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String exec(JavascriptExecutor js, String script, Object... args) {
        try {
            Object r = js.executeScript(script, args);
            return r != null ? r.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String t = text.replaceAll("\\s+", " ").trim();
        return t.isEmpty() ? null : t;
    }

    private static String escapeQuotes(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\"", "\\\"").replace("'", "\\'");
    }

    private static String safe(String input) {
        return input == null ? "" : input;
    }

    private static String blankToNull(String input) {
        return isBlank(input) ? null : input;
    }

    private static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
