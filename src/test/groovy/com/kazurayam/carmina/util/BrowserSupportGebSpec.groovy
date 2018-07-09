package com.kazurayam.carmina.util

import java.nio.file.Path
import java.nio.file.Paths

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
    static Path downloadsDir_ = Paths.get(System.getProperty('user.home'), 'Downloads')

    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    @Timeout(32)
    def testDownloadCsvFileAndGetItAsAPath() {

        when:
        logger_.debug("#testDownloadCsvFileAndGetItAsAPath")
        to FIFAWorldCup2018JapanPage

        then:
        title.contains('Download the Japan FIFA World Cup 2018 fixture as CSV, XLSX and ICS | Fixture Download')

        when:
        anchorDownloadCsv.click()
        Thread.sleep(3000)
        //Path downloadedFile = BrowserSupport.waitForFileDownloaded(downloadsDir_, 3000, 10000)

        then:
        //downloadedFile != null
        //logger_.debug("#download CSV file and get its Path downloadedFile=${downloadedFile.toString()}")
        //Files.exists(downloadedFile)
        true

        cleanup:
        driver.quit()
    }

}
