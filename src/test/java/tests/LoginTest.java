package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import tests.pages.BasePage;
import tests.pages.LoginPage;

public class LoginTest extends BaseTest {
    WebDriver driver;
    @Test
    public void loginTest() throws InterruptedException {

        driver = new ChromeDriver();
        driver.get("https://www.google.com");

        LoginPage loginPage = new LoginPage(driver);

        loginPage.clickGmail();
    }
}
