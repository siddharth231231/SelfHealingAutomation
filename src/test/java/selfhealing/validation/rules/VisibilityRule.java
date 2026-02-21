package selfhealing.validation.rules;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.validation.ValidationRule;

import java.util.List;

public class VisibilityRule implements ValidationRule {

    @Override
    public String getName() {
        return "Visibility Rule";
    }

    @Override
    public int getWeight() {
        return 20;
    }

    @Override
    public int validate(WebDriver driver, String xpath) {

        List<WebElement> elements = driver.findElements(By.xpath(xpath));

        if (elements.isEmpty()) {
            return 0;
        }

        return elements.get(0).isDisplayed() ? 100 : 40;
    }
}