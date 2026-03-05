package tests.pages;

import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import selfhealing.locator.NamedBy;

public class LoginPage extends BasePage {

    /* ================= LOCATORS ================= */

    private final NamedBy Obj_Sustainablebusiness_Btn =
            new NamedBy("Obj_Sustainablebusiness_Btn",
                    By.xpath("//a[@aria-label='Link to Sustainable']"));

    private final NamedBy Obj_RejectCoockies =
            new NamedBy("Obj_RejectCoockies",
                    By.xpath("//button[@id='abc']"));

    private final NamedBy Obj_VbButton =
            new NamedBy("Obj_VBBtn",
                    By.xpath("//a[text()='Vodafone' and@ title='nbvvnbvb']"));

    private final NamedBy Obj_InvestorBtnNavbar =
            new NamedBy("Obj_InvestorBtnNavbar",
                    By.xpath("//span[text()='Stakeholder']"));



    /* ================= CONSTRUCTOR ================= */

    public LoginPage(WebDriver driver,
                     LocatorMetaService locatorMetaService) {
        super(driver, locatorMetaService);
    }

    /* ================= PAGE ACTIONS ================= */

    public void loginTestOrangeHRM() {

        click(Obj_RejectCoockies);
       // click(Obj_InvestorBtnNavbar);
       // click(Obj_Sustainablebusiness_Btn);



    }

}
