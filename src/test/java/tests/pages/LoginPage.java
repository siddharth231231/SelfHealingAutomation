package tests.pages;

import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import selfhealing.locator.NamedBy;

public class LoginPage extends BasePage {

    /* ================= LOCATORS ================= */

    private final NamedBy loginBtn =
            new NamedBy("Obj_Orangehrm_username_textbox",
                    By.xpath("//input[@name='name']"));

    /* ================= CONSTRUCTOR ================= */

    public LoginPage(WebDriver driver,
                     LocatorMetaService locatorMetaService) {
        super(driver, locatorMetaService);
    }

    /* ================= PAGE ACTIONS ================= */

    public void loginTestOrangeHRM() {
        find(loginBtn).click();
    }

}