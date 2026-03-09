package tests;

import base.BaseTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import tests.pages.LoginPage;

public class LoginTest extends BaseTest {

    @Test
    public void loginTest() {
        WebDriver driver = getDriver();
        LoginPage loginPage =
                new LoginPage(driver, locatorMetaService);
        loginPage.LoginVodafone();






    }
}