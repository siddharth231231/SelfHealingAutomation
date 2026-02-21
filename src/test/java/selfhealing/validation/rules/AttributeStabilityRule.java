package selfhealing.validation.rules;

import org.openqa.selenium.WebDriver;
import selfhealing.validation.ValidationRule;

public class AttributeStabilityRule implements ValidationRule {

    @Override
    public String getName() {
        return "Attribute Stability Rule";
    }

    @Override
    public int getWeight() {
        return 15;  // Medium importance
    }

    @Override
    public int validate(WebDriver driver, String xpath) {

        if (xpath == null || xpath.isEmpty()) {
            return 0;
        }

        int score = 100;

        // Penalize unstable class usage (e.g., class with numbers)
        if (xpath.contains("@class") && xpath.matches(".*\\d+.*")) {
            score -= 40;
        }

        // Penalize dynamic IDs (ids containing numbers)
        if (xpath.contains("@id") && xpath.matches(".*\\d+.*")) {
            score -= 40;
        }

        // If heavily unstable → fail completely
        if (score <= 20) {
            return 0;
        }

        return score;
    }
}