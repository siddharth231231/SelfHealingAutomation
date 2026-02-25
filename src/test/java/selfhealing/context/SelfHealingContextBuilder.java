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
            ctx.setExpectedTag(live.getExpectedTag());
            ctx.setExpectedText(live.getExpectedText());
            ctx.setFailureType(live.getFailureType());

            ctx.setPageUrl(live.getPageUrl());
            ctx.setPageTitle(live.getPageTitle());
            ctx.setPageRole(live.getPageRole());

            ctx.setElementFound(live.isElementFound());

            ctx.setSemanticParentHtml(live.getSemanticParentHtml());
            ctx.setSiblingElements(live.getSiblingElements());

            ctx.setClosestMatchingText(live.getClosestMatchingText());
            ctx.setNearbyText(live.getNearbyText());

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

            ctx.setHistoricalPageUrl(stored.getPageUrl());
            ctx.setHistoricalPageTitle(stored.getPageTitle());

            ctx.setDomSnapshot(stored.getDomSnapshot());
            ctx.setDomHash(stored.getDomHash());

            ctx.setLocatorVersion(stored.getLocatorVersion());
            ctx.setHealCount(stored.getHealCount());
            ctx.setLastSimilarityScore(stored.getLastSimilarityScore());

            ctx.setCreatedAt(stored.getCreatedAt());
            ctx.setUpdatedAt(stored.getUpdatedAt());
        }

        return ctx;
    }
}
