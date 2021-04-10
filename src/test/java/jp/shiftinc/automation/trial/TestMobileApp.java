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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SuppressWarnings({"SameParameterValue", "FieldCanBeLocal"})
@Feature("Appiumイメージセレクタテスト")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestMobileApp {
    private AppiumDriver driver;
    private OSBase osInfo;

    @BeforeAll
    void setupClass() {
    }

    URL url(){
        try {
            return new URL("http://127.0.0.1:4723/wd/hub");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Step("{osName}／イメージセレクタでログイン")
    @ParameterizedTest
    @MethodSource("osMartix")
    void imageSelectorTest(String osName) throws IOException, InterruptedException {
        setup(osName);

        // イメージセレクタ
        WebElement loginElm = driver.findElementByImage(getReferenceImageB64("racine_sut_login.png"));
        // visualization
        String imageResult = loginElm.getAttribute("visual");
        // Allureにアタッチ(サービス関数経由）
        addAttachment(Base64.getDecoder().decode(imageResult), "image-selector");
        loginElm.click();

        Thread.sleep(10000);
        addAttachment(driver.getScreenshotAs(OutputType.BYTES),"img1");
        // テンプレートマッチング
        OccurrenceMatchingResult result = templateMatch("original_template.png");
        // Allureにアタッチ(サービス関数経由）
        addAttachment(Base64.getDecoder().decode(result.getVisualization()), "template-matched-result");
        assertNotNull(result.getRect());
    }

    Stream<Arguments> osMartix() {
        return Stream.of(
                arguments("android"),
                arguments("ios")
        );
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.closeApp();
        }
    }

    private void setup(String os) {
        osInfo =  os.equals("ios")? new IOSBase():new AndroidBase();
        driver = new AppiumDriver(url(), osInfo.readCapabilities());
        driver.launchApp();
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

        MobileElement loginId = (MobileElement) driver.findElement(osInfo.loginId());
        MobileElement loginPwd = (MobileElement) driver.findElement(osInfo.loginPwd());
        loginId.sendKeys("demo");
        loginPwd.click();
        loginPwd.sendKeys("demo");
    }

    private String getReferenceImageB64(String imageName) throws IOException {
        BufferedImage image = ImageIO.read(new File(osInfo.resourceDir() + imageName));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return Base64.getEncoder().encodeToString(os.toByteArray());
    }

    private OccurrenceMatchingResult templateMatch(String template) throws IOException {
        return driver
                .findImageOccurrence(
                        driver.getScreenshotAs(OutputType.FILE),
                        new File(osInfo.resourceDir() + template),
                        new OccurrenceMatchingOptions()
                                .withThreshold(0.6)
                                .withEnabledVisualization());
    }

    private static void addAttachment(byte[] byteArray, String imageName) {
        Allure.addAttachment(imageName, "image/png", new ByteArrayInputStream(byteArray), "png");
    }
}
