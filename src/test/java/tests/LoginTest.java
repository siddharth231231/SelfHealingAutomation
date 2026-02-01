package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test
    public void loginTest() {

        // Always get driver via getter
        getDriver().get("https://google.com");

        // Intentionally broken XPath (for self-healing later)
        getDriver().findElement(By.xpath("//a[@aria-label='Gmai ']")).click();
    }
}
