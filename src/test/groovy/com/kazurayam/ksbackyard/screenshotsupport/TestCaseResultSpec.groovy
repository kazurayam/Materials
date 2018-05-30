package com.kazurayam.ksbackyard.screenshotsupport

import spock.lang.Specification

class TestCaseResultSpec extends Specification {
    // fields
    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testTargetPageParseScreenshotFileName() {
        when:
        List<String> values1 = TestCaseResult.TargetPage.parseScreenshotFileName('C:/temp/a/b/c.png')
        then:
        values1.size() == 1
        values1[0] == 'c'
        when:
        List<String> values2 = TestCaseResult.TargetPage.parseScreenshotFileName('C:/temp/a/b/c.1.png')
        then:
        values2.size() == 2
        values2[0] == 'c'
        values2[1] == '1'
    }

    // helper methods
}
