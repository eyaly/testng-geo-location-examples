# Testing on Mobile Devices with Location Services  
This project contains Java examples for running Appium tests on Sauce Labs platform and on local emualtors and simulators.
The scripts example how to handle the location services alerts, how to change the geo-location and fo iOS - how to enable the location services in the iOS settings. 
There are examples for Native Apps and for Web Apps.

- [Native App on Sauce Platform](#run-native-app-tests-on-android-real-devices-ios-real-devices-android-emulators-and-ios-simulators-in-the-sauce-labs-platform)

The framework uses testNG xml file for parallel executions. All the tests in the same class will run in parallel on different devices 
## Important information
### Environment variables for Sauce Labs
The examples in this repository that can run on Sauce Labs, use environment variables, make sure you've added the following

    # For Sauce Labs Real devices
    export SAUCE_USERNAME=********
    export SAUCE_ACCESS_KEY=*******
    
### Demo app
The Native demo app that has been used for these tests can be found [here](https://github.com/saucelabs/sample-app-mobile/releases).

### Upload apps to Sauce Storage
* If you want to use iOS real devices and Android real devices in Sauce Labs you need to upload the apps to the Sauce Storage.
For more information on this step please visit: [Application Storage](https://wiki.saucelabs.com/display/DOCS/Application+Storage).
* In the app capability you must use `storage:<app-id>` or `storage:filename=<file-name>`. For more information on this step please visit: [Application Storage](https://wiki.saucelabs.com/display/DOCS/Application+Storage).
* Change the value of appName in the native apps tests for Android and iOS according to your app name.
### Useful Links 
* How to upload the apps to the Sauce Storage and the app capability: [Application Storage](https://wiki.saucelabs.com/display/DOCS/Application+Storage).
* Appium Capabilities for Real Device Testing: [Appium Capabilities](https://wiki.saucelabs.com/display/DOCS/Appium+Capabilities+for+Real+Device+Testing).
* Sauce Labs Data Center Endpoints: [Data Center EndPoints](https://wiki.saucelabs.com/display/DOCS/Data+Center+Endpoints).
* How to set the pass/fail status of a test: [Annotating Tests with Selenium's JavaScript Executor](https://wiki.saucelabs.com/display/DOCS/Annotating+Tests+with+Selenium%27s+JavaScript+Executor).
## Run Native App tests on Android real devices, iOS real devices, Android Emulators and iOS Simulators in the Sauce Labs Platform
* The tests will run on Sauce [Demo app](https://github.com/saucelabs/sample-app-mobile/releases). 
* The test flow: (1) login (2) click on the hamburger menu (3) select the "GEO LOCATION" from the menu  (4) change the geo location using Appium command "setLocation"
* The [Android class](https://github.com/eyaly/testng-geo-location-examples/blob/master/src/test/java/tests/all/LocationAndroidAppTest.java) and the [iOS class](https://github.com/eyaly/testng-geo-location-examples/blob/master/src/test/java/tests/all/LocationIosAppTest.java) contain two tests: (1) change the location to Eiffel Tower (2) change the location to Tower Bridge
* The Android tests handle the location services alerts by the capability "autoGrantPermissions". Appium grants the permission, and the alerts will not popup:
> capabilities.setCapability("autoGrantPermissions", true);
* The iOS tests handle the location services alerts with 2 options by the capability "autoAcceptAlerts"
> // this capability will work for alerts with 2 options (iOS 12 and below)
>capabilities.setCapability("autoAcceptAlerts", true);
* The iOS tests handle the location services alerts with 3 options by the setting ["acceptAlertButtonSelector"](https://github.com/appium/appium-xcuitest-driver#settings-api)
>         // change the setting to accept alerts with 3 options and select the "Allow While Using App" (iOS 13 and above)
>          getiosDriver().setSetting("acceptAlertButtonSelector", "**/XCUIElementTypeButton[`label == \"Allow While Using App\"`]");
* The TestNG xml file to execute the tests can be found [here](https://github.com/eyaly/testng-geo-location-examples/blob/master/src/test/resources/config/location_sauce_app_test.xml).
* In the xml file, change the "thread-count="20" with the number of thread you want to run in parallel. The xml file contains the executions on all the platforms (Android, iOS, real devices, emulators and simulators)
* In the xml file, each test contains "enabled="true". You can changee  it to "false" if you don't want to execute this test.
* The command line to run the tests

      // If using the US DC
      mvn clean install -DtestngXmlFile=location_sauce_app_test.xml -Dregion=us
    
      // If using the EU DC
      mvn clean install -DtestngXmlFile=location_sauce_app_test.xml -Dregion=eu
    
> NOTE: Make sure you are in the folder `testng-geo-location-examples` when you execute this command



