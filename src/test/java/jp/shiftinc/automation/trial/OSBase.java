package jp.shiftinc.automation.trial;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

abstract class OSBase {
    abstract String osName();
    abstract String resourceDir();
    abstract public AppiumDriver driver();
    abstract By loginId();
    abstract By loginPwd();
    abstract DesiredCapabilities readCapabilities();

    URL url(){
        try {
            return new URL("http://127.0.0.1:4723/wd/hub");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
