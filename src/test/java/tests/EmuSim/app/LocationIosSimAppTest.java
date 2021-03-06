package tests.EmuSim.app;


import io.appium.java_client.MobileBy;
import io.appium.java_client.ios.IOSDriver;
import org.decimal4j.util.DoubleRounder;
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

import static helpers.Config.host;
import static helpers.Config.region;
import static helpers.Utils.handleiOSMapsApp;
import static helpers.Utils.waiting;
import static org.assertj.core.api.Assertions.assertThat;

public class LocationIosSimAppTest {

    private static ThreadLocal<IOSDriver> iosDriver = new ThreadLocal<IOSDriver>();

    @BeforeMethod
    public void setup(Method method) throws MalformedURLException {

        System.out.println("Sauce iOS Native - BeforeMethod hook");
        String methodName = method.getName();
        URL url;

        DesiredCapabilities capabilities = new DesiredCapabilities();
        if (host.equals("sauce")) {
            System.out.println("Sauce iOS Native - Run on Sauce Simulator");
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

            capabilities.setCapability("deviceName", "iPhone Simulator");
            capabilities.setCapability("platformVersion", "14.0");
            capabilities.setCapability("appiumVersion", "1.18.3");
            capabilities.setCapability("app", "https://github.com/saucelabs/sample-app-mobile/releases/download/2.7.1/iOS.Simulator.SauceLabs.Mobile.Sample.app.2.7.1.zip");
            capabilities.setCapability("name", methodName);
        }
        else {
            System.out.println("Sauce iOS Native - Run on Local Simulator");
            // Run on local Appium Server
            url = new URL("http://localhost:4723/wd/hub");

            capabilities.setCapability("platformVersion", "14.0");
            capabilities.setCapability("deviceName", "iPhone 11");
            capabilities.setCapability("noReset", false);
            capabilities.setCapability("app","/Users/eyalyovel/Documents/sauce/demo/apps/ios/iOS.Simulator.SauceLabs.Mobile.Sample.app.2.7.1.zip");
        }

        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("automationName", "XCuiTest");

        capabilities.setCapability("autoAcceptAlerts", true);

        try{
            iosDriver.set(new IOSDriver(url, capabilities));
        } catch (Exception e) {
            System.out.println("*** Problem to create the iOS driver " + e.getMessage());
            throw new RuntimeException(e);
        }

        // change the setting to accept alerts with 3 options
        // and select the "Allow While Using App" (iOS 13 and above)
        getiosDriver().setSetting("acceptAlertButtonSelector",
                "**/XCUIElementTypeButton[`label == \"Allow While Using App\"`]");
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        System.out.println("Sauce - AfterMethod hook");
        try {
            if (host.equals("sauce")) {
                ((JavascriptExecutor) getiosDriver()).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
            }
        } finally {
            getiosDriver().quit();
        }

    }

    public  IOSDriver getiosDriver() {
        return iosDriver.get();
    }


    @Test
    public void setLocationEiffelTower() {

        System.out.println("Sauce - Start setLocation test");

        login("standard_user", "secret_sauce");
        selectGeoLocationMenu();
        waiting(2);
        setGeoLocation(48.8584,  2.2945);
        // wait 5 sec to update with the changes
        waiting(5);
    }

    @Test
    public void setLocationTowerBridge() {

        System.out.println("Sauce - Start setLocation test");

        login("standard_user", "secret_sauce");
        selectGeoLocationMenu();

        waiting(2);
        setGeoLocation(51.5055,  -0.0754);
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
        waiting(1);
    }

    public void selectGeoLocationMenu(){
        By testMenu = By.name("test-Menu");
        By testGeoLocationItem = By.name("test-GEO LOCATION");
        IOSDriver driver = getiosDriver();

        driver.findElement(testMenu).click();
        driver.findElement(testGeoLocationItem).click();

//        String platformVersion = driver.getCapabilities().getCapability("platformVersion").toString();
//        // version can be 12.3.1 so it is not long number. Need to get only the main vrsion (12 in the example)
//        String mainPlatformVersion = platformVersion.split("\\.")[0];
//        System.out.println("platform version is: " + platformVersion );
//        if (Integer.valueOf(mainPlatformVersion) < 13) {
//            //  alert with 2 options
//            try {
//                driver.switchTo().alert().accept();
//            } catch (NoAlertPresentException e) {
//                System.out.println("Alert is not present" + e.getMessage());
//            }
//        } else {
//            //  alert with 3 options
//            try {
//                WebDriverWait wait = new WebDriverWait(driver, 2);
//
//                final WebElement alertAllow = wait.until(ExpectedConditions.visibilityOfElementLocated(new MobileBy.ByAccessibilityId("Allow While Using App")));
//                alertAllow.click();
//            } catch (Exception e) {
//                System.out.println("Alert is not present" + e.getMessage());
//            }
//        }
    }

    public void setGeoLocation(double latitude, double longitude){
        String latitudeLocator = "test-latitude";
        String longitudeLocator = "test-longitude";

        IOSDriver driver = getiosDriver();
        driver.setLocation(new Location(latitude, longitude, 0.0));

        // wait 2 sec to update with the changes
        waiting(2);

        // Verify
        double actualLatitude = DoubleRounder.round(Double.valueOf(driver.findElementByAccessibilityId(latitudeLocator).getText()),4);
        double actualLongitude = DoubleRounder.round(Double.valueOf(driver.findElementByAccessibilityId(longitudeLocator).getText()),4);
        assertThat(actualLatitude).isEqualTo(latitude).as("Incorrect latitude");
        assertThat(actualLongitude).isEqualTo(longitude).as("Incorrect longitude");
    }

}