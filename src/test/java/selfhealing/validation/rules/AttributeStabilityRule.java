package selfhealing.validation.rules;

import org.openqa.selenium.WebDriver;
import selfhealing.validation.ValidationResult;
import selfhealing.validation.ValidationRule;

public class AttributeStabilityRule implements ValidationRule {

    @Override
    public ValidationResult validate(WebDriver driver, String xpath) {

        if (xpath.contains("@class") && xpath.matches(".*\\d+.*")) {
            return ValidationResult.failure(
                    "XPath relies on unstable class attribute"
            );
        }

        if (xpath.contains("@id") && xpath.matches(".*\\d+.*")) {
            return ValidationResult.failure(
                    "XPath relies on dynamic id"
            );
        }

        return ValidationResult.success();
    }
}
