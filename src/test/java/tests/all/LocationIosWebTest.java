package tests.all;


import com.google.common.collect.ImmutableMap;
import helpers.Utils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
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
import io.appium.java_client.MobileBy;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static helpers.Config.region;
import static helpers.Utils.waiting;


public class LocationIosWebTest {

    private static ThreadLocal<IOSDriver> iosDriver = new ThreadLocal<IOSDriver>();
    private static ThreadLocal<Boolean> isFirstRun = new ThreadLocal<Boolean>();

    @BeforeMethod
    public void setup(Method method) throws MalformedURLException {
        if (isFirstRun.get() == null){
            isFirstRun.set(Boolean.TRUE);
        }

        System.out.println("Sauce iOS Native - BeforeMethod hook");
        String username = System.getenv("SAUCE_USERNAME");
        String accesskey = System.getenv("SAUCE_ACCESS_KEY");

        String sauceUrl;
        if (region.equalsIgnoreCase("eu")) {
            sauceUrl = "@ondemand.eu-central-1.saucelabs.com:443";
        } else {
            sauceUrl = "@ondemand.us-west-1.saucelabs.com:443";
        }
        String SAUCE_REMOTE_URL = "https://" + username + ":" + accesskey + sauceUrl +"/wd/hub";

        String methodName = method.getName();
        URL url = new URL(SAUCE_REMOTE_URL);

        boolean isRDC = true;
        String deviceName = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("deviceName");
        String platformVersion = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("platformVersion");
        String appiumVersion = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("appiumVersion");
        String cacheId = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("cacheId");

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("deviceName", deviceName == null ? "iPhone.*" : deviceName);
        capabilities.setCapability("platformVersion", platformVersion == null ? "14" : platformVersion);
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("automationName", "XCuiTest");
        capabilities.setCapability("name", methodName);
        capabilities.setCapability("browserName", "Safari");

        if (appiumVersion !=  null) {
            capabilities.setCapability("appiumVersion", appiumVersion);
        }

        if (cacheId !=  null) {
            capabilities.setCapability("noReset", false);
            capabilities.setCapability("cacheId", cacheId);
        }

        if (deviceName.contains("Simulator")) {
            isRDC = false;
        }

        iosDriver.set(new IOSDriver(url, capabilities));
        getIosDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        // for simulator - By default the location services is enabled
        if (isRDC) {
            // If this is the first execution on the device
            if (isFirstRun.get()) {
                System.out.println("Sauce - Check Location settings");
                getIosDriver().executeScript("mobile: pressButton", ImmutableMap.of("name", "home"));
                // preferences app
                Utils utils = new Utils();
                utils.enableIosLocationServices( getIosDriver());
                isFirstRun.set(Boolean.FALSE);
                // back to the app under test
                getIosDriver().launchApp();
            }
        }
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        System.out.println("Sauce - AfterMethod hook");
        ((JavascriptExecutor)getIosDriver()).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
        getIosDriver().quit();
    }

    public  IOSDriver getIosDriver() {
        return iosDriver.get();
    }

    @Test
    public void setLocationEiffelTowerMap() {

        System.out.println("Sauce - Start setLocationEiffelTower test");
        AppiumDriver driver = getIosDriver();

        // maps web -  go to Eiffel tower paris france
        openMapsWeb(driver);
        setGeoLocation(driver,48.8584,2.2945);

    }

    // ios map web app
    public void openMapsWeb(AppiumDriver driver){

        driver.get("https://maps.google.com");
        // Get the current context
        String currentWebContext = driver.getContext();

        WebDriverWait wait = new WebDriverWait(driver, 2);

        //1. Check if the "Stay on Web" button is presented and click on it (need to be in webview)
        try {
            final WebElement stayOnWebBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ml-promotion-no-thanks")));
            stayOnWebBtn.click();
        } catch (Exception e){
            // Do nothing - the popup dialog doesn't exist
            System.out.println("Stay on Web button is not presented");
        }

        // 2. Click on "your location" to see the location on the map
        WebElement myLocationBtn =
                driver.findElementByClassName("ml-button-my-location-fab");
        myLocationBtn.click();

        // change context to native to allow
        // Appium to handle the alerts popups
        driver.context("NATIVE_APP");


        // First alert from Safari App
        String platformVersion = driver.getCapabilities().getCapability("platformVersion").toString();
        // version can be 12.3.1 so it is not long number. Need to get only the main vrsion (12 in the example)
        String mainPlatformVersion = platformVersion.split("\\.")[0];
        System.out.println("platform version is: " + platformVersion );
        if (Integer.valueOf(mainPlatformVersion) < 13) {
            // can be alert with 2 options
            try {
                driver.switchTo().alert().accept();
            } catch (NoAlertPresentException e) {
                System.out.println("Alert is not presented" + e.getMessage());
            }
        } else {
          // new alert with 3 options
            try {
                final WebElement alertAllow = wait.until(ExpectedConditions.visibilityOfElementLocated(new MobileBy.ByAccessibilityId("Allow While Using App")));
                alertAllow.click();
            } catch (Exception e) {
                System.out.println("Alert is not presented" + e.getMessage());
            }
        }

        // Second alert from google App
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            System.out.println("Alert is not presented" + e.getMessage());
        }

        // switch back to previous context (web)
        driver.context(currentWebContext);
    }

    public void setGeoLocation(AppiumDriver driver, double latitude, double longitude) {

        driver.setLocation(new Location(latitude, longitude, 0.0));
        // wait 5 sec to update with the changes and see on the video
        waiting(5);
        // Click on "your location" to see the location on the map
        WebElement myLocationBtn =  driver.findElementByClassName("ml-button-my-location-fab");
        myLocationBtn.click();
        // To see th new location on th video
        waiting(2);
    }

}