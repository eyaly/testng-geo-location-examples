package tests.rdc.app;


import com.google.common.collect.ImmutableMap;
import helpers.Utils;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import helpers.Utils.*;

import static helpers.Utils.*;

public class LocationIosRdcAppTest {

    private static ThreadLocal<IOSDriver> iosDriver = new ThreadLocal<IOSDriver>();
    private static ThreadLocal<Boolean> isFirstRun = new ThreadLocal<Boolean>();

    LocationIosRdcAppTest(){
        System.out.println("Sauce iOS Native - In the LocationIosRdcAppTest ctor");
        isFirstRun.set(Boolean.TRUE);
    }

    @BeforeMethod
    public void setup(Method method) throws MalformedURLException {

        System.out.println("Sauce iOS Native - BeforeMethod hook");
        String username = System.getenv("SAUCE_USERNAME");
        String accesskey = System.getenv("SAUCE_ACCESS_KEY");

        String sauceUrl = "@ondemand.eu-central-1.saucelabs.com:443";
        String SAUCE_REMOTE_URL = "https://" + username + ":" + accesskey + sauceUrl +"/wd/hub";

        String methodName = method.getName();
        URL url = new URL(SAUCE_REMOTE_URL);

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("deviceName", "iPhone.*");
        capabilities.setCapability("platformVersion", "14");
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("automationName", "XCuiTest");
        capabilities.setCapability("name", methodName);
        capabilities.setCapability("app", "https://github.com/saucelabs/sample-app-mobile/releases/download/2.7.1/iOS.RealDevice.SauceLabs.Mobile.Sample.app.2.7.1.ipa");

        capabilities.setCapability("noReset", false);
        capabilities.setCapability("cacheId", "1234");

        //capabilities.setCapability("autoAcceptAlerts", true);

        iosDriver.set(new IOSDriver(url, capabilities));
        getiosDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        // If this is the first execution on the device
        if (isFirstRun.get()) {
            System.out.println("Sauce - Check Location settings");
            getiosDriver().executeScript("mobile: pressButton", ImmutableMap.of("name", "home"));
            // preferences app
            Utils utils = new Utils();
            utils.enableIosLocationServices(getiosDriver());
            isFirstRun.set(Boolean.FALSE);

            // back to the native app
            getiosDriver().launchApp();
        }
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        System.out.println("Sauce - AfterMethod hook");
        ((JavascriptExecutor)getiosDriver()).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
        getiosDriver().quit();
    }

    public  IOSDriver getiosDriver() {
        return iosDriver.get();
    }

    @Test
    public void setLocationEiffelTower() {

        System.out.println("Sauce - Start setLocation test");

        login("standard_user", "secret_sauce");
        goToGeoLocationMenu();

        waiting(2);
        setGeoLocation(48.8584,  2.2945);
        // wait 5 sec to update with the changes
        waiting(5);
    }

    @Test
    public void setLocationTowerBridge() {

        System.out.println("Sauce - Start setLocation test");

        login("standard_user", "secret_sauce");
        goToGeoLocationMenu();

        waiting(2);
        setGeoLocation(51.5055,  -0.0754);
        // wait 5 sec to update with the changes
        waiting(5);
    }

    public void login(String user, String pass){
        String usernameID = "test-Username";
        String passwordID = "test-Password";
        String submitButtonID = "test-LOGIN";
        IOSDriver driver = getiosDriver();

        WebElement usernameEdit = (WebElement) driver.findElementByAccessibilityId(usernameID);
        usernameEdit.click();
        usernameEdit.sendKeys(user);

        WebElement passwordEdit = (WebElement) driver.findElementByAccessibilityId(passwordID);
        passwordEdit.click();
        passwordEdit.sendKeys(pass);

        WebElement submitButton = (WebElement) driver.findElementByAccessibilityId(submitButtonID);
        submitButton.click();
    }

    public void goToGeoLocationMenu(){
        By testMenu = By.name("test-Menu");
        By testGeoLocationItem = By.name("test-GEO LOCATION");
        IOSDriver driver = getiosDriver();

        driver.findElement(testMenu).click();
        driver.findElement(testGeoLocationItem).click();

        String platformVersion = driver.getCapabilities().getCapability("platformVersion").toString();
        // version can be 12.3.1 so it is not long number. Need to get only the main vrsion (12 in the example)
        String mainPlatformVersion = platformVersion.split("\\.")[0];
        System.out.println("platform version is: " + platformVersion );
        if (Integer.valueOf(mainPlatformVersion) < 13) {
            // can be alert with 2 options
            try {
                driver.switchTo().alert().accept();
            } catch (NoAlertPresentException e) {
                System.out.println("Alert is not present" + e.getMessage());
            }
        } else {
          // new alert with 3 options
            try {
                WebDriverWait wait = new WebDriverWait(driver, 2);

                final WebElement alertAllow = wait.until(ExpectedConditions.visibilityOfElementLocated(new MobileBy.ByAccessibilityId("Allow While Using App")));
                alertAllow.click();
            } catch (NoAlertPresentException e) {
                System.out.println("Alert is not present" + e.getMessage());
            }

        }
    }

    public void setGeoLocation(double latitude, double longitude){
        IOSDriver driver = getiosDriver();
        driver.setLocation(new Location(latitude, longitude, 0.0));
    }

}