package base;

import com.yourcompany.selfhealing.SelfHealingApplication;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import config.FrameworkConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.*;

import java.time.Duration;

public class BaseTest {

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ConfigurableApplicationContext springContext;

    protected LocatorMetaService locatorMetaService;

    /* ================= SPRING BOOT ================= */

    @BeforeSuite(alwaysRun = true)
    public void startSpring() {
        FrameworkConfig.load();
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

        ChromeOptions options = new ChromeOptions();

        // ✅ Disable cookies
        options.addArguments("--disable-cookies");

        // (Optional but recommended for automation stability)
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized"); // Alternative way to maximize

        WebDriver webDriver = new ChromeDriver(options);
        driver.set(webDriver);

        // ✅ If you prefer explicit maximize instead of argument:
        webDriver.manage().window().maximize();

        // ✅ Add implicit wait (5 sec)
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        webDriver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        //webDriver.findElement(By.xpath("//button[contains(text(),'Reject All')]")).click();
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
