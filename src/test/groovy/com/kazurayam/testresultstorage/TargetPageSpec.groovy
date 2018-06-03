package com.kazurayam.testresultstorage

import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher

import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
class TargetPageSpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TargetPageSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
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

    def testParseScreentshotFileName6() {
        when:
        List<String> values = TargetPage.parseScreenshotFileName('http%3A%2F%2Fdemoaut.katalon.com%2F.0.png')
        then:
        values.size() == 2
        values[0] == 'http://demoaut.katalon.com/'
        values[1] == '0'
    }

    /**
     * TargetPage#uniqueScreenshotWrapper will scan the TestCase diretory.
     * Supposing that there are following files in the TestCase directory,
     *     http%3A%2F%2Fdemoaut.katalon.com%2F.png
     *     http%3A%2F%2Fdemoaut.katalon.com%2F.1.png
     *     http%3A%2F%2Fdemoaut.katalon.com%2F.3.png
     * then we will find the following Path is unique and avaliable:
     *     http%3A%2F%2Fdemoaut.katalon.com%2F.2.png
     *
     */

    @Ignore
    def testUniqueScreenshotWrapper() {
        setup:
        String dirName = 'testUniqueScreenshotWrapper'
        Path baseDir = workdir.resolve(dirName)
        TestSuiteName tsn = new TestSuiteName('TS')
        TestCaseName tcn = new TestCaseName('TC')
        URL url = new URL('http://demoauto.katalon.com/')
        ScreenshotRepositoryImpl sr = new ScreenshotRepositoryImpl(baseDir, tsn)
        when:
        TestCaseResult tcr = sr.getCurrentTestSuiteResult().findOrNewTestCaseResult(tcn)
        then:
        tcr != null
        when:
        Path testCaseDir = tcr.getTestCaseDir()
        Helpers.ensureDirs(testCaseDir)
        Helpers.touch(testCaseDir.resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        Helpers.touch(testCaseDir.resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.1.png'))
        Helpers.touch(testCaseDir.resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.3.png'))
        TargetPage tp = tcr.findOrNewTargetPage(url)
        then:
        assert tp != null
        when:
        ScreenshotWrapper sw = tp.uniqueScreenshotWrapper()
        then:
        sw != null
        sw.getScreenshotFilePath() == testCaseDir.resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.2.png')
    }

    // helper methods

}
