package tests.pages;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.locator.NamedBy;

public class BasePage {

    protected WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    protected WebElement find(NamedBy locator) {

        try {
            return driver.findElement(locator.getBy());
        }

        catch (NoSuchElementException e) {

            String message =
                    "\n======= LOCATOR FAILURE =======\n" +
                            "Element Name : " + locator.getElementName() + "\n" +
                            "Locator      : " + locator.getBy().toString() + "\n" +
                            "================================";

            //throw new NoSuchElementException(message);
            throw e;
        }
    }
}