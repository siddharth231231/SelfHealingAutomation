package selfhealing.validation;

import org.openqa.selenium.WebDriver;
import selfhealing.validation.rules.*;

import java.util.List;

public class ValidationEngine {

    private static final List<ValidationRule> RULES = List.of(
            new UniquenessRule(),
            new VisibilityRule(),
            new InteractableRule(),
            new TagTypeRule(),
            new AttributeStabilityRule()
    );

    public static ValidationResult validate(WebDriver driver, String xpath) {

        for (ValidationRule rule : RULES) {
            ValidationResult result = rule.validate(driver, xpath);
            if (!result.isValid()) {
                return result; // FAIL FAST
            }
        }

        return ValidationResult.success();
    }
}
