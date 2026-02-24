package tests.pages;


import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.locator.NamedBy;

import java.util.Optional;

public class BasePage {

    protected WebDriver driver;
    protected LocatorMetaService locatorMetaService;

    public BasePage(WebDriver driver,
                    LocatorMetaService locatorMetaService) {
        this.driver = driver;
        this.locatorMetaService = locatorMetaService;
    }

    protected WebElement find(NamedBy locator) {

        try {
            return driver.findElement(locator.getBy());
        }

        catch (NoSuchElementException e) {
            //String pageUrl = driver.getCurrentUrl();
            String locatorName = locator.getElementName();

            System.out.println(
                    "\n======= LOCATOR FAILURE =======\n" +
                            "Element Name : " + locator.getElementName() + "\n" +
                            "Locator      : " + locator.getBy() + "\n" +
                            "================================"
            );

            Optional<LocatorMetaEntity> locatorStoredData =
                    locatorMetaService.findByLocatorName(locatorName);

            if (locatorStoredData.isPresent()) {

                LocatorMetaEntity storedData = locatorStoredData.get();

                System.out.println("======= DB RECORD FOUND =======");
                System.out.println(storedData.toString());   // 🔥 Prints EVERYTHING
                System.out.println("================================");




            } else {
                System.out.println("No DB record found for this locator.");
            }

            throw e;
        }
    }
}