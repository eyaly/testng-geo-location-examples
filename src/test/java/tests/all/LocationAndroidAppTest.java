package tests.all;


import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.decimal4j.util.DoubleRounder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public class LocationAndroidAppTest {

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

        capabilities.setCapability("app", "https://github.com/saucelabs/sample-app-mobile/releases/download/2.7.1/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk");
        capabilities.setCapability("appWaitActivity", "com.swaglabsmobileapp.MainActivity");
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
    public void setLocationEiffelTower() {
        AndroidDriver driver = getAndroidDriver();
        System.out.println("Sauce - Start setLocation test");

        login("standard_user", "secret_sauce");

        selectGeoLocationMenu();

        waiting(2);
        setGeoLocation(48.8584,  2.2945);
    }

    @Test
    public void setLocationTowerBridge() {
        AndroidDriver driver = getAndroidDriver();
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

        AndroidDriver driver = getAndroidDriver();
        WebDriverWait wait = new WebDriverWait(driver, 3);

        final WebElement usernameEdit = wait.until(ExpectedConditions.visibilityOfElementLocated(new MobileBy.ByAccessibilityId(usernameID)));
        usernameEdit.click();
        usernameEdit.sendKeys(user);

        WebElement passwordEdit = (WebElement) driver.findElementByAccessibilityId(passwordID);
        passwordEdit.click();
        passwordEdit.sendKeys(pass);

        WebElement submitButton = (WebElement) driver.findElementByAccessibilityId(submitButtonID);
        submitButton.click();
    }

    public void selectGeoLocationMenu(){
        String testMenuName = "test-Menu";
        String geoLocationName = "test-GEO LOCATION";

        AndroidDriver driver = getAndroidDriver();

        // click on the menu
        WebElement testMenu = (WebElement) driver.findElementByAccessibilityId(testMenuName);
        testMenu.click();

        // click on GEO Location
        WebElement geoLocationMenu = (WebElement) driver.findElementByAccessibilityId(geoLocationName);
        geoLocationMenu.click();

        // To enable the App in the location service
        // dialog: For a better experience, turn on device location, which uses Google???s location service.
        try {
            WebDriverWait wait = new WebDriverWait(driver, 2);
            final WebElement BtnOK = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("android:id/button1")));
            BtnOK.click();
        } catch (Exception e){
            // Do nothing - the popup dialog doesn't exist
            System.out.println("Alert to turn on device location, is not present" + e.getMessage());
        }
    }

    public void setGeoLocation(double latitude, double longitude){
        String latitudeLocator = "test-latitude";
        String longitudeLocator = "test-longitude";

        AndroidDriver driver = getAndroidDriver();
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