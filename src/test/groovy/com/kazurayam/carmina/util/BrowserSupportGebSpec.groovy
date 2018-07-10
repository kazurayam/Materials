package com.kazurayam.carmina.util

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.openqa.selenium.Capabilities
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
        waitFor(10) {
            anchorDownloadCsv
        }
        logger_.debug("#testDownloadCsvFileAndGetItAsAPath before anchorDownloadCsv.click")
        anchorDownloadCsv.click()
        logger_.debug("#testDownloadCsvFileAndGetItAsAPath after anchorDownloadCsv.click")

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
}
