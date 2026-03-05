package listeners;

import config.FrameworkConfig;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class SelfHealingListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {

        if (!FrameworkConfig.isSelfHealingEnabled()) {
            return;
        }

        Throwable exception = result.getThrowable();

        if (!isLocatorFailure(exception)) {
            return;
        }

        // V2 healing executes inline in BasePage.find(...), so listener is now
        // informational only.
        System.out.println(
                "[SELF-HEALING] Test failed after inline healing pipeline. test="
                        + result.getName()
        );
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
