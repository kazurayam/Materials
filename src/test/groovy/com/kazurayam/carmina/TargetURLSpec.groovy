package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TargetURLSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TargetURLSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static RepositoryScanner scanner_
    private static TCaseResult tCaseResult

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TargetURLSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {
        scanner_ = new RepositoryScanner(workdir_)
        scanner_.scan()
        TSuiteResult tsr = scanner_.getTSuiteResult(new TSuiteName('TS1'),
                new TSuiteTimestamp('20180530_130419'))
        tCaseResult = tsr.getTCaseResult(new TCaseName('TC1'))
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetParent_GetParent() {
        when:
        TargetURL tu = new TargetURL(new URL('http://demoaut.katalon.com/'))
        TargetURL modified = tu.setParent(tCaseResult)
        then:
        modified == tu
        modified.getParent() == tCaseResult
    }

    def testGetMaterials() {
        when:
        TargetURL tu = tCaseResult.getTargetURL(new URL('http://demoaut.katalon.com/'))
        assert tu != null
        List<Material> materials = tu.getMaterials()
        then:
        materials.size() == 2
        when:
        Path file = materials.get(0).getMaterialFilePath()
        then:
        file.getFileName().endsWith('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
    }


    def testGetMaterialBySuffixAndFileType_withoutSuffix() {
        when:
        TargetURL tu = tCaseResult.getTargetURL(new URL('http://demoaut.katalon.com/'))
        Material mw = tu.getMaterial(Suffix.NULL, FileType.PNG)
        then:
        mw != null
    }

    def testGetMaterialBySuffixAndFileType_withSuffix() {
        when:
        TargetURL tu = tCaseResult.getTargetURL(new URL('http://demoaut.katalon.com/'))
        Material mw = tu.getMaterial(new Suffix('1'), FileType.PNG)
        then:
        mw != null
    }



    def testToJson() {
        when:
        TargetURL tp = tCaseResult.getTargetURL(new URL('http://demoaut.katalon.com/'))
        def str = tp.toString()
        def pretty = JsonOutput.prettyPrint(str)
        logger_.debug("#testToJson: ${pretty}")
        then:
        str.startsWith('{"TargetURL":{')
        str.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        str.contains(Helpers.escapeAsJsonText('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        str.endsWith('}}')
    }


    // helper methods

}
