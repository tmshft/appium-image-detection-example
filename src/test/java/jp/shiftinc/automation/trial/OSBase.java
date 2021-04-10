package jp.shiftinc.automation.trial;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

abstract class OSBase {
    abstract String resourceDir();
    abstract By loginId();
    abstract By loginPwd();
    abstract DesiredCapabilities readCapabilities();
}
