package selfhealing.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import selfhealing.metrics.SelfHealingMetrics;

import java.time.Duration;
import java.util.Optional;

/**
 * Thin wrapper to keep Extent logging concerns out of the engine.
 * Caller is responsible for wiring singleton ExtentReports instance.
 */
public final class HealingReportLogger {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    private HealingReportLogger() {}

    public static void init(ExtentReports reports) {
        extent = reports;
    }

    public static void setCurrentTest(ExtentTest test) {
        currentTest.set(test);
    }

    public static void logHealingSuccess(
            String locatorName,
            String originalXpath,
            String healedXpath,
            String healeniumXpath,
            Double healeniumScore,
            String source,
            int attempt,
            String domHash,
            Duration duration) {

        ExtentTest test = currentTest.get();
        if (extent == null || test == null) {
            return; // graceful degradation
        }

        String label = "SELF HEALING TRIGGERED";
        String msg = String.format(
                "Original XPath: %s\nHealed XPath: %s\nSource: %s\nHealenium Suggestion: %s\nSimilarity Score: %.3f\nRetry Attempt: %d\nDOM Hash: %s\nHealing Time: %d ms",
                optional(originalXpath),
                optional(healedXpath),
                optional(source),
                optional(healeniumXpath),
                healeniumScore == null ? 0.0 : healeniumScore,
                attempt,
                optional(domHash),
                duration == null ? 0 : duration.toMillis()
        );
        test.log(Status.WARNING, label + "\n" + msg);
    }

    public static void attachHealingScreenshot(WebDriver driver) {
        ExtentTest test = currentTest.get();
        if (extent == null || test == null || !(driver instanceof TakesScreenshot ts)) {
            return;
        }
        String base64 = ts.getScreenshotAs(OutputType.BASE64);
        test.addScreenCaptureFromBase64String(base64, "Healing screenshot");
    }

    public static void logSummary() {
        if (extent == null) {
            return;
        }
        SelfHealingMetrics.Snapshot s = SelfHealingMetrics.snapshot();
        String summary = String.format(
                "Total Locators Used: %d\nTotal Healed Locators: %d\nHealing Success Rate: %.2f%%\nAverage Healing Time: %.2f seconds",
                s.totalLocatorsUsed(),
                s.healedLocators(),
                s.successRatePercent(),
                s.avgHealingSeconds()
        );
        extent.createTest("Self-Healing Summary").log(Status.INFO, summary);
    }

    private static String optional(String value) {
        return Optional.ofNullable(value).orElse("N/A");
    }
}
