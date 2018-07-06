package com.kazurayam.carmina.util

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Ignore
import spock.lang.Specification


class BrowserSupportGebSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(BrowserSupportGebSpec.class)

    // fields

    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testFindLastModifiedFileInDirectory() {
        setup:
        Path downloadsDir = Paths.get(System.getProperty('user.home'), 'Downloads')
        when:
        Path lastModifiedFile = BrowserSupport.findLastModifiedFileInDirectory(downloadsDir)
        logger_.debug("#testFindLastModifiedFileInDirectory lastModifiedFile=${lastModifiedFile.toString()}")
        then:
        lastModifiedFile != null
        Files.isRegularFile(lastModifiedFile)
    }

    def testWaitForFileDownloadedNoChange() {
        setup:
        Path downloadsDir = Paths.get(System.getProperty('user.home'), 'Downloads')
        when:
        Path lastModifiedFileAfterWait = BrowserSupport.waitForFileDownloaded(downloadsDir, 3000, 10000)
        then:
        lastModifiedFileAfterWait != null
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
