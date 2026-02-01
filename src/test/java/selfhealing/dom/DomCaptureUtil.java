package selfhealing.dom;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        /* -------- Semantic Parent -------- */
        ctx.setSemanticParentHtml(exec(js,
                "var el=document.evaluate(arguments[0],document,null," +
                        "XPathResult.FIRST_ORDERED_NODE_TYPE,null).singleNodeValue;" +
                        "if(el) return el.closest('nav,header,[role=\"navigation\"]')?.outerHTML;" +
                        "return document.querySelector('nav,header')?.outerHTML;",
                brokenXpath));

        /* -------- Same-tag siblings -------- */
        ctx.setSiblingElements(execList(js,
                "var r=[];" +
                        "document.querySelectorAll('" + expectedTag + "').forEach(e=>{" +
                        " if(e.innerText && e.innerText.length<30)" +
                        "   r.push(e.outerHTML);" +
                        "}); return r;",
                null));

        /* -------- Text intelligence -------- */
        ctx.setClosestMatchingText(exec(js,
                "var t='" + expectedText + "';" +
                        "var best=null;" +
                        "document.querySelectorAll('" + expectedTag + "').forEach(e=>{" +
                        " if(e.innerText && e.innerText.toLowerCase().includes(t.toLowerCase().substring(0,3)))" +
                        "   best=e.innerText;" +
                        "}); return best;",
                null));

        ctx.setNearbyText(exec(js,
                "return document.body.innerText.substring(0,500);",
                null));

        /* -------- Constraints -------- */
        ctx.setAvoidStrategies(List.of("id", "class", "button", "input"));
        ctx.setPreferredStrategies(List.of("visible text", "aria-label", "relative xpath"));

        return ctx;
    }

    /* ================= Helpers ================= */

    private static String exec(JavascriptExecutor js, String script, String arg) {
        Object r = arg == null
                ? js.executeScript(script)
                : js.executeScript(script, arg);
        return r != null ? r.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> execList(JavascriptExecutor js, String script, String arg) {
        Object r = arg == null
                ? js.executeScript(script)
                : js.executeScript(script, arg);
        return r instanceof List ? (List<String>) r : new ArrayList<>();
    }
}
