package base;

import com.yourcompany.selfhealing.SelfHealingApplication;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ConfigurableApplicationContext springContext;

    /* ================= SPRING BOOT ================= */

    @BeforeSuite(alwaysRun = true)
    public void startSpring() {
        if (springContext == null) {
            springContext = SpringApplication.run(SelfHealingApplication.class);
            System.out.println("Spring Boot context started");
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return springContext.getBean(clazz);
    }

    /* ================= WEBDRIVER ================= */

    @BeforeMethod
    public void setup() {

        WebDriver webDriver = new ChromeDriver();  // create normal driver
        driver.set(webDriver);                     // set into ThreadLocal

        webDriver.get("https://www.google.com");
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    @AfterMethod
    public void tearDown() {

        WebDriver webDriver = driver.get();

        if (webDriver != null) {
            webDriver.quit();
            driver.remove();   // VERY IMPORTANT for parallel safety
        }
    }
}