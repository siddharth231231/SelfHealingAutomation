package selfhealing.agent;

public class AgentResponse {

    private String healedXpath;
    private double confidence;

    public String getHealedXpath() {
        return healedXpath;
    }

    public void setHealedXpath(String healedXpath) {
        this.healedXpath = healedXpath;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
