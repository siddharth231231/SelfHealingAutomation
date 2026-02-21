package selfhealing.validation;

import org.openqa.selenium.WebDriver;

public interface ValidationRule {

    String getName();

    int getWeight();   // importance of this rule

    int validate(WebDriver driver, String xpath);
    // return score (0-100)
}