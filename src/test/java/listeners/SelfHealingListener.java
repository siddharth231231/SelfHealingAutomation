package listeners;

import base.BaseTest;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import selfhealing.core.SelfHealingEngine;

public class SelfHealingListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {

        Throwable exception = result.getThrowable();

        if (!isLocatorFailure(exception)) {
            return;
        }

        // ✅ Correct way to fetch driver when using WebDriverManager + ThreadLocal
        WebDriver driver = BaseTest.getDriver();

        if (driver == null) {
            System.out.println("❌ WebDriver is NULL. Self-healing cannot proceed.");
            return;
        }

        SelfHealingEngine.heal(
                driver,
                exception,
                result.getName()
        );
    }

    private boolean isLocatorFailure(Throwable exception) {
        return exception instanceof org.openqa.selenium.NoSuchElementException
                || exception instanceof org.openqa.selenium.TimeoutException
                || exception.getCause() instanceof org.openqa.selenium.NoSuchElementException;
    }
}
