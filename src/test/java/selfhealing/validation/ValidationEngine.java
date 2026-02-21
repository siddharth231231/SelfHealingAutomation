package selfhealing.validation;

import org.openqa.selenium.WebDriver;
import selfhealing.validation.rules.*;

import java.util.*;

public class ValidationEngine {

    private static final int ACCEPTANCE_THRESHOLD = 80;

    public static ValidationResult validate(WebDriver driver, String xpath) {

        List<ValidationRule> rules = Arrays.asList(
                new UniquenessRule(),
                new VisibilityRule(),
                new InteractableRule(),
                new TagTypeRule(),
                new AttributeStabilityRule()
        );

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