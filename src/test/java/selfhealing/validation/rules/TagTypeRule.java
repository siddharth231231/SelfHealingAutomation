package selfhealing.validation.rules;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.validation.ValidationRule;

import java.util.List;
import java.util.Set;

public class TagTypeRule implements ValidationRule {

    private static final Set<String> ALLOWED_TAGS =
            Set.of("input", "button", "select", "textarea", "a");

    @Override
    public String getName() {
        return "Tag Type Rule";
    }

    @Override
    public int getWeight() {
        return 15;  // Medium importance
    }

    @Override
    public int validate(WebDriver driver, String xpath) {

        List<WebElement> elements = driver.findElements(By.xpath(xpath));

        if (elements.isEmpty()) {
            return 0;
        }

        String tag = elements.get(0).getTagName();

        if (ALLOWED_TAGS.contains(tag)) {
            return 100;
        }

        return 40;  // Partial score if tag unexpected
    }
}