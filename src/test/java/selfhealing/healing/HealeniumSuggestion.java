package selfhealing.healing;

public class HealeniumSuggestion {

    private String xpath;
    private double score;

    public HealeniumSuggestion() {
    }

    public HealeniumSuggestion(String xpath, double score) {
        this.xpath = xpath;
        this.score = score;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
