package selfhealing.validation;

import org.openqa.selenium.WebDriver;

public interface ValidationRule {

    ValidationResult validate(WebDriver driver, String xpath);
}
