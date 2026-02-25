package selfhealing.context.dom;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import selfhealing.context.storedContext.DbExtractedData;

import java.security.MessageDigest;
import java.util.*;

public class DomCaptureUtil {

        public static DomContext capture(
                        WebDriver driver,
                        DbExtractedData stored,
                        Throwable exception) {

                DomContext ctx = new DomContext();

                if (driver == null)
                        return ctx;

                JavascriptExecutor js = (JavascriptExecutor) driver;

                /*
                 * =====================================================
                 * 1️⃣ Failure & Page Info
                 * =====================================================
                 */
                ctx.setFailureType(exception != null
                                ? exception.getClass().getSimpleName()
                                : "UNKNOWN");

                ctx.setPageUrl(safe(driver.getCurrentUrl()));
                ctx.setPageTitle(safe(driver.getTitle()));

                /*
                 * =====================================================
                 * 2️⃣ Page Level Intelligence
                 * =====================================================
                 */

                String fullDom = exec(js,
                                "return document.documentElement ? document.documentElement.outerHTML : '';");
                ctx.setFullDomLength(fullDom != null ? fullDom.length() : 0);

                String currentHash = hash(fullDom);
                ctx.setCurrentDomHash(currentHash);

                String bodyText = exec(js,
                                "var t=document.body?document.body.innerText:'';" +
                                                "return t.length>1000?t.substring(0,1000):t;");
                ctx.setBodyTextSnippet(bodyText);

                // If caller didn't pass stored context, try global one
                if (stored == null) {
                        stored = DbExtractedData.get();
                }

                /*
                 * =====================================================
                 * 3️⃣ If No Historical Metadata → Lightweight Mode
                 * =====================================================
                 */
                if (stored == null) {
                        return ctx;
                }

                /*
                 * =====================================================
                 * 4️⃣ DOM Drift Detection
                 * =====================================================
                 */
                ctx.setSamePage(
                                stored.getPageUrl() != null &&
                                                ctx.getPageUrl().contains(stored.getPageUrl()));

                ctx.setDomChanged(
                                stored.getDomHash() != null &&
                                                !stored.getDomHash().equals(currentHash));

                /*
                 * =====================================================
                 * 5️⃣ Validate Stored Locators
                 * =====================================================
                 */
                ctx.setRelativeXpathValid(
                                xpathExists(js, stored.getRelativeXpath()));

                ctx.setAbsoluteXpathValid(
                                xpathExists(js, stored.getAbsoluteXpath()));

                ctx.setCssSelectorValid(
                                cssExists(js, stored.getCssSelector()));

                /*
                 * =====================================================
                 * 5️⃣.b Element-level interaction using stored locator
                 * =====================================================
                 */
                String primaryXpath = firstNotBlank(
                                stored.getRelativeXpath(),
                                stored.getAbsoluteXpath());

                if (notBlank(primaryXpath)) {

                        boolean elementExists = xpathExists(js, primaryXpath);
                        ctx.setElementFound(elementExists);

                        if (elementExists) {
                                // Basic geometry
                                String coordinates = exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "if(!el) return null;" +
                                                                "var r=el.getBoundingClientRect();" +
                                                                "return r.x + ',' + r.y;",
                                                primaryXpath);
                                ctx.setElementCoordinates(coordinates);

                                String size = exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "if(!el) return null;" +
                                                                "var r=el.getBoundingClientRect();" +
                                                                "return r.width + ',' + r.height;",
                                                primaryXpath);
                                ctx.setElementSize(size);

                                String visible = exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "if(!el) return 'false';" +
                                                                "var style=window.getComputedStyle(el);" +
                                                                "var vis=style && style.display!=='none' && " +
                                                                "         style.visibility!=='hidden' && " +
                                                                "         el.offsetWidth>0 && el.offsetHeight>0;" +
                                                                "return vis.toString();",
                                                primaryXpath);
                                ctx.setElementVisible(parseBoolean(visible));

                                String enabled = exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "if(!el) return 'false';" +
                                                                "return (!el.disabled).toString();",
                                                primaryXpath);
                                ctx.setElementEnabled(parseBoolean(enabled));

                                // Live attribute snapshot
                                Map<String, String> live = new HashMap<>();
                                putIfNotBlank(live, "id", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el?el.id:null;",
                                                primaryXpath));
                                putIfNotBlank(live, "name", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el?el.name:null;",
                                                primaryXpath));
                                putIfNotBlank(live, "type", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el&&el.type?el.type:null;",
                                                primaryXpath));
                                putIfNotBlank(live, "class", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el&&el.className?el.className:null;",
                                                primaryXpath));
                                putIfNotBlank(live, "value", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el&&('value' in el)?el.value:null;",
                                                primaryXpath));
                                putIfNotBlank(live, "aria-label", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el?el.getAttribute('aria-label'):null;",
                                                primaryXpath));
                                putIfNotBlank(live, "role", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el?el.getAttribute('role'):null;",
                                                primaryXpath));
                                putIfNotBlank(live, "text", exec(js,
                                                "var el=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "return el&&el.innerText?el.innerText:null;",
                                                primaryXpath));

                                if (!live.isEmpty()) {
                                        ctx.setLiveAttributes(live);
                                }
                        }
                }

                /*
                 * =====================================================
                 * 6️⃣ Parent Structural Analysis
                 * Priority: chain (absolute) → parentXpath (relative) → JS proximity fallback
                 * =====================================================
                 */
                String parentXpathCandidate = null;

                // 1st choice: last absolute xpath extracted from the stored chain
                String rawParentChain = stored.getParentXpathChain();
                if (notBlank(rawParentChain)) {
                        String fromChain = extractXpathFromChain(rawParentChain);
                        if (notBlank(fromChain) && xpathExists(js, fromChain)) {
                                parentXpathCandidate = fromChain;
                        }
                }

                // 2nd choice: stored relative parentXpath (more resilient to DOM shifts)
                if (!notBlank(parentXpathCandidate) && xpathExists(js, stored.getParentXpath())) {
                        parentXpathCandidate = stored.getParentXpath();
                }

                if (notBlank(parentXpathCandidate)) {
                        ctx.setParentStillExists(true);

                        ctx.setSemanticParentHtml(exec(js,
                                        "var el=document.evaluate(arguments[0],document,null," +
                                                        "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;" +
                                                        "return el?el.outerHTML:null;",
                                        parentXpathCandidate));

                        String childCount = exec(js,
                                        "var el=document.evaluate(arguments[0],document,null," +
                                                        "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;" +
                                                        "return el?String(el.children.length):'0';",
                                        parentXpathCandidate);
                        ctx.setParentChildCount(parseInt(childCount));

                        if (notBlank(primaryXpath)) {
                                String indexStr = exec(js,
                                                "var parent=document.evaluate(arguments[0],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "var el=document.evaluate(arguments[1],document,null," +
                                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;"
                                                                +
                                                                "if(!parent||!el||!parent.children) return '-1';" +
                                                                "for(var i=0;i<parent.children.length;i++){" +
                                                                " if(parent.children[i]===el) return String(i);" +
                                                                "}" +
                                                                "return '-1';",
                                                parentXpathCandidate, primaryXpath);
                                int idx = parseInt(indexStr);
                                if (idx >= 0)
                                        ctx.setElementIndexInsideParent(idx);
                        }
                } else {
                        // 3rd choice: JS proximity – smart 4-step strategy to capture the meaningful
                        // container
                        ctx.setParentStillExists(false);
                        String fallbackParent = exec(js,
                                        // Step 1: try any <form> on the page
                                        "var form=document.querySelector('form');" +
                                                        "if(form) return form.outerHTML;" +
                                                        // Step 2: find a submit/clickable button and walk to its
                                                        // ancestor
                                                        "var btn=document.querySelector(" +
                                                        "  'button[type=\"submit\"],button[type=\"button\"],input[type=\"submit\"],button');"
                                                        +
                                                        "if(btn){" +
                                                        "  var c=btn.closest('form,section,div[class],main,article')||btn.parentElement;"
                                                        +
                                                        "  if(c) return c.outerHTML;" +
                                                        "}" +
                                                        // Step 3: body with script/style stripped
                                                        "try{" +
                                                        "  var clone=document.body.cloneNode(true);" +
                                                        "  clone.querySelectorAll('script,style,link,noscript').forEach(function(n){n.remove();});"
                                                        +
                                                        "  var html=clone.innerHTML.trim();" +
                                                        "  return html.substring(0,4000);" +
                                                        "}catch(e){}" +
                                                        // Step 4: just visible body text
                                                        "return document.body?document.body.innerText.substring(0,2000):null;");
                        if (notBlank(fallbackParent)) {
                                ctx.setSemanticParentHtml(fallbackParent);
                        }
                }

                /*
                 * =====================================================
                 * 7️⃣ Sibling Cluster Intelligence
                 * Priority: siblingXpathCluster (plain xpath) → siblingXpaths → JS same-tag
                 * fallback
                 * =====================================================
                 */
                String siblingClusterCandidate = null;

                // 1st choice: cluster xpath only if it's a plain XPath (not a JSON object) and
                // exists
                String rawCluster = stored.getSiblingXpathCluster();
                if (notBlank(rawCluster) && !rawCluster.trim().startsWith("{") && xpathExists(js, rawCluster.trim())) {
                        siblingClusterCandidate = rawCluster.trim();
                }

                // 2nd choice: individual sibling xpaths
                if (!notBlank(siblingClusterCandidate) && xpathExists(js, stored.getSiblingXpaths())) {
                        siblingClusterCandidate = stored.getSiblingXpaths();
                }

                if (notBlank(siblingClusterCandidate)) {
                        ctx.setSiblingClusterStillExists(true);

                        List<Object> siblings = execList(js,
                                        "var r=[];" +
                                                        "var snap=document.evaluate(arguments[0],document,null," +
                                                        "XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,null);" +
                                                        "for(var i=0;i<snap.snapshotLength && i<20;i++){" +
                                                        " r.push(snap.snapshotItem(i).outerHTML);" +
                                                        "}" +
                                                        "return r;",
                                        siblingClusterCandidate);
                        ctx.setSiblingElements(castToStringList(siblings));
                } else {
                        // 3rd choice: JS fallback — visible interactive elements on the page
                        ctx.setSiblingClusterStillExists(false);
                        List<Object> fallbackSiblings = execList(js,
                                        "var r=[];" +
                                                        "var nodes=document.querySelectorAll('input,button,a,label,select,textarea');"
                                                        +
                                                        "for(var i=0;i<nodes.length && r.length<20;i++){" +
                                                        " if(nodes[i].offsetWidth>0) r.push(nodes[i].outerHTML);" +
                                                        "}" +
                                                        "return r;");
                        ctx.setSiblingElements(castToStringList(fallbackSiblings));
                }

                /*
                 * =====================================================
                 * 8️⃣ Controlled Candidate Scan (NOT full '*')
                 * =====================================================
                 */
                List<Object> candidates = execList(js,
                                "var r=[];" +
                                                "var nodes=document.querySelectorAll('a,button,input,label,span,div');"
                                                +
                                                "for(var i=0;i<nodes.length && i<200;i++){" +
                                                " var e=nodes[i];" +
                                                " if(e.innerText && e.innerText.length<50){" +
                                                "   r.push({" +
                                                "     tag:e.tagName," +
                                                "     text:e.innerText," +
                                                "     role:e.getAttribute('role')," +
                                                "     aria:e.getAttribute('aria-label')" +
                                                "   });" +
                                                " }" +
                                                "}" +
                                                "return r;");

                ctx.setCandidateElements(castToMapList(candidates));

                /*
                 * =====================================================
                 * 9️⃣ Layout Intelligence
                 * =====================================================
                 */
                ctx.setViewportSize(exec(js,
                                "return window.innerWidth + 'x' + window.innerHeight;"));

                ctx.setScrollPosition(exec(js,
                                "return window.scrollX + ',' + window.scrollY;"));

                return ctx;
        }

        /*
         * =====================================================
         * Helper Methods
         * =====================================================
         */

        private static boolean xpathExists(JavascriptExecutor js, String xpath) {
                if (!notBlank(xpath))
                        return false;

                Object result = js.executeScript(
                                "return !!document.evaluate(arguments[0],document,null," +
                                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;",
                                xpath);

                return Boolean.TRUE.equals(result);
        }

        private static boolean cssExists(JavascriptExecutor js, String css) {
                if (!notBlank(css))
                        return false;

                Object result = js.executeScript(
                                "return document.querySelector(arguments[0])!=null;",
                                css);

                return Boolean.TRUE.equals(result);
        }

        private static String exec(JavascriptExecutor js, String script, Object... args) {
                try {
                        Object r = js.executeScript(script, args);
                        return r != null ? r.toString() : null;
                } catch (Exception e) {
                        return null;
                }
        }

        @SuppressWarnings("unchecked")
        private static List<Object> execList(JavascriptExecutor js, String script, Object... args) {
                try {
                        Object r = js.executeScript(script, args);
                        return r instanceof List ? (List<Object>) r : new ArrayList<>();
                } catch (Exception e) {
                        return new ArrayList<>();
                }
        }

        private static String hash(String input) {
                try {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        byte[] digest = md.digest(
                                        input != null ? input.getBytes() : new byte[0]);

                        StringBuilder sb = new StringBuilder();
                        for (byte b : digest)
                                sb.append(String.format("%02x", b));

                        return sb.toString();

                } catch (Exception e) {
                        return null;
                }
        }

        private static String firstNotBlank(String... values) {
                if (values == null)
                        return null;
                for (String v : values) {
                        if (notBlank(v))
                                return v;
                }
                return null;
        }

        /**
         * parentXpathChain is stored as a JSON-like array, e.g.:
         * ["/html/body/.../form", "/html/body/.../form/div[3]"]
         * This method extracts the last non-blank XPath from it.
         * If the value is already a plain XPath (not starting with '['), it is returned
         * as-is.
         */
        private static String extractXpathFromChain(String chain) {
                if (!notBlank(chain))
                        return null;
                String trimmed = chain.trim();
                if (!trimmed.startsWith("[")) {
                        return trimmed; // already a plain XPath
                }
                try {
                        // Strip surrounding [ ]
                        trimmed = trimmed.substring(1);
                        if (trimmed.endsWith("]")) {
                                trimmed = trimmed.substring(0, trimmed.length() - 1);
                        }
                        // Split by comma — each part is a quoted XPath entry
                        String[] parts = trimmed.split(",");
                        String candidate = null;
                        for (String part : parts) {
                                String p = part.trim();
                                if (p.startsWith("\""))
                                        p = p.substring(1);
                                if (p.endsWith("\""))
                                        p = p.substring(0, p.length() - 1);
                                if (notBlank(p))
                                        candidate = p;
                        }
                        return candidate;
                } catch (Exception e) {
                        return null;
                }
        }

        private static boolean notBlank(String s) {
                return s != null && !s.trim().isEmpty();
        }

        private static String safe(String s) {
                return s != null ? s : "";
        }

        private static Integer parseInt(String s) {
                try {
                        return Integer.parseInt(s);
                } catch (Exception e) {
                        return 0;
                }
        }

        private static Boolean parseBoolean(String s) {
                if (s == null)
                        return null;
                if ("true".equalsIgnoreCase(s.trim()))
                        return Boolean.TRUE;
                if ("false".equalsIgnoreCase(s.trim()))
                        return Boolean.FALSE;
                return null;
        }

        private static List<String> castToStringList(List<Object> list) {
                List<String> result = new ArrayList<>();
                for (Object o : list) {
                        if (o != null)
                                result.add(o.toString());
                }
                return result;
        }

        @SuppressWarnings("unchecked")
        private static List<Map<String, Object>> castToMapList(List<Object> list) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Object o : list) {
                        if (o instanceof Map) {
                                result.add((Map<String, Object>) o);
                        }
                }
                return result;
        }

        private static void putIfNotBlank(Map<String, String> map, String key, String value) {
                if (map == null || key == null)
                        return;
                if (value == null)
                        return;
                String trimmed = value.trim();
                if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed))
                        return;
                map.put(key, trimmed);
        }
}