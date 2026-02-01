package selfhealing.validation.rules;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.validation.ValidationResult;
import selfhealing.validation.ValidationRule;

public class InteractableRule implements ValidationRule {

    @Override
    public ValidationResult validate(WebDriver driver, String xpath) {
        WebElement element = driver.findElement(By.xpath(xpath));

        if (!element.isEnabled()) {
            return ValidationResult.failure("Element is disabled");
        }

        String readonly = element.getAttribute("readonly");
        if (readonly != null) {
            return ValidationResult.failure("Element is readonly");
        }

        return ValidationResult.success();
    }
}
