package selfhealing.healing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HealeniumAlgorithmServiceTest {

    private static final Map<String, String> BUTTON_ATTRIBUTES = Map.of(
            "id", "pay-btn",
            "type", "submit"
    );

    @Test
    public void pathSimilarityBoostsCandidateWithCloserAncestorChain() throws Exception {
        String storedJson = new ObjectMapper().writeValueAsString(buildStoredNode());

        LiveNodeCandidate distantCandidate = candidate(
                "/html/body/nav/button[1]",
                buildCandidatePath("main")
        );
        LiveNodeCandidate closeCandidate = candidate(
                "/html/body/form/button[1]",
                buildCandidatePath("body")
        );

        HealeniumSuggestion suggestion = HealeniumAlgorithmService.suggest(
                storedJson,
                List.of(distantCandidate, closeCandidate)
        );

        Assert.assertEquals(suggestion.getXpath(), closeCandidate.getXpath());
        Assert.assertTrue(suggestion.getScore() > 0.9, "Score should reflect the strong match");
    }

    @Test
    public void longestCommonSubsequenceGuidesPathSelection() throws Exception {
        Map<String, Object> storedNode = buildChainNode("html", "body", "div", "form", "button");
        String storedJson = new ObjectMapper().writeValueAsString(storedNode);

        LiveNodeCandidate extendedPath = candidate(
                "/html/body/div/form/button[1]",
                buildChainNode("html", "body", "div", "form", "button")
        );
        LiveNodeCandidate fragmentedPath = candidate(
                "/html/body/section/form/button[1]",
                buildChainNode("html", "body", "section", "footer", "button")
        );

        HealeniumSuggestion suggestion = HealeniumAlgorithmService.suggest(
                storedJson,
                List.of(fragmentedPath, extendedPath)
        );

        Assert.assertEquals(suggestion.getXpath(), extendedPath.getXpath());
        Assert.assertTrue(suggestion.getScore() > 0.8, "Better LCS should yield higher score");
    }

    @Test
    public void returnsCandidateEvenWhenAllScoresZero() throws Exception {
        Map<String, Object> storedNode = buildChainNode("html", "body", "div", "button");
        String storedJson = new ObjectMapper().writeValueAsString(storedNode);

        LiveNodeCandidate loneCandidate = candidate(
                "/html/body/footer[1]",
                buildNode("footer", "Other text", Collections.emptyMap(), null)
        );

        HealeniumSuggestion suggestion = HealeniumAlgorithmService.suggest(
                storedJson,
                List.of(loneCandidate)
        );

        Assert.assertEquals(suggestion.getXpath(), loneCandidate.getXpath());
        Assert.assertEquals(suggestion.getScore(), 0.0);
    }

    private static Map<String, Object> buildStoredNode() {
        Map<String, Object> body = buildNode("body", null, Collections.emptyMap(), null);
        Map<String, Object> form = buildNode("form", null, Map.of("data-role", "checkout"), body);
        return buildNode("button", "Pay now", BUTTON_ATTRIBUTES, form);
    }

    private static Map<String, Object> buildCandidatePath(String grandparentTag) {
        Map<String, Object> grandparent = buildNode(grandparentTag, null, Collections.emptyMap(), null);
        Map<String, Object> parent = buildNode("form", null, Map.of("data-role", "checkout"), grandparent);
        return buildNode("button", "Pay now", BUTTON_ATTRIBUTES, parent);
    }

    private static LiveNodeCandidate candidate(String xpath, Map<String, Object> nodePath) {
        LiveNodeCandidate candidate = new LiveNodeCandidate();
        candidate.setXpath(xpath);
        candidate.setNodePath(nodePath);
        return candidate;
    }

    private static Map<String, Object> buildNode(
            String tag,
            String innerText,
            Map<String, String> attributes,
            Map<String, Object> parent) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("tag", tag);
        node.put("attributes", new LinkedHashMap<>(attributes));
        node.put("innerText", innerText);
        node.put("children", List.of());
        node.put("parent", parent);
        return node;
    }

    @SafeVarargs
    private static Map<String, Object> buildChainNode(String... tags) {
        Map<String, Object> parent = null;
        Map<String, Object> current = null;
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            Map<String, String> attrs = (i == tags.length - 1) ? BUTTON_ATTRIBUTES : Collections.emptyMap();
            String text = (i == tags.length - 1) ? "Pay now" : null;
            current = buildNode(tag, text, attrs, parent);
            parent = current;
        }
        return current;
    }
}
