package config;

import java.io.FileInputStream;
import java.util.Properties;

public class FrameworkConfig {

    private static boolean baselineMode = false;

    public static void load() {
        try {
            Properties prop = new Properties();
            FileInputStream fis =
                    new FileInputStream("src/test/resources/framework.properties");
            prop.load(fis);

            baselineMode = Boolean.parseBoolean(
                    prop.getProperty("baseline", "false")
            );

        } catch (Exception e) {
            System.out.println("Unable to load framework.properties. Using defaults.");
        }
    }

    public static boolean isBaselineMode() {
        return baselineMode;
    }
}
