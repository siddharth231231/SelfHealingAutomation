package selfhealing.healing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import org.jsoup.Jsoup;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public final class LiveDomExtractor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int DOM_MAX_CHARS = 5000;

    private LiveDomExtractor() {
    }

    public static LiveDomData extract(WebDriver driver, LocatorMetaEntity stored) {
        LiveDomData live = new LiveDomData();
        if (driver == null || stored == null) {
            return live;
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> structural = parseJson(stored.getStructuralFingerprintJson());
        Map<String, Object> fingerprint = parseJson(stored.getElementFingerprintJson());

        List<Anchor> anchors = buildAnchors(stored, structural, fingerprint);
        AnchorResult best = null;
        for (Anchor anchor : anchors) {
            AnchorResult candidate = probeAnchor(js, anchor, stored.getElementTag());
            if (candidate != null && notBlank(candidate.rawDom())) {
                best = candidate;
                break;
            }
        }

        if (best == null) {
            best = probeAnchor(js, new Anchor("fallback_main_or_body", "fallback", null), stored.getElementTag());
        }
        if (best == null) {
            best = new AnchorResult("fallback_main_or_body", "", new ArrayList<>(), new LinkedHashMap<>());
        }

        String cleaned = cleanHtml(best.rawDom());
        String structuralJson = toJson(best.structural());
        live.setUsedAnchor(best.anchorName());
        live.setRawDom(best.rawDom());
        live.setCleanedDom(cleaned);
        live.setStructuralFingerprintJson(structuralJson);
        live.setLiveNeighborhoodHash(sha256(structuralJson));
        live.setCandidates(best.candidates());
        return live;
    }

    private static List<Anchor> buildAnchors(
            LocatorMetaEntity stored,
            Map<String, Object> structural,
            Map<String, Object> fingerprint) {
        List<Anchor> anchors = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        String dataTestId = firstNotBlank(
                stored.getDataTestId(),
                valueFromMap(fingerprint, "data-testid")
        );
        addAnchor(anchors, seen, "data_testid", "css", "[data-testid=\"" + escapeCss(dataTestId) + "\"]");

        addAnchor(anchors, seen, "stored.currentActiveLocator", "xpath", stored.getCurrentActiveLocator());
        addAnchor(anchors, seen, "stored.relativeXpath", "xpath", stored.getRelativeXpath());
        addAnchor(anchors, seen, "stored.absoluteXpath", "xpath", stored.getAbsoluteXpath());
        addAnchor(anchors, seen, "stored.cssSelector", "css", stored.getCssSelector());

        if (notBlank(stored.getParentTag()) && notBlank(stored.getParentId())) {
            addAnchor(anchors, seen, "parent_tag_id", "css", stored.getParentTag() + "#" + escapeCss(stored.getParentId()));
        }

        String semanticPath = firstNotBlank(
                stored.getSemanticPath(),
                valueFromMap(structural, "semanticPath")
        );
        addAnchor(anchors, seen, "semantic_path", "css", semanticPath);

        if (notBlank(stored.getParentTag()) && notBlank(stored.getParentClass())) {
            String firstClass = stored.getParentClass().trim().split("\\s+")[0];
            addAnchor(anchors, seen, "parent_tag_class", "css", stored.getParentTag() + "." + escapeCss(firstClass));
        }

        return anchors;
    }

    private static void addAnchor(
            List<Anchor> anchors,
            Set<String> seen,
            String name,
            String type,
            String value) {
        if (!notBlank(value)) {
            return;
        }
        String key = type + ":" + value;
        if (seen.add(key)) {
            anchors.add(new Anchor(name, type, value));
        }
    }

    private static AnchorResult probeAnchor(
            JavascriptExecutor js,
            Anchor anchor,
            String storedTag) {
        try {
            Object raw = js.executeScript(
                    anchorScript(),
                    anchor.type(),
                    anchor.value(),
                    DOM_MAX_CHARS,
                    storedTag
            );
            if (!(raw instanceof Map<?, ?> rawMap)) {
                return null;
            }

            String html = stringValue(rawMap.get("html"));
            List<LiveNodeCandidate> candidates = parseCandidates(rawMap.get("candidates"));
            Map<String, Object> structural = toStringKeyMap(rawMap.get("structural"));
            int candidateCount = candidates == null ? 0 : candidates.size();
            System.out.println("[SELF-HEALING] Anchor probe '" + anchor.name()
                    + "' type=" + anchor.type()
                    + " candidates=" + candidateCount
                    + " rootHtmlLen=" + (html == null ? 0 : html.length()));
            return new AnchorResult(anchor.name(), html, candidates, structural);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<LiveNodeCandidate> parseCandidates(Object rawCandidates) {
        List<LiveNodeCandidate> out = new ArrayList<>();
        if (!(rawCandidates instanceof List<?> list)) {
            return out;
        }
        for (Object candidate : list) {
            if (!(candidate instanceof Map<?, ?> rawMap)) {
                continue;
            }
            LiveNodeCandidate item = new LiveNodeCandidate();
            item.setXpath(stringValue(rawMap.get("xpath")));
            if (rawMap.get("nodePath") instanceof Map<?, ?> node) {
                Map<String, Object> nodeMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : node.entrySet()) {
                    if (entry.getKey() != null) {
                        nodeMap.put(entry.getKey().toString(), entry.getValue());
                    }
                }
                item.setNodePath(nodeMap);
            }
            if (notBlank(item.getXpath()) && !item.getNodePath().isEmpty()) {
                out.add(item);
            }
        }
        return out;
    }

    private static String cleanHtml(String rawDom) {
        if (!notBlank(rawDom)) {
            return "";
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(rawDom);
        doc.select("script,style").remove();
        String html = doc.body() != null ? doc.body().html() : doc.html();
        return html.length() > DOM_MAX_CHARS ? html.substring(0, DOM_MAX_CHARS) : html;
    }

    private static String anchorScript() {
        return """
                var type = arguments[0];
                var value = arguments[1];
                var maxChars = arguments[2] || 5000;
                function pickRoot() {
                  try {
                    if (type === 'css' && value) {
                      var cssNode = document.querySelector(value);
                      if (cssNode) { return cssNode; }
                    }
                    if (type === 'xpath' && value) {
                      var res = document.evaluate(value, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
                      if (res) {
                        var node = res.singleNodeValue;
                        if (node) { return node; }
                      }
                    }
                    if (type === 'fallback') {
                      return document.querySelector('main') || document.body || document.documentElement;
                    }
                  } catch (ignore) {}
                  return document.querySelector('main') || document.body || document.documentElement;
                }
                function attrs(node) {
                  var out = {};
                  if (!node || !node.attributes) { return out; }
                  for (var i = 0; i < node.attributes.length; i++) {
                    var a = node.attributes[i];
                    if (a && a.name) { out[a.name] = a.value; }
                  }
                  return out;
                }
                function xPath(node) {
                  if (!node || node.nodeType !== 1) { return null; }
                  var segments = [];
                  while (node && node.nodeType === 1 && segments.length < 30) {
                    var idx = 1;
                    var sib = node.previousElementSibling;
                    while (sib) {
                      if (sib.tagName === node.tagName) { idx++; }
                      sib = sib.previousElementSibling;
                    }
                    segments.unshift(node.tagName.toLowerCase() + '[' + idx + ']');
                    node = node.parentElement;
                  }
                  return '/' + segments.join('/');
                }
                function nodePath(node, depth, includeParent) {
                  if (!node || depth < 0) { return null; }
                  var info = {
                    tag: (node.tagName || '').toLowerCase(),
                    attributes: attrs(node),
                    innerText: (node.innerText || '').trim(),
                    children: []
                  };
                  if (depth > 0) {
                    var kids = Array.from(node.children || []).slice(0, 6);
                    for (var i = 0; i < kids.length; i++) {
                      info.children.push(nodePath(kids[i], depth - 1, false));
                    }
                  }
                  if (includeParent && depth > 0 && node.parentElement) {
                    info.parent = nodePath(node.parentElement, depth - 1, true);
                  }
                  return info;
                }
                var root = pickRoot();
                if (!root) { return { html: '', candidates: [] }; }
                var dom = root.outerHTML || '';
                if (dom.length > maxChars) { dom = dom.substring(0, maxChars); }
                function semanticPath(node) {
                  var out = [];
                  while (node && node.nodeType === 1 && out.length < 8) {
                    var part = node.tagName.toLowerCase();
                    if (node.id) { part += '#' + node.id; }
                    else if (node.className) {
                      var cls = ('' + node.className).trim().split(/\\s+/)[0];
                      if (cls) { part += '.' + cls; }
                    }
                    out.unshift(part);
                    node = node.parentElement;
                  }
                  return out.join(' > ');
                }
                function parentInfo(node) {
                  var out = [];
                  var p = node ? node.parentElement : null;
                  for (var i = 0; i < 3 && p; i++) {
                    out.push({
                      tag: (p.tagName || '').toLowerCase(),
                      id: p.id || null,
                      stableClass: p.className ? ('' + p.className).trim().split(/\\s+/)[0] : null
                    });
                    p = p.parentElement;
                  }
                  return out;
                }
                function siblingInfo(node) {
                  function toData(s) {
                    if (!s) { return null; }
                    return {
                      tag: (s.tagName || '').toLowerCase(),
                      text: (s.innerText || '').trim(),
                      href: s.getAttribute ? s.getAttribute('href') : null
                    };
                  }
                  return { prev: toData(node ? node.previousElementSibling : null), next: toData(node ? node.nextElementSibling : null) };
                }
                var selectors = [
                  'input',
                  'button',
                  'a',
                  'textarea',
                  'select',
                  'label',
                  '[role]',
                  '[data-testid]',
                  '[name]'
                ];
                var storedTag = (arguments[3] || '').trim().toLowerCase();
                if (storedTag && selectors.indexOf(storedTag) === -1) {
                  selectors.unshift(storedTag);
                }
                var css = selectors.join(',');
                var nodes = root.querySelectorAll(css);
                var candidates = [];
                for (var j = 0; j < nodes.length && candidates.length < 250; j++) {
                  var n = nodes[j];
                  candidates.push({ xpath: xPath(n), nodePath: nodePath(n, 3, true) });
                }
                var structural = {
                  semanticPath: semanticPath(root),
                  parents: parentInfo(root),
                  siblings: siblingInfo(root)
                };
                return { html: dom, candidates: candidates, structural: structural };
                """;
    }

    private static Map<String, Object> parseJson(String json) {
        if (!notBlank(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String valueFromMap(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String escapeCss(String text) {
        if (!notBlank(text)) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (notBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static Map<String, Object> toStringKeyMap(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                out.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return out;
    }

    private record Anchor(String name, String type, String value) {}

    private record AnchorResult(
            String anchorName,
            String rawDom,
            List<LiveNodeCandidate> candidates,
            Map<String, Object> structural) {}
}
