package config;

import java.io.FileInputStream;
import java.util.Properties;

public class FrameworkConfig {

    private static boolean baselineMode = false;
    private static boolean captureOnFirstRun = false;
    private static boolean selfHealingEnabled = true;

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
}
