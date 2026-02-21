package selfhealing.validation.rules;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.validation.ValidationRule;

import java.util.List;

public class InteractableRule implements ValidationRule {

    @Override
    public String getName() {
        return "Interactable Rule";
    }

    @Override
    public int getWeight() {
        return 20;  // Important rule
    }

    @Override
    public int validate(WebDriver driver, String xpath) {

        List<WebElement> elements = driver.findElements(By.xpath(xpath));

        if (elements.isEmpty()) {
            return 0;
        }

        WebElement element = elements.get(0);

        int score = 100;

        if (!element.isEnabled()) {
            score -= 50;
        }

        String readonly = element.getAttribute("readonly");
        if (readonly != null) {
            score -= 40;
        }

        return Math.max(score, 0);
    }
}