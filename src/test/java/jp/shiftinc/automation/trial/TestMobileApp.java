package jp.shiftinc.automation.trial;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.imagecomparison.OccurrenceMatchingOptions;
import io.appium.java_client.imagecomparison.OccurrenceMatchingResult;
import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings({"SameParameterValue", "FieldCanBeLocal"})
@Feature("Appiumイメージセレクタテスト")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestMobileApp {
    private AppiumDriver driver;
    private final String resourceDir = "src/test/resources/image/";

    By loginId() {return By.xpath("//XCUIElementTypeTextField[1]");}
    By loginPwd() {return By.xpath("//XCUIElementTypeSecureTextField[1]");}

   @BeforeAll
    void setupClass() {
        driver = new AppiumDriver(url(), readCapabilities());
        driver.launchApp();
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

        MobileElement loginId = (MobileElement) driver.findElement(loginId());
        MobileElement loginPwd = (MobileElement) driver.findElement(loginPwd());
        loginId.sendKeys("demo");
        loginPwd.click();
        loginPwd.sendKeys("demo");
    }


    URL url(){
        try {
            return new URL("http://127.0.0.1:4723/wd/hub");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    DesiredCapabilities readCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("platformVersion", "14.0");
        capabilities.setCapability("deviceName", "iPhone 11 Pro Max");
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("language", "ja");
        capabilities.setCapability("locale", "ja_JP");
        capabilities.setCapability("newCommandTimeout", 1000);
        capabilities.setCapability("app", "sut_app/app/racinesut.zip");
        capabilities.setCapability("settings[getMatchedImageResult]", true);
        capabilities.setCapability("settings[imageMatchThreshold]", 0.8);
        return  capabilities;
    }

    @Step("イメージセレクタでログイン")
    @Test
    void imageSelectorTest() throws IOException, InterruptedException {
        // イメージセレクタ
        WebElement loginElm = driver.findElementByImage(getReferenceImageB64("racine_sut_login.png"));
        // visualization
        String imageResult = loginElm.getAttribute("visual");
        // Allureにアタッチ(サービス関数経由）
        addAttachment(Base64.getDecoder().decode(imageResult), "image-selector");
        loginElm.click();

        Thread.sleep(10000);
        // テンプレートマッチング
        OccurrenceMatchingResult result = templateMatch("original_template.png");
        // Allureにアタッチ(サービス関数経由）
        addAttachment(Base64.getDecoder().decode(result.getVisualization()), "template-matched-result");

        assertNotNull(result.getRect());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.closeApp();
        }
    }

    private String getReferenceImageB64(String imageName) throws IOException {
        BufferedImage image = ImageIO.read(new File(resourceDir + imageName));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return Base64.getEncoder().encodeToString(os.toByteArray());
    }

    private OccurrenceMatchingResult templateMatch(String template) throws IOException {
        return driver
                .findImageOccurrence(
                        driver.getScreenshotAs(OutputType.FILE),
                        new File(resourceDir + template),
                        new OccurrenceMatchingOptions()
                                .withThreshold(0.6)
                                .withEnabledVisualization());
    }

    private static void addAttachment(byte[] byteArray, String imageName) {
        Allure.addAttachment(imageName, "image/png", new ByteArrayInputStream(byteArray), "png");
    }
}
