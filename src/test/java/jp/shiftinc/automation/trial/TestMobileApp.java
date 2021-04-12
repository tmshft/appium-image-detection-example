package jp.shiftinc.automation.trial;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.imagecomparison.OccurrenceMatchingOptions;
import io.appium.java_client.imagecomparison.OccurrenceMatchingResult;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import io.appium.java_client.screenrecording.CanRecordScreen;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import java.nio.file.Files;
import java.nio.file.Paths;
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
        //addAttachment(driver.getScreenshotAs(OutputType.BYTES),"img1");
        // テンプレートマッチング
        OccurrenceMatchingResult result = templateMatch("original_template.png");
        // Allureにアタッチ(サービス関数経由）
        addAttachment(Base64.getDecoder().decode(result.getVisualization()), "template-matched-result");
        assertNotNull(result.getRect());
    }

    Stream<Arguments> osMartix() {
        return Stream.of(
                arguments("ios"),
                arguments("android")
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        if (driver != null) {
            String base64Video = ((CanRecordScreen) driver).stopRecordingScreen();
            String path = String.format("./build/test_%s.mp4",osInfo.osName());
            Files.write(Paths.get(path), Base64.getDecoder().decode(base64Video));
            attachVideo(path);
            driver.closeApp();
        }
    }

    private void setup(String os) {
        osInfo =  os.equals("ios")? new IOSBase():new AndroidBase();
        driver = osInfo.driver();
        driver.launchApp();
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
        if (os.equals("ios")) {
            ((IOSBase)osInfo).iosDriver.startRecordingScreen(
                    new IOSStartScreenRecordingOptions()
                            .withVideoType("libx264")
                            .withVideoQuality(IOSStartScreenRecordingOptions.VideoQuality.MEDIUM)
            );
        } else {
            ((AndroidBase)osInfo).androidDriver.startRecordingScreen();
        }
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

    @SuppressWarnings("UnusedReturnValue")
    @Attachment(value = "video",type="video/mp4",fileExtension = "mp4")
    private byte[] attachVideo(String path) throws IOException {
        return getFile(path);
    }

    private byte[] getFile(String fileName) throws IOException {
        File file = new File(fileName);
        return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
    }
}
