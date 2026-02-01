package selfhealing.validation.rules;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.validation.ValidationResult;
import selfhealing.validation.ValidationRule;

import java.util.Set;

public class TagTypeRule implements ValidationRule {

    private static final Set<String> ALLOWED_TAGS =
            Set.of("input", "button", "select", "textarea", "a");

    @Override
    public ValidationResult validate(WebDriver driver, String xpath) {
        WebElement element = driver.findElement(By.xpath(xpath));
        String tag = element.getTagName();

        if (!ALLOWED_TAGS.contains(tag)) {
            return ValidationResult.failure(
                    "Unexpected tag type: " + tag
            );
        }

        return ValidationResult.success();
    }
}
