package selfhealing.retry;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Applies the required smart retry policy before healing kicks in.
 * Attempt 1: immediate; Attempt 2: +1s wait; Attempt 3: +2s wait.
 */
public final class SmartRetryEngine {

    private SmartRetryEngine() {}

    public static WebElement tryFindWithRetries(WebDriver driver, By by) {
        if (driver == null || by == null) {
            throw new NoSuchElementException("Driver or By is null");
        }

        // Attempt 1: immediate
        try {
            return driver.findElement(by);
        } catch (NoSuchElementException ignored) { }

        // Attempt 2: wait 1 second
        try {
            return waitFor(driver, by, Duration.ofSeconds(1));
        } catch (NoSuchElementException ignored) { }

        // Attempt 3: wait 2 seconds
        return waitFor(driver, by, Duration.ofSeconds(2));
    }

    private static WebElement waitFor(WebDriver driver, By by, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (Exception e) {
            throw new NoSuchElementException("Element not found after wait " + timeout.toSeconds() + "s", e);
        }
    }
}
