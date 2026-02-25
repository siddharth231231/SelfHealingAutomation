package selfhealing.dom;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import selfhealing.context.storedContext.DbExtractedData;

import java.util.ArrayList;
import java.util.List;

public class DomCaptureUtil {

    public static DomContext capture(
            WebDriver driver,
            String brokenXpath,
            String expectedTag,
            String expectedText,
            Throwable exception
    ) {

        DomContext ctx = new DomContext();

        ctx.setBrokenXpath(brokenXpath);
        ctx.setExpectedTag(expectedTag);
        ctx.setExpectedText(expectedText);
        ctx.setFailureType(exception.getClass().getSimpleName());

        ctx.setPageUrl(driver.getCurrentUrl());
        ctx.setPageTitle(driver.getTitle());
        ctx.setPageRole("NAVIGATION"); // heuristic for now

        JavascriptExecutor js = (JavascriptExecutor) driver;

        boolean found = Boolean.parseBoolean(exec(js,
                "return !!document.evaluate(arguments[0],document,null," +
                        "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;",
                brokenXpath));

        ctx.setElementFound(found);

        DbExtractedData stored = DbExtractedData.get();

        // If the broken xpath fails, try using stored locators to find the live element.
        // This lets us capture current parent/sibling HTML even when the test locator is broken.
        boolean liveElementFound = false;
        String liveElementXpath = null;
        String liveElementCss = null;
        if (stored != null) {
            liveElementXpath = firstNotBlank(
                    stored.getCurrentActiveLocator(),
                    stored.getRelativeXpath(),
                    stored.getAbsoluteXpath()
            );
            liveElementCss = stored.getCssSelector();
        }

        if (!found) {
            if (notBlank(liveElementXpath) && xpathExists(js, liveElementXpath)) {
                liveElementFound = true;
                ctx.setElementFound(true);
            } else if (notBlank(liveElementCss) && cssExists(js, liveElementCss)) {
                liveElementFound = true;
                ctx.setElementFound(true);
            }
        } else {
            // broken xpath actually found an element
            liveElementFound = true;
            liveElementXpath = brokenXpath;
        }

        // If we found the element (via broken or stored locator), capture its parent + siblings directly.
        if (liveElementFound) {
            String parentHtml = null;
            if (notBlank(liveElementXpath)) {
                parentHtml = exec(js,
                        "var el=document.evaluate(arguments[0],document,null," +
                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;" +
                                "if(!el) return null;" +
                                "var p=el.closest('form,div,section,article,main') || el.parentElement;" +
                                "return p?p.outerHTML:null;",
                        liveElementXpath);
            } else if (notBlank(liveElementCss)) {
                parentHtml = exec(js,
                        "var el=document.querySelector(arguments[0]);" +
                                "if(!el) return null;" +
                                "var p=el.closest('form,div,section,article,main') || el.parentElement;" +
                                "return p?p.outerHTML:null;",
                        liveElementCss);
            }
            if (notBlank(parentHtml)) {
                ctx.setSemanticParentHtml(parentHtml);
            }

            List<String> siblingHtml = new ArrayList<>();
            if (notBlank(liveElementXpath)) {
                siblingHtml = execList(js,
                        "var out=[];" +
                                "var el=document.evaluate(arguments[0],document,null," +
                                "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;" +
                                "if(!el || !el.parentElement) return out;" +
                                "var kids=Array.from(el.parentElement.children);" +
                                "for(var i=0;i<kids.length && out.length<20;i++){" +
                                " out.push(kids[i].outerHTML);" +
                                "}" +
                                "return out;",
                        liveElementXpath);
            } else if (notBlank(liveElementCss)) {
                siblingHtml = execList(js,
                        "var out=[];" +
                                "var el=document.querySelector(arguments[0]);" +
                                "if(!el || !el.parentElement) return out;" +
                                "var kids=Array.from(el.parentElement.children);" +
                                "for(var i=0;i<kids.length && out.length<20;i++){" +
                                " out.push(kids[i].outerHTML);" +
                                "}" +
                                "return out;",
                        liveElementCss);
            }
            if (siblingHtml != null && !siblingHtml.isEmpty()) {
                ctx.setSiblingElements(siblingHtml);
            }
        }

        /* -------- Semantic Parent (using stored parent xpath when available) -------- */
        String parentXpathCandidate = null;
        if (stored != null) {
            if (notBlank(stored.getParentXpathChain())) {
                parentXpathCandidate = extractXpathFromChain(stored.getParentXpathChain());
            }
            if (!notBlank(parentXpathCandidate) && notBlank(stored.getParentXpath())) {
                parentXpathCandidate = stored.getParentXpath();
            }
        }

        // Only fall back to stored parent xpath if we didn't already capture a live parent above
        if (!notBlank(ctx.getSemanticParentHtml()) && notBlank(parentXpathCandidate)) {
            ctx.setSemanticParentHtml(exec(js,
                    "var el=document.evaluate(arguments[0],document,null," +
                            "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;" +
                            "return el?el.outerHTML:null;",
                    parentXpathCandidate));
        } else if (!notBlank(ctx.getSemanticParentHtml())) {
            ctx.setSemanticParentHtml(exec(js,
                    "var el=document.evaluate(arguments[0],document,null," +
                            "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;" +
                            "if(el) return el.closest('nav,header,[role=\"navigation\"]')?.outerHTML;" +
                            "return document.querySelector('nav,header')?.outerHTML;",
                    brokenXpath));
        }

        /* -------- Sibling cluster (using stored sibling xpath when available) -------- */
        String siblingXpathCandidate = null;
        if (stored != null) {
            if (notBlank(stored.getSiblingXpathCluster())) {
                String raw = stored.getSiblingXpathCluster().trim();
                // Ignore JSON object-style cluster (not an XPath)
                if (!raw.startsWith("{")) {
                    siblingXpathCandidate = raw;
                }
            }
            if (!notBlank(siblingXpathCandidate) && notBlank(stored.getSiblingXpaths())) {
                siblingXpathCandidate = stored.getSiblingXpaths();
            }
        }

        // Only fall back to stored sibling xpath if we didn't already capture live siblings above
        if ((ctx.getSiblingElements() == null || ctx.getSiblingElements().isEmpty()) && notBlank(siblingXpathCandidate)) {
            ctx.setSiblingElements(execList(js,
                    "var r=[];" +
                            "var snap=document.evaluate(arguments[0],document,null," +
                            "XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,null);" +
                            "for(var i=0;i<snap.snapshotLength && i<20;i++){" +
                            " r.push(snap.snapshotItem(i).outerHTML);" +
                            "}" +
                            "return r;",
                    siblingXpathCandidate));
        } else if (ctx.getSiblingElements() == null || ctx.getSiblingElements().isEmpty()) {
            /* -------- Same-tag siblings (fallback) -------- */
            ctx.setSiblingElements(execList(js,
                    "var r=[];" +
                            "var tag=(arguments[0]||'').toLowerCase();" +
                            "if(!tag) tag='button';" +
                            "var nodes=document.querySelectorAll(tag);" +
                            "for(var i=0;i<nodes.length && r.length<20;i++){" +
                            " r.push(nodes[i].outerHTML);" +
                            "}" +
                            "return r;",
                    expectedTag));
        }

        /* -------- Text intelligence -------- */
        if (notBlank(expectedText)) {
            ctx.setClosestMatchingText(exec(js,
                    "var t=arguments[0];" +
                            "var tag=(arguments[1]||'').toLowerCase();" +
                            "if(!tag) tag='*';" +
                            "var best=null;" +
                            "document.querySelectorAll(tag).forEach(e=>{" +
                            " if(e && e.innerText && e.innerText.toLowerCase().includes(t.toLowerCase().substring(0,3)))" +
                            "   best=e.innerText;" +
                            "}); return best;",
                    expectedText, expectedTag));
        }

        String nearby = exec(js,
                "var t=(document.body && document.body.innerText) ? document.body.innerText : '';" +
                        "if(!t && document.documentElement) t=document.documentElement.innerText||'';" +
                        "return t.length>500?t.substring(0,500):t;");
        ctx.setNearbyText(nearby);

        /* -------- Constraints -------- */
        ctx.setAvoidStrategies(List.of("id", "class", "button", "input"));
        ctx.setPreferredStrategies(List.of("visible text", "aria-label", "relative xpath"));

        return ctx;
    }

    /* ================= Helpers ================= */

    private static String exec(JavascriptExecutor js, String script, Object... args) {
        try {
            Object r = (args == null || args.length == 0)
                    ? js.executeScript(script)
                    : js.executeScript(script, args);
            return r != null ? r.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> execList(JavascriptExecutor js, String script, Object... args) {
        try {
            Object r = (args == null || args.length == 0)
                    ? js.executeScript(script)
                    : js.executeScript(script, args);
            return r instanceof List ? (List<String>) r : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static boolean xpathExists(JavascriptExecutor js, String xpath) {
        if (!notBlank(xpath)) return false;
        Object r = js.executeScript(
                "return !!document.evaluate(arguments[0],document,null," +
                        "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;",
                xpath
        );
        return Boolean.TRUE.equals(r);
    }

    private static boolean cssExists(JavascriptExecutor js, String css) {
        if (!notBlank(css)) return false;
        Object r = js.executeScript(
                "return document.querySelector(arguments[0])!=null;",
                css
        );
        return Boolean.TRUE.equals(r);
    }

    /**
     * parentXpathChain is stored as a JSON-like array string, e.g.
     * [
     *   "/html/body/.../form",
     *   "/html/body/.../form/div[3]"
     * ]
     * This helper extracts the last non-blank XPath from that list.
     */
    private static String extractXpathFromChain(String chain) {
        if (!notBlank(chain)) return null;
        String trimmed = chain.trim();
        // If it's already a plain XPath, just return it
        if (!trimmed.startsWith("[")) {
            return trimmed;
        }
        try {
            // Strip [ and ] and split by comma
            if (trimmed.startsWith("[")) {
                trimmed = trimmed.substring(1);
            }
            if (trimmed.endsWith("]")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            String[] parts = trimmed.split(",");
            String candidate = null;
            for (String part : parts) {
                String p = part.trim();
                if (p.startsWith("\"")) {
                    p = p.substring(1);
                }
                if (p.endsWith("\"")) {
                    p = p.substring(0, p.length() - 1);
                }
                if (notBlank(p)) {
                    candidate = p;
                }
            }
            return candidate;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String firstNotBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (notBlank(v)) return v;
        }
        return null;
    }
}
