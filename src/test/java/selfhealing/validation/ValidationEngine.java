package selfhealing.validation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import selfhealing.validation.rules.*;

import java.util.*;

public class ValidationEngine {

    private static final int ACCEPTANCE_THRESHOLD = 60;

    public static ValidationResult validate(WebDriver driver, String xpath) {

        // Explicitly wait up to 5 seconds for the suggested element to appear in the
        // DOM
        // because modern SPAs (like Vue in OrangeHRM) render components asynchronously.
        try {
            WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        } catch (Exception e) {
            // If it doesn't appear after 5 seconds, let the rules score it (they will score
            // it 0)
        }

        List<ValidationRule> rules = Arrays.asList(
                new UniquenessRule(),
                new VisibilityRule(),
                new InteractableRule(),
                new TagTypeRule(),
                new AttributeStabilityRule());

        Map<String, Integer> scoreMap = new LinkedHashMap<>();

        int totalWeight = 0;
        int weightedScoreSum = 0;

        for (ValidationRule rule : rules) {

            int ruleScore = rule.validate(driver, xpath);
            int weight = rule.getWeight();

            totalWeight += weight;
            weightedScoreSum += (ruleScore * weight);

            scoreMap.put(rule.getName(), ruleScore);
        }

        int finalScore = weightedScoreSum / totalWeight;

        boolean isValid = finalScore >= ACCEPTANCE_THRESHOLD;

        return new ValidationResult(isValid, finalScore, scoreMap);
    }
}