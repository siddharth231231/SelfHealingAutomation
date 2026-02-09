package base;

import com.yourcompany.selfhealing.SelfHealingApplication;
import config.FrameworkConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
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

        FrameworkConfig.load();

        WebDriverManager.chromedriver().setup();
        driver.set(new ChromeDriver());
        getDriver().manage().window().maximize();
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
        }
    }
}
