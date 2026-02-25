package base;

import com.yourcompany.selfhealing.SelfHealingApplication;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.*;

public class BaseTest {

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ConfigurableApplicationContext springContext;

    protected LocatorMetaService locatorMetaService;

    /* ================= SPRING BOOT ================= */

    @BeforeSuite(alwaysRun = true)
    public void startSpring() {
        if (springContext == null) {
            springContext = SpringApplication.run(SelfHealingApplication.class);
            System.out.println("Spring Boot context started");
        }
    }

    @BeforeClass(alwaysRun = true)
    public void initService() {
        locatorMetaService = springContext.getBean(LocatorMetaService.class);
        System.out.println("LocatorMetaService initialized");
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    /* ================= WEBDRIVER ================= */

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        WebDriver webDriver = new ChromeDriver();
        driver.set(webDriver);
        webDriver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
//        WebDriver webDriver = driver.get();
//        if (webDriver != null) {
//            webDriver.quit();
//            driver.remove();
//        }
  }
}