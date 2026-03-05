package tests.pages;

import com.yourcompany.selfhealing.service.LocatorMetaService;
import config.FrameworkConfig;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import selfhealing.capture.CaptureInterceptor;
import selfhealing.core.SelfHealingEngine;
import selfhealing.locator.NamedBy;

import java.time.Duration;

public class BasePage {

    protected final WebDriver driver;
    protected final LocatorMetaService locatorMetaService;

    public BasePage(WebDriver driver, LocatorMetaService locatorMetaService) {
        this.driver = driver;
        this.locatorMetaService = locatorMetaService;
    }

    protected WebElement find(NamedBy locator) {
        try {
            WebElement element = driver.findElement(locator.getBy());
            if (FrameworkConfig.isHealingCaptureEnabled()) {
                CaptureInterceptor.capture(driver, element, locator, locatorMetaService);
            }
            return element;
        } catch (NoSuchElementException | InvalidSelectorException failure) {
            if (FrameworkConfig.isHealingModeEnabled()) {
                return SelfHealingEngine.heal(driver, locator, locatorMetaService, failure);
            }
            throw failure;
        }
    }

    protected void click(NamedBy locator) {
        RuntimeException lastFailure = null;
        int maxAttempts = Math.max(2, FrameworkConfig.getHealingRetryMax());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            WebElement element = find(locator);
            try {
                scrollIntoView(element);
                waitUntilInteractable(element, Duration.ofSeconds(5));
                element.click();
                return;
            } catch (ElementNotInteractableException
                     | StaleElementReferenceException
                     | TimeoutException e) {

                lastFailure = wrap(locator, attempt, e);

                if (tryActionsClick(element)) {
                    return;
                }
                if (tryJsClick(element)) {
                    return;
                }
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
        }
        throw new RuntimeException("Click failed for locator: " + locator.getElementName());
    }

    private void waitUntilInteractable(WebElement element, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(d -> {
            try {
                return element != null && element.isDisplayed() && element.isEnabled();
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                    element
            );
        } catch (Exception ignored) {
            // best effort
        }
    }

    private boolean tryActionsClick(WebElement element) {
        try {
            new Actions(driver).moveToElement(element).pause(Duration.ofMillis(100)).click().perform();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean tryJsClick(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private RuntimeException wrap(NamedBy locator, int attempt, Exception cause) {
        return new RuntimeException(
                "Click failed for locator " + locator.getElementName() + " on attempt " + attempt,
                cause
        );
    }
}
