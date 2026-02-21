package selfhealing.validation;

import java.util.Map;

public class ValidationResult {

    private boolean valid;
    private int totalScore;
    private Map<String, Integer> ruleScores;

    public ValidationResult(boolean valid, int totalScore,
                            Map<String, Integer> ruleScores) {
        this.valid = valid;
        this.totalScore = totalScore;
        this.ruleScores = ruleScores;
    }

    public boolean isValid() {
        return valid;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public Map<String, Integer> getRuleScores() {
        return ruleScores;
    }
}