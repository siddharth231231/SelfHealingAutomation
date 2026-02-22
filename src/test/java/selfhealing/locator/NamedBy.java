package selfhealing.locator;

import org.openqa.selenium.By;

public class NamedBy {

    private String elementName;
    private By by;

    public NamedBy(String elementName, By by) {
        this.elementName = elementName;
        this.by = by;
    }

    public String getElementName() {
        return elementName;
    }

    public By getBy() {
        return by;
    }
}