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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import selfhealing.capture.CaptureInterceptor;
import selfhealing.core.SelfHealingEngine;
import selfhealing.locator.NamedBy;
import selfhealing.metrics.SelfHealingMetrics;
import selfhealing.retry.SmartRetryEngine;

import java.time.Duration;

public class BasePage {

    protected final WebDriver driver;
    protected final LocatorMetaService locatorMetaService;

    public BasePage(WebDriver driver, LocatorMetaService locatorMetaService) {
        this.driver = driver;
        this.locatorMetaService = locatorMetaService;
    }

    protected WebElement find(NamedBy locator) {
        SelfHealingMetrics.incrementLocatorUsed();

        try {
            //retry mechanism
            WebElement element = SmartRetryEngine.tryFindWithRetries(driver, locator.getBy());
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
                WebElement element = find(locator);
                scrollIntoView(element);
                waitUntilInteractable(element, Duration.ofSeconds(5));
                element.click();


    }

    private void waitUntilInteractable(WebElement element, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.ignoring(StaleElementReferenceException.class)
                .until(d -> element != null && element.isDisplayed() && element.isEnabled());
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
