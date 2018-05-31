package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Paths
import java.util.regex.Matcher

import spock.lang.Specification

//@Ignore
class TargetPageSpec extends Specification {

    // fields

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testExtensionPartPattern1() {
        setup:
        Matcher m = TargetPage.EXTENSION_PART_PATTERN.matcher('a.png')
        expect:
        m.find()
        m.groupCount() == 2
        m.group() == '.png'
        m.group(1) == null
        m.group(2) == null
    }

    def testExtensionPartPattern2() {
        setup:
        Matcher m = TargetPage.EXTENSION_PART_PATTERN.matcher('a.1.png')
        expect:
        m.find()
        m.groupCount() == 2
        m.group(0) == '.1.png'
        m.group(1) == '.1'
        m.group(2) == '1'
    }

    def testExtensionPartPattern3() {
        setup:
        Matcher m = TargetPage.EXTENSION_PART_PATTERN.matcher('a.jpn')
        expect:
        ! m.find()
    }

    def testExtensionPartPattern4() {
        setup:
        Matcher m = TargetPage.EXTENSION_PART_PATTERN.matcher('foo')
        expect:
        ! m.find()
    }

    def testParseScreenshotFileName1() {
        when:
        List<String> values = TargetPage.parseScreenshotFileName('C:/temp/a/b/c.png')
        then:
        values.size() == 1
        values[0] == 'c'
    }

    def testParseScreenshotFileName2() {
        when:
        List<String> values = TargetPage.parseScreenshotFileName('C:/temp/a/b/c.1.png')
        then:
        values.size() == 2
        values[0] == 'c'
        values[1] == '1'
    }

    def testParseScreenshotFileName3() {
        when:
        List<String> values = TargetPage.parseScreenshotFileName('C:\\temp\\d.9.png')
        then:
        values.size() == 2
        values[0] == 'd'
        values[1] == '9'
    }

    def testParseScreentshotFileName4() {
        when:
        String p = Paths.get('./src/test/fixture/photo1.png').normalize().toAbsolutePath().toString()
        //System.out.println("p=${p}")
        List<String> values = TargetPage.parseScreenshotFileName(p)
        then:
        values.size() == 1
        values[0] == 'photo1'
    }

    def testParseScreentshotFileName5() {
        when:
        List<String> values = TargetPage.parseScreenshotFileName('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        values.size() == 1
        values[0] == 'http://demoaut.katalon.com/'
    }


    // helper methods

}
