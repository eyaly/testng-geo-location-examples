package tests.all;


import helpers.Utils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.decimal4j.util.DoubleRounder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static helpers.Config.host;
import static helpers.Config.region;
import static helpers.Utils.waiting;
import static org.assertj.core.api.Assertions.assertThat;

public class LocationAndroidWebTest {

    private static ThreadLocal<AndroidDriver> androidDriver = new ThreadLocal<AndroidDriver>();

    @BeforeMethod
    public void setup(Method method) throws MalformedURLException {

        System.out.println("Sauce Android Native - BeforeMethod hook");
        String methodName = method.getName();
        URL url;

        String username = System.getenv("SAUCE_USERNAME");
        String accesskey = System.getenv("SAUCE_ACCESS_KEY");
        String sauceUrl;
        if (region.equalsIgnoreCase("eu")) {
            sauceUrl = "@ondemand.eu-central-1.saucelabs.com:443";
        } else {
            sauceUrl = "@ondemand.us-west-1.saucelabs.com:443";
        }
        String SAUCE_REMOTE_URL = "https://" + username + ":" + accesskey + sauceUrl + "/wd/hub";
        url = new URL(SAUCE_REMOTE_URL);

        String deviceName = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("deviceName");
        String platformVersion = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("platformVersion");
        String appiumVersion = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("appiumVersion");
        String cacheId = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("cacheId");

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("deviceName", deviceName == null ? "Samsung.*" : deviceName);
        capabilities.setCapability("platformVersion", platformVersion == null ? "11" : platformVersion);
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("orientation", "PORTRAIT");

        capabilities.setCapability("browserName", "Chrome");
        capabilities.setCapability("name", methodName);

        if (appiumVersion !=  null) {
            capabilities.setCapability("appiumVersion", appiumVersion);
        }

        if (cacheId !=  null) {
            capabilities.setCapability("noReset", false);
            capabilities.setCapability("cacheId", cacheId);
        }


        // Grant permission for alert popups
        capabilities.setCapability("autoGrantPermissions", true);

        androidDriver.set(new AndroidDriver(url, capabilities));
        getAndroidDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        System.out.println("Sauce - AfterMethod hook");
        try {
            if (host.equals("sauce")) {
                ((JavascriptExecutor) getAndroidDriver()).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
            }
        } finally {
            getAndroidDriver().quit();
        }

    }

    public  AndroidDriver getAndroidDriver() {
        return androidDriver.get();
    }

    @Test
    public void setLocationMetro() {

        System.out.println("Sauce - Start setLocationEiffelTower test");
        AppiumDriver driver = getAndroidDriver();
        navigateToRoyalMail(getAndroidDriver());
        // change location to Tower Bridge
        setGeoLocation(getAndroidDriver(), 51.5055,  -0.0754);

    }

    public void navigateToRoyalMail(AppiumDriver driver) {

        driver.get("https://www.metrobankonline.co.uk/store-locator/");

        // Get the current context
        String currentWebContext = driver.getContext();

        WebDriverWait wait = new WebDriverWait(driver, 2);
        // check cookie Notice
        try {
            final WebElement cookieNoticeBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#js-mbCookieNotice-button")));
            cookieNoticeBtn.click();
        } catch (Exception e){
                // Do nothing - the popup dialog doesn't exist
                System.out.println("cookie notice button doeesn't displayed" + e.getMessage());
        }

        // click on the geo location  button
        getAndroidDriver().findElementByCssSelector(".map-buttons-overlay .geolocator").click();

        // change context to native to allow Appium to handle the alerts popups
        driver.context("NATIVE_APP");

        String deviceApilovel = driver.getCapabilities().getCapability("deviceApiLevel").toString();
        System.out.println("Sauce - Device API level is:"  + deviceApilovel);

        // check alert "metrobank wants to use your device location" alert
        // for some android emulators version 8,9. The  id  of  the allow is "android:id/button1" - I didn't handle these cases
        try {
            final WebElement metroDeviceLocationAllow = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("com.android.chrome:id/positive_button")));
            metroDeviceLocationAllow.click();
        } catch (Exception e){
            // Do nothing - the popup dialog doesn't exist
            System.out.println("cookie notice button doeesn't displayed" + e.getMessage());
        }

         //for api < 29 (android 9 and below)
        if (Long.valueOf(deviceApilovel) < 29) {
         //permission popup with 2 options
            try {
                final WebElement allowBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("com.android.packageinstaller:id/permission_allow_button")));
                allowBtn.click();
            } catch (Exception e){
                // Do nothing - the popup dialog doesn't exist
                System.out.println("Alert is not present" + e.getMessage());
            }
        } else { // for api >= 29 (android 10 and above)

         //permission popup with 3 options
            try {
                final WebElement allowBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("com.android.permissioncontroller:id/permission_allow_foreground_only_button")));
                allowBtn.click();
            } catch (Exception e){
                // Do nothing - the popup dialog doesn't exist
                System.out.println("Alert is not present" + e.getMessage());
            }
        }

        // To enable the App if the location service are disabled
        // dialog: For a better experience, turn on device location, which uses Google’s location service.
        try {
            final WebElement BtnOK = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("android:id/button1")));
            BtnOK.click();
        } catch (Exception e){
            // Do nothing - the popup dialog doesn't exist
            System.out.println("Alert to turn on device location, is not present" + e.getMessage());
        }

        // switch back to previous context (web)
        driver.context(currentWebContext);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // This  will scroll down the page by  200 pixel vertical
        js.executeScript("window.scrollBy(0,200)");
    }

    public void setGeoLocation(AppiumDriver driver, double latitude, double longitude) {

        // to see in the video the map before
        waiting(2);

        driver.setLocation(new Location(latitude, longitude, 0.0));
        // click on the geo location  button
        getAndroidDriver().findElementByCssSelector(".map-buttons-overlay .geolocator").click();
        // click on the map view
        getAndroidDriver().findElementByCssSelector(".radioSwitch-label_first").click();
        // wait to update with the changes and see on the video
        waiting(2);
    }

}