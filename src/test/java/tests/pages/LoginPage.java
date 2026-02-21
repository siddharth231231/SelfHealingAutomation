package tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import selfhealing.locator.NamedBy;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    private NamedBy gmailButton =
            new NamedBy("Gmail Button",
                    By.xpath("//a[@aria-label='Gmail and save']"));

    public void clickGmail() {
        find(gmailButton).click();
    }
}