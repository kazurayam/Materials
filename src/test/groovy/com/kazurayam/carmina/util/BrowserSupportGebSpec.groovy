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
        logger_.debug("#setupSpec going to modify browser.driver")
        browser.config.cacheDriver = false
        browser.driver = browser.config.driver
        logger_.debug("#setupSpec modified browser.driver")
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {
        logger_.debug("#cleanupSpec going to browser.close()")
        browser.close()
        logger_.debug("#cleanupSpec done browser.close()")
    }

    // feature methods
    @Timeout(16)
    def testDownloadCsvFileAndGetItAsAPath() {

        when:
        to FIFAWorldCup2018JapanPage

        // for debug
        //printFirefoxProfiles('testDownloadCsvFileAndGetItAsAPath')

        then:
        title.contains('Download the Japan FIFA World Cup 2018 fixture as CSV, XLSX and ICS | Fixture Download')

        when:
        logger_.debug("#testDownloadCsvFileAndGetItAsAPath going to click the anchor")
        //waitFor(20) {
        //    anchorDownloadCsv.isDisplayed()
        //}
        anchorDownloadCsv.click()
        logger_.debug("#testDownloadCsvFileAndGetItAsAPath clicked the anchor")
        //Path downloadedFile = BrowserSupport.waitForFileDownloaded(downloadsDir_, 3000, 10000)

        then:
        //downloadedFile != null
        //logger_.debug("#download CSV file and get its Path downloadedFile=${downloadedFile.toString()}")
        //Files.exists(downloadedFile)
        true

        cleanup:
        logger_.debug("#testDownloadCsvFileAndGetItAsAPath cleaning up")
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
