package tests.pages;

import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import selfhealing.locator.NamedBy;

public class LoginPage extends BasePage {

    /* ================= LOCATORS ================= */

    private final NamedBy Obj_Vodafone_OurCompanyBtn =
            new NamedBy("Obj_Orangehrm_login_button",
                    By.xpath("//button[@type='sub']"));




    /* ================= CONSTRUCTOR ================= */

    public LoginPage(WebDriver driver,
                     LocatorMetaService locatorMetaService) {
        super(driver, locatorMetaService);
    }

    /* ================= PAGE ACTIONS ================= */

    public void loginTestOrangeHRM() {

        find(Obj_Vodafone_OurCompanyBtn).click();
    }

}