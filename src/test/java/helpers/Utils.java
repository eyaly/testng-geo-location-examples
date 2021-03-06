package helpers;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    public static void waiting(int sec){
        try
        {
            Thread.sleep(sec*1000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    // for ios settings
    public void enableIosLocationServices(IOSDriver driver){
        // Launch preferences
        driver.activateApp("com.apple.Preferences");
        driver.context("NATIVE_APP");

        // privacy
        RemoteWebElement settingsTable = (RemoteWebElement)driver.findElementByIosNsPredicate("type == \"XCUIElementTypeTable\"");
        String elementID = settingsTable.getId();

        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("element", elementID);
        scrollObject.put("predicateString", "label == 'Privacy' AND name == 'Privacy' AND type == 'XCUIElementTypeCell'");
        scrollObject.put("direction", "down");
        driver.executeScript("mobile:scroll", scrollObject);

        WebElement privacyBtn = driver.findElementByIosNsPredicate("label == 'Privacy' AND name == 'Privacy' AND type == 'XCUIElementTypeCell'");
        privacyBtn.click();

        // Click on Location Services
        driver.findElementByIosClassChain("**/XCUIElementTypeStaticText[`label == \"Location Services\"`]").click();

        // Location services check
        WebElement locationServicesStatus = driver.findElementByIosClassChain("**/XCUIElementTypeSwitch[`label == \"Location Services\"`]");
        if (locationServicesStatus.getAttribute("value").equals("0")) {
            locationServicesStatus.click();
        }

        // close the Preferences App
        driver.terminateApp("com.apple.Preferences");
    }

    // ios Map app
    public static void handleiOSMapsApp(IOSDriver driver, double latitude, double longitude){
        // **** MAP APP *****
        System.out.println("** Open apple Maps **");
        driver.activateApp("com.apple.Maps");
        WebDriverWait wait = new WebDriverWait(driver, 5);

        driver.context("NATIVE_APP");

        // What's new in Maps
        try {
            final WebElement stayOnWebBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//XCUIElementTypeButton[@name=\"Continue\"]")));
            stayOnWebBtn.click();
        } catch (Exception e){
            // Do nothing - the popup dialog doesn't exist
            System.out.println("What's new in Maps - is not presented");
        }

        // Allow "Maps" to use your location?
        try {
            final WebElement useYourLocation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//XCUIElementTypeButton[@name='Allow While Using App']")));
            useYourLocation.click();
        } catch (Exception e){
            // Do nothing - the popup dialog doesn't exist
            System.out.println("Allow Maps to use your location? - is not presented");
        }

        // set the Tracking arrow on, if it is off
        WebElement trackingArrow = driver.findElementByAccessibilityId("Tracking");
        if (trackingArrow.getAttribute("value").equalsIgnoreCase("off")) {
            trackingArrow.click();
        }

        waiting(4);
        System.out.println("** set location **");
        driver.setLocation(new Location(latitude, longitude, 0.0));

    }
}
