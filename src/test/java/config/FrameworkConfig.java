package config;

import java.io.FileInputStream;
import java.util.Properties;

public class FrameworkConfig {

    private static boolean baselineMode = false;
    private static boolean captureOnFirstRun = false;
    private static boolean selfHealingEnabled = true;
    private static boolean healingCaptureEnabled = true;
    private static boolean healingModeEnabled = false;
    private static int healingRetryMax = 3;
    private static String healingSpringAiUrl = "http://localhost:8081/api/heal";

    public static void load() {
        try {
            Properties prop = new Properties();
            FileInputStream fis =
                    new FileInputStream("src/test/resources/framework.properties");
            prop.load(fis);

            baselineMode = Boolean.parseBoolean(
                    prop.getProperty("baseline", "false")
            );
            captureOnFirstRun = Boolean.parseBoolean(
                    prop.getProperty("selfhealing.capture_on_first_run", "false")
            );
            selfHealingEnabled = Boolean.parseBoolean(
                    prop.getProperty("selfhealing.enabled", "true")
            );
            healingCaptureEnabled = Boolean.parseBoolean(
                    prop.getProperty(
                            "healing.capture.enabled",
                            String.valueOf(captureOnFirstRun)
                    )
            );
            healingModeEnabled = Boolean.parseBoolean(
                    prop.getProperty(
                            "healing.mode.enabled",
                            String.valueOf(selfHealingEnabled)
                    )
            );
            healingRetryMax = Integer.parseInt(
                    prop.getProperty("healing.retry.max", "3")
            );
            healingSpringAiUrl = prop.getProperty(
                    "healing.springai.url",
                    "http://localhost:8081/api/heal"
            );

        } catch (Exception e) {
            System.out.println("Unable to load framework.properties. Using defaults.");
        }
    }

    public static boolean isBaselineMode() {
        return baselineMode;
    }

    public static boolean isCaptureOnFirstRun() {
        return captureOnFirstRun;
    }

    public static boolean isSelfHealingEnabled() {
        return selfHealingEnabled;
    }

    public static boolean isHealingCaptureEnabled() {
        return healingCaptureEnabled;
    }

    public static boolean isHealingModeEnabled() {
        return healingModeEnabled;
    }

    public static int getHealingRetryMax() {
        return Math.max(1, healingRetryMax);
    }

    public static String getHealingSpringAiUrl() {
        return healingSpringAiUrl;
    }
}
