package tests.pages;

import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import selfhealing.locator.NamedBy;

public class LoginPage extends BasePage {

    /* ================= LOCATORS ================= */

    private final NamedBy Obj_LoginBtn =
            new NamedBy("Obj_LoginBtn",
                    By.xpath("//button[@type='submit and Save']"));

//    private final NamedBy Obj_RejectCoockies =
//            new NamedBy("Obj_RejectCoockies",
//                    By.xpath("//button[contains(text(),'Reject All')]"));




    /* ================= CONSTRUCTOR ================= */

    public LoginPage(WebDriver driver,
                     LocatorMetaService locatorMetaService) {
        super(driver, locatorMetaService);
    }

    /* ================= PAGE ACTIONS ================= */

    public void loginTestOrangeHRM() {

        find(Obj_LoginBtn).click();


    }

}