package tests.pages;


import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.context.storedContext.DbExtractedData;
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
                System.out.println(storedData.toString());   // full entity

                // Focused log for parent & sibling information
                System.out.println("--- Parent & Sibling from DB ---");
                System.out.println("Parent XPath        : " + storedData.getParentXpath());
                System.out.println("Parent XPath Chain  : " + storedData.getParentXpathChain());
                System.out.println("Sibling XPaths      : " + storedData.getSiblingXpaths());
                System.out.println("Sibling XPathCluster: " + storedData.getSiblingXpathCluster());
                System.out.println("================================");

                // Populate shared stored context for DOM capture
                DbExtractedData.set(DbExtractedData.fromEntity(storedData));

            } else {
                System.out.println("No DB record found for this locator.");
            }

            throw e;
        }
    }
}