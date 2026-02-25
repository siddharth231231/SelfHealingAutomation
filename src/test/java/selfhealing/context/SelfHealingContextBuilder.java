package selfhealing.context;

import selfhealing.context.dom.DomContext;
import selfhealing.context.storedContext.DbExtractedData;

/**
 * Builds a unified {@link SelfHealingContext} from:
 * <ul>
 * <li>Live DOM capture data ({@link DomContext})</li>
 * <li>Stored historical locator metadata ({@link DbExtractedData})</li>
 * </ul>
 * This merged object is what gets sent to the Spring AI agent.
 */
public class SelfHealingContextBuilder {

    private SelfHealingContextBuilder() {
        /* utility class */ }

    public static SelfHealingContext build(
            String testName,
            DomContext live,
            DbExtractedData stored) {

        SelfHealingContext ctx = new SelfHealingContext();

        /*
         * =====================================================
         * Test identity
         * =====================================================
         */
        ctx.setTestName(testName);

        /*
         * =====================================================
         * Live DOM fields (from DomContext)
         * =====================================================
         */
        if (live != null) {
            ctx.setBrokenXpath(live.getBrokenXpath());
            ctx.setNormalizedBrokenXpath(live.getNormalizedBrokenXpath());
            ctx.setExpectedTag(live.getExpectedTag());
            ctx.setExpectedText(live.getExpectedText());
            ctx.setExpectedAttributes(live.getExpectedAttributes());
            ctx.setBrokenXpathDepth(live.getBrokenXpathDepth());
            ctx.setBrokenXpathDynamicRiskScore(live.getBrokenXpathDynamicRiskScore());
            ctx.setBrokenXpathRiskReasons(live.getBrokenXpathRiskReasons());
            ctx.setFailureType(live.getFailureType());

            ctx.setPageUrl(live.getPageUrl());
            ctx.setPageTitle(live.getPageTitle());
            ctx.setPageRole(live.getPageRole());

            ctx.setElementFound(live.isElementFound());
            ctx.setBrokenXpathValid(live.isBrokenXpathValid());
            ctx.setBrokenXpathMatchCount(live.getBrokenXpathMatchCount());

            ctx.setSemanticParentHtml(live.getSemanticParentHtml());
            ctx.setSiblingElements(live.getSiblingElements());

            ctx.setClosestMatchingText(live.getClosestMatchingText());
            ctx.setNearbyText(live.getNearbyText());
            ctx.setCandidateElements(live.getCandidateElements());
            ctx.setTopCandidateXpaths(live.getTopCandidateXpaths());

            ctx.setStableAttributes(live.getStableAttributes());

            ctx.setAvoidStrategies(live.getAvoidStrategies());
            ctx.setPreferredStrategies(live.getPreferredStrategies());
        }

        /*
         * =====================================================
         * Stored historical metadata (from DbExtractedData)
         * =====================================================
         */
        if (stored != null) {
            ctx.setLocatorName(stored.getLocatorName());
            ctx.setOriginalLocator(stored.getOriginalLocator());
            ctx.setCurrentActiveLocator(stored.getCurrentActiveLocator());

            ctx.setRelativeXpath(stored.getRelativeXpath());
            ctx.setAbsoluteXpath(stored.getAbsoluteXpath());
            ctx.setCssSelector(stored.getCssSelector());

            ctx.setParentXpath(stored.getParentXpath());
            ctx.setSiblingXpaths(stored.getSiblingXpaths());
            ctx.setParentXpathChain(stored.getParentXpathChain());
            ctx.setSiblingXpathCluster(stored.getSiblingXpathCluster());
            ctx.setElementTag(stored.getElementTag());
            ctx.setElementRole(stored.getElementRole());
            ctx.setElementType(stored.getElementType());
            ctx.setNormalizedVisibleText(stored.getNormalizedVisibleText());
            ctx.setStableAttributeJson(stored.getStableAttributeJson());
            ctx.setVolatileAttributeJson(stored.getVolatileAttributeJson());
            ctx.setAnchorHierarchyJson(stored.getAnchorHierarchyJson());
            ctx.setSiblingSignatureJson(stored.getSiblingSignatureJson());

            ctx.setHistoricalPageUrl(stored.getPageUrl());
            ctx.setHistoricalPageTitle(stored.getPageTitle());

            ctx.setDomSnapshot(stored.getDomSnapshot());
            ctx.setDomHash(stored.getDomHash());

            ctx.setLocatorVersion(stored.getLocatorVersion());
            ctx.setHealCount(stored.getHealCount());
            ctx.setLastSimilarityScore(stored.getLastSimilarityScore());
            ctx.setAverageValidationScore(stored.getAverageValidationScore());
            ctx.setLocatorConfidence(stored.getLocatorConfidence());
            ctx.setAmbiguityScore(stored.getAmbiguityScore());
            ctx.setHealSuccessCount(stored.getHealSuccessCount());
            ctx.setHealFailureCount(stored.getHealFailureCount());
            ctx.setLastHealedAt(stored.getLastHealedAt());
            ctx.setLastValidatedAt(stored.getLastValidatedAt());

            ctx.setCreatedAt(stored.getCreatedAt());
            ctx.setUpdatedAt(stored.getUpdatedAt());
        }

        return ctx;
    }
}
