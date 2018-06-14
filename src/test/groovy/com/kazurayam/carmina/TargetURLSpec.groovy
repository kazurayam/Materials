package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TargetURLSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(TargetURLSpec.class);

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static RepositoryScanner scanner
    private static TCaseResult tcr

    // fixture methods
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TargetURLSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {
        scanner = new RepositoryScanner(workdir)
        scanner.scan()
        TSuiteResult tsr = scanner.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
        tcr = tsr.getTCaseResult(new TCaseName('TC1'))
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        TargetURL tu = new TargetURL(new URL('http://demoaut.katalon.com/'))
        TargetURL modified = tu.setParent(tcr)
        then:
        modified == tu
        modified.getParent() == tcr
    }

    def testGetMaterialWrappers() {
        when:
        TargetURL tu = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
        assert tu != null
        List<MaterialWrapper> materialWrappers = tu.getMaterialWrappers()
        then:
        materialWrappers.size() == 2
        when:
        Path file = materialWrappers.get(0).getMaterialFilePath()
        then:
        file.getFileName().endsWith('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
    }

    /*
    def testGetMaterialWrapperBySuffixAndFileType() {
        when:
        TargetURL tu = tcr.getTargetURL(new URL('http://demoauto.katalon.com/'))
        MaterialWrapper mw = tu.getMaterialWrapper(suffix, fileType)
    }
    */

    def testToJson() {
        when:
        TargetURL tp = tcr.getTargetURL(new URL('http://demoaut.katalon.com/'))
        def str = tp.toString()
        def pretty = JsonOutput.prettyPrint(str)
        logger.debug("#testToJson: ${pretty}")
        then:
        str.startsWith('{"TargetURL":{')
        str.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        str.contains(Helpers.escapeAsJsonText('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        str.endsWith('}}')
    }


    // helper methods

}
