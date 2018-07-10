package com.kazurayam.carmina.util

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.openqa.selenium.Capabilities
import org.openqa.selenium.Keys
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import geb.spock.GebSpec
import spock.lang.Timeout

/**
 * This GebSpec is designed to test the
 * com.kazurayam.carmina.util.BrowserSupport class
 * with interaction with browser driven by WebDriver.
 *
 */
class BrowserSupportGebSpec extends GebSpec {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(BrowserSupportGebSpec.class)

    // fixture methods
    def setupSpec() {
        /*
        logger_.debug("#setupSpec going to modify browser.driver")
        browser.config.cacheDriver = false
        browser.driver = browser.config.driver
        logger_.debug("#setupSpec modified browser.driver")
        */
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {
        /*
        logger_.debug("#cleanupSpec going to browser.close()")
        browser.close()   # close the browser leaving the driver instance still running
        logger_.debug("#cleanupSpec done browser.close()")
        */
    }

    // feature methods
    @Timeout(32)
    def testDownloadCsvFileAndGetItAsAPath() {

        when:
        // for debug
        //printFirefoxProfiles('testDownloadCsvFileAndGetItAsAPath')
        to ResultsFIFAWorldCup2018JapanPage
        logger_.debug("${getTimestampOfNow()} #testDownloadCsvFileAndGetItAsAPath before waitFor anchorDownloadCsv")
        waitFor(10) {
            anchorDownloadCsv
        }
        logger_.debug("${getTimestampOfNow()} #testDownloadCsvFileAndGetItAsAPath before anchorDownloadCsv.click")

        anchorDownloadCsv.sendKeys(Keys.chord(Keys.ENTER))
        // anchorDownloadCsv.click() is problematic. Invoking click() gets stuck in there and does not continue the execution till the timeout.
        // see https://stackoverflow.com/questions/46939451/timeout-when-using-click-webdriver-selenium-function-python

        logger_.debug("${getTimestampOfNow()} #testDownloadCsvFileAndGetItAsAPath after anchorDownloadCsv.click")

        //Path downloadedFile = BrowserSupport.waitForFileDownloaded(downloadsDir_, 3000, 10000)

        then:
        at DownloadCsvFIFAWorldCup2018JapanPage
        //downloadedFile != null
        //logger_.debug("#download CSV file and get its Path downloadedFile=${downloadedFile.toString()}")
        //Files.exists(downloadedFile)
    }

    private def printFirefoxProfiles(String methodName) {
        Capabilities capabilities = ((RemoteWebDriver)driver).getCapabilities()
        String preferencePath = (String)capabilities.getCapability("moz:profile") + "\\user.js"
        Path preference = Paths.get(preferencePath)
        assert Files.exists(preference)
        List<String> lines = Files.readAllLines(preference, StandardCharsets.UTF_8)
        def count = 0
        for (String line : lines) {
            count += 1
            logger_.debug("#${methodName} FirefoxProfile:${count} ${line}")
        }
    }

    private String getTimestampOfNow() {
        LocalDateTime ldt = LocalDateTime.now()
        //return DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss.SSS").format(ldt)
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ldt)
    }
}
