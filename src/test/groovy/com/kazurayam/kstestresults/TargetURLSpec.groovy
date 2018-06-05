package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TargetURLSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TargetURLSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanupSpec() {}

    // feature methods
    def testExtensionPartPattern1() {
        setup:
        Matcher m = TargetURL.EXTENSION_PART_PATTERN.matcher('a.png')
        expect:
        m.find()
        m.groupCount() == 2
        m.group() == '.png'
        m.group(1) == null
        m.group(2) == null
    }

    def testExtensionPartPattern2() {
        setup:
        Matcher m = TargetURL.EXTENSION_PART_PATTERN.matcher('a.1.png')
        expect:
        m.find()
        m.groupCount() == 2
        m.group(0) == '.1.png'
        m.group(1) == '.1'
        m.group(2) == '1'
    }

    def testExtensionPartPattern3() {
        setup:
        Matcher m = TargetURL.EXTENSION_PART_PATTERN.matcher('a.jpn')
        expect:
        ! m.find()
    }

    def testExtensionPartPattern4() {
        setup:
        Matcher m = TargetURL.EXTENSION_PART_PATTERN.matcher('foo')
        expect:
        ! m.find()
    }

    def testParseScreenshotFileName1() {
        when:
        List<String> values = TargetURL.parseScreenshotFileName('C:/temp/a/b/c.png')
        then:
        values.size() == 1
        values[0] == 'c'
    }

    def testParseScreenshotFileName2() {
        when:
        List<String> values = TargetURL.parseScreenshotFileName('C:/temp/a/b/c.1.png')
        then:
        values.size() == 2
        values[0] == 'c'
        values[1] == '1'
    }

    def testParseScreenshotFileName3() {
        when:
        List<String> values = TargetURL.parseScreenshotFileName('C:\\temp\\d.9.png')
        then:
        values.size() == 2
        values[0] == 'd'
        values[1] == '9'
    }

    def testParseScreentshotFileName4() {
        when:
        String p = Paths.get('./src/test/fixture/photo1.png').normalize().toAbsolutePath().toString()
        //System.out.println("p=${p}")
        List<String> values = TargetURL.parseScreenshotFileName(p)
        then:
        values.size() == 1
        values[0] == 'photo1'
    }

    def testParseScreentshotFileName5() {
        when:
        List<String> values = TargetURL.parseScreenshotFileName('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        values.size() == 1
        values[0] == 'http://demoaut.katalon.com/'
    }

    def testParseScreentshotFileName6() {
        when:
        List<String> values = TargetURL.parseScreenshotFileName('http%3A%2F%2Fdemoaut.katalon.com%2F.0.png')
        then:
        values.size() == 2
        values[0] == 'http://demoaut.katalon.com/'
        values[1] == '0'
    }

    def testToJson() {
        setup:
        String dirName = 'testGetTestCaseResult'
        Path baseDir = workdir.resolve(dirName)
        Helpers.ensureDirs(baseDir)
        Helpers.copyDirectory(fixture, baseDir)
        TestResultsImpl sr = new TestResultsImpl(baseDir, new TsName('TS1'))
        TsResult tsr = sr.getCurrentTestSuiteResult()
        TcResult tcr = tsr.findOrNewTestCaseResult(new TcName('TC1'))
        TargetURL tp = tcr.findOrNewTargetPage(new URL('http://demoaut.katalon.com/'))
        ScreenshotWrapper sw = tp.findOrNewScreenshotWrapper('')
        when:
        def str = tp.toString()
        def pretty = JsonOutput.prettyPrint(str)
        System.out.println("#testToString: ${pretty}")
        then:
        str.startsWith('{"TargetPage":{')
        str.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        str.contains(Helpers.escapeAsJsonText('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        str.endsWith('}}')
    }

    // helper methods

}
