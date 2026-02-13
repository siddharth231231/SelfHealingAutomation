package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test
    public void loginTest() throws InterruptedException {

        // Always get driver via getter
        getDriver().get("https://google.com");
        Thread.sleep(2000);
        // Intentionally broken XPath (for self-healing later)
        getDriver().findElement(By.xpath("//a[@aria-label='Gmail and save']")).click();
    }
}
