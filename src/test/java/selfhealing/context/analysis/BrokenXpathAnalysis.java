package selfhealing.context.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BrokenXpathAnalysis {
    private String rawXpath;
    private String normalizedXpath;
    private String expectedTag;
    private String expectedText;
    private Map<String, String> expectedAttributes = new LinkedHashMap<>();
    private Integer depth;
    private Integer dynamicRiskScore;
    private List<String> riskReasons = new ArrayList<>();

    public String getRawXpath() {
        return rawXpath;
    }

    public void setRawXpath(String rawXpath) {
        this.rawXpath = rawXpath;
    }

    public String getNormalizedXpath() {
        return normalizedXpath;
    }

    public void setNormalizedXpath(String normalizedXpath) {
        this.normalizedXpath = normalizedXpath;
    }

    public String getExpectedTag() {
        return expectedTag;
    }

    public void setExpectedTag(String expectedTag) {
        this.expectedTag = expectedTag;
    }

    public String getExpectedText() {
        return expectedText;
    }

    public void setExpectedText(String expectedText) {
        this.expectedText = expectedText;
    }

    public Map<String, String> getExpectedAttributes() {
        return expectedAttributes;
    }

    public void setExpectedAttributes(Map<String, String> expectedAttributes) {
        this.expectedAttributes = expectedAttributes;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getDynamicRiskScore() {
        return dynamicRiskScore;
    }

    public void setDynamicRiskScore(Integer dynamicRiskScore) {
        this.dynamicRiskScore = dynamicRiskScore;
    }

    public List<String> getRiskReasons() {
        return riskReasons;
    }

    public void setRiskReasons(List<String> riskReasons) {
        this.riskReasons = riskReasons;
    }
}
