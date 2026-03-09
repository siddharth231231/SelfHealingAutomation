package selfhealing.metrics;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central place to collect healing KPIs for summary reporting.
 */
public final class SelfHealingMetrics {

    private static final AtomicInteger totalLocatorsUsed = new AtomicInteger();
    private static final AtomicInteger healedLocators = new AtomicInteger();
    private static final AtomicInteger failedHealings = new AtomicInteger();
    private static final AtomicLong totalHealingTimeNanos = new AtomicLong();

    private SelfHealingMetrics() {}

    public static void incrementLocatorUsed() {
        totalLocatorsUsed.incrementAndGet();
    }

    public static void recordHealingSuccess(Duration duration) {
        healedLocators.incrementAndGet();
        if (duration != null) {
            totalHealingTimeNanos.addAndGet(duration.toNanos());
        }
    }

    public static void recordHealingFailure() {
        failedHealings.incrementAndGet();
    }

    public static Snapshot snapshot() {
        int total = Math.max(totalLocatorsUsed.get(), 1); // avoid div-by-zero
        int healed = healedLocators.get();
        int failed = failedHealings.get();
        long nanos = totalHealingTimeNanos.get();
        double avgSeconds = healed == 0 ? 0 : nanos / 1_000_000_000.0 / healed;
        double successRate = healed == 0 ? 0 : (healed * 100.0) / (healed + failed);
        return new Snapshot(total, healed, failed, successRate, avgSeconds);
    }

    public record Snapshot(
            int totalLocatorsUsed,
            int healedLocators,
            int failedHealings,
            double successRatePercent,
            double avgHealingSeconds) {
    }
}
