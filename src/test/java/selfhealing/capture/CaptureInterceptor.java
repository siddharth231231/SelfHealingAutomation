package selfhealing.capture;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.context.dom.ElementSnapshotUtil;
import selfhealing.locator.NamedBy;

public final class CaptureInterceptor {

    private CaptureInterceptor() {
    }

    public static void capture(
            WebDriver driver,
            WebElement element,
            NamedBy locator,
            LocatorMetaService locatorMetaService) {

        if (driver == null || element == null || locator == null || locatorMetaService == null) {
            return;
        }

        LocatorMetaEntity captured = ElementSnapshotUtil.buildEntity(
                driver,
                element,
                locator.getElementName(),
                locator.getBy().toString()
        );
        locatorMetaService.upsertIfDomChanged(captured);
    }
}
