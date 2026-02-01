package selfhealing.validation.rules;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.validation.ValidationResult;
import selfhealing.validation.ValidationRule;

import java.util.List;

public class UniquenessRule implements ValidationRule {

    @Override
    public ValidationResult validate(WebDriver driver, String xpath) {
        try {
            List<WebElement> elements = driver.findElements(By.xpath(xpath));

            if (elements.isEmpty()) {
                return ValidationResult.failure("XPath does not match any element");
            }

            if (elements.size() > 1) {
                return ValidationResult.failure(
                        "XPath is not unique. Matches " + elements.size() + " elements"
                );
            }

            return ValidationResult.success();

        } catch (Exception e) {
            return ValidationResult.failure("Invalid XPath syntax");
        }
    }
}
