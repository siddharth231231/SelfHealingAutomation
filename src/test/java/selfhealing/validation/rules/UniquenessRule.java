package selfhealing.validation.rules;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.validation.ValidationRule;

import java.util.List;

public class UniquenessRule implements ValidationRule {

    @Override
    public String getName() {
        return "Uniqueness Rule";
    }

    @Override
    public int getWeight() {
        return 25;  // 25% importance
    }

    @Override
    public int validate(WebDriver driver, String xpath) {

        List<WebElement> elements = driver.findElements(By.xpath(xpath));

        if (elements.size() == 1) {
            return 100;
        } else if (elements.size() > 1) {
            return 50;
        } else {
            return 0;
        }
    }
}