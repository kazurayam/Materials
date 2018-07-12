package com.kazurayam.carmina.util

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.openqa.selenium.Capabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import geb.spock.GebSpec
import spock.lang.Ignore
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
    static Path downloadsDir_ = Paths.get(System.getProperty('user.home'), 'Downloads')

    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testDownloadXls_SmileyChart() {
        when:
        browser.driver.manage().window().maximize()
        to SpreadsheetComSmileyChartPage
        waitFor(10) {
            anchorDownloadXls
        }
        anchorDownloadXls.click()
        Thread.sleep(1000)
        Path xlsFile = downloadsDir_.resolve('smilechart.xls')

        then:
        Files.exists(xlsFile)
    }

    @Ignore
    @Timeout(32)
    def testDownloadCsvFileAndGetItAsAPath_FIFA() {
        when:
        browser.driver.manage().window().maximize()
        // for debug
        //printFirefoxProfiles('testDownloadCsvFileAndGetItAsAPath')
        to FIFAWorldCup2018ResultsPage
        logger_.debug("${getTimestampOfNow()} #testDownloadCsvFileAndGetItAsAPath_FIFA before waitFor anchorDownloadCsv")
        waitFor(10) {
            anchorDownloadCsv
        }
        logger_.debug("${getTimestampOfNow()} #testDownloadCsvFileAndGetItAsAPath_FIFA before anchorDownloadCsv.click")
        anchorDownloadCsv.click()
        //anchorDownloadCsv.sendKeys(Keys.chord(Keys.ENTER))
        // anchorDownloadCsv.click() is problematic. Invoking click() gets stuck in there and does not continue the execution till the timeout.
        // see https://stackoverflow.com/questions/46939451/timeout-when-using-click-webdriver-selenium-function-python
        logger_.debug("${getTimestampOfNow()} #testDownloadCsvFileAndGetItAsAPath_FIFA after anchorDownloadCsv.click")
        //Path downloadedFile = BrowserSupport.waitForFileDownloaded(downloadsDir_, 3000, 10000)
        then:
        at FIFAWorldCup2018DownloadCsvPage
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
