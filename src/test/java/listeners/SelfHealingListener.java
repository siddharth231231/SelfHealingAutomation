package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import config.FrameworkConfig;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import selfhealing.reporting.ExtentManager;
import selfhealing.reporting.HealingReportLogger;

public class SelfHealingListener implements ITestListener {

    private static final ExtentReports EXTENT = ExtentManager.getInstance();
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = EXTENT.createTest(result.getName());
        CURRENT.set(test);
        HealingReportLogger.setCurrentTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = CURRENT.get();
        if (test != null) {
            test.pass("Test passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {

        if (!FrameworkConfig.isSelfHealingEnabled()) {
            return;
        }

        Throwable exception = result.getThrowable();

        if (!isLocatorFailure(exception)) {
            return;
        }

        ExtentTest test = CURRENT.get();
        if (test != null) {
            test.warning("Test failed due to locator issue after healing attempts.");
        }

        System.out.println(
                "[SELF-HEALING] Test failed after inline healing pipeline. test="
                        + result.getName()
        );
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = CURRENT.get();
        if (test != null) {
            test.skip("Test skipped");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        HealingReportLogger.logSummary();
        EXTENT.flush();
    }

    private boolean isLocatorFailure(Throwable exception) {
        if (exception == null) {
            return false;
        }
        return exception instanceof org.openqa.selenium.NoSuchElementException
                || exception instanceof org.openqa.selenium.InvalidSelectorException
                || exception instanceof org.openqa.selenium.TimeoutException
                || (exception.getCause() instanceof org.openqa.selenium.NoSuchElementException);
    }
}
