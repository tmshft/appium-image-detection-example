package jp.shiftinc.automation.trial;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

class IOSBase extends OSBase {
    IOSDriver<WebElement> iosDriver;

    @Override
    public String osName() {return "ios";}

    @Override
    public AppiumDriver driver() {
        iosDriver = new IOSDriver<>(url(), readCapabilities());
        return iosDriver;
    }

    @Override
    public String resourceDir() {return "src/test/resources/image/ios/";}

    @Override
    public By loginId() {return By.xpath("//android.widget.EditText[1]");}
    @Override
    public By loginPwd() {return By.xpath("//android.widget.EditText[2]");}

    @Override
    DesiredCapabilities readCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("platformVersion", "13.2");
        capabilities.setCapability("deviceName", "iPhone 11 Pro Max");
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("newCommandTimeout", 1000);
        capabilities.setCapability("app", "sut_app/app/racinesut.zip");
        capabilities.setCapability("settings[getMatchedImageResult]", true);
        capabilities.setCapability("settings[imageMatchThreshold]", 0.8);
        return  capabilities;
    }
}
