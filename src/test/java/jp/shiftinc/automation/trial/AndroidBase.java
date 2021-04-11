package jp.shiftinc.automation.trial;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

class AndroidBase extends OSBase {
    AndroidDriver<WebElement> androidDriver;

    @Override
    public String osName() {return "android";}

    @Override
    public String resourceDir() {return "src/test/resources/image/android/";}

    @Override
    public AppiumDriver driver() {
        androidDriver = new AndroidDriver<>(url(), readCapabilities());
        return androidDriver;
    }

    @Override
    public By loginId() {return By.xpath("//android.widget.EditText[1]");}
    @Override
    public By loginPwd() {return By.xpath("//android.widget.EditText[2]");}

    @Override
    DesiredCapabilities readCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        //capabilities.setCapability("platformVersion", "9.0");
        capabilities.setCapability("deviceName", "Android Emulator");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 1000);
        capabilities.setCapability("app", "sut_app/app/racinesut.apk");
        capabilities.setCapability("settings[getMatchedImageResult]", true);
        capabilities.setCapability("settings[imageMatchThreshold]", 0.8);
        return  capabilities;
    }
}
