package tests.pages;

import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import selfhealing.locator.NamedBy;

public class LoginPage extends BasePage {

    /* ================= LOCATORS ================= */

    private final NamedBy gmailLink =
            new NamedBy("Obj_GmailBtn_Navbar",
                    By.xpath("//a[@aria-label='Gmail and save']"));

    /* ================= CONSTRUCTOR ================= */

    public LoginPage(WebDriver driver,
                     LocatorMetaService locatorMetaService) {
        super(driver, locatorMetaService);
    }

    /* ================= PAGE ACTIONS ================= */

    public void clickGmail() {
        find(gmailLink).click();
    }

}