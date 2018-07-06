/**
 * This is the Geb configuration file
 */

 import org.openqa.selenium.firefox.FirefoxDriver

 waiting {
     timeout = 2
 }

 environments {
     // run via "./gradlew firefoxTest"
     // see http://code.google.com/p/selenium/wiki/FirefoxDriver
     firefox {
         atCheckWaiting = 1
         dirver = { new FirefoxDriver() }
     }
 }

 // To run the tests with all briwser just run "./gradlew test"

 baseUrl = "http://gebish.org"