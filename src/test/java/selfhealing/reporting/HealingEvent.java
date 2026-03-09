package selfhealing.reporting;

import java.time.Duration;

public record HealingEvent(
        String locatorName,
        String originalXpath,
        String healedXpath,
        String healeniumXpath,
        Double healeniumScore,
        String source,
        int attempt,
        String domHash,
        Duration duration
) {}
