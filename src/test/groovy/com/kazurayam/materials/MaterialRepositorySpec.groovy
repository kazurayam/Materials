package com.kazurayam.materials

import static groovy.json.JsonOutput.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.model.TSuiteTimestampImpl

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class MaterialRepositorySpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositorySpec.class);

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepository mr_

    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialRepositorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }

    def setup() {
        mr_ = MaterialRepositoryFactory.createInstance(workdir_.resolve('Materials'))
    }

    def testPutCurrentTestSuite_oneStringArg() {
        when:
        mr_.putCurrentTestSuite('oneStringArg')
        Path timestampdir = mr_.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        when:
        String dirName = timestampdir.getFileName()
        LocalDateTime ldt = TSuiteTimestampImpl.parse(dirName)
        then:
        true
    }

    def testPutCurrentTestSuite_twoStringArg() {
        when:
        mr_.putCurrentTestSuite('oneStringArg', '20180616_160000')
        Path timestampdir = mr_.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        timestampdir.getFileName().toString().contains('20180616_160000')
    }

    def testPutCurrentTestSuite_tSuiteName_tSuiteTimestamp() {
        when:
        mr_.putCurrentTestSuite(
                new TSuiteName('oneStringArg'), TSuiteTimestampImpl.newInstance('20180616_160000'))
        Path timestampdir = mr_.getCurrentTestSuiteDirectory()
        logger_.debug("#testSetCurrentTestSuite_oneStringArg timestampdir=${timestampdir}")
        then:
        timestampdir.toString().contains('oneStringArg')
        timestampdir.getFileName().toString().contains('20180616_160000')
    }

    def testJsonOutput() {
        setup:
        List<String> errors = []
        errors << 'Your password was bad.'
        errors << 'Your feet smell.'
        errors << 'I am having a bad day.'
        when:
        def jsonString = toJson(errors)
        then:
        jsonString == '["Your password was bad.","Your feet smell.","I am having a bad day."]'
    }

    def testToJson() {
        when:
        mr_.putCurrentTestSuite('Test Suites/TS1')
        def str = JsonOutput.prettyPrint(mr_.toString())
        //logger_.debug(JsonOutput.prettyPrint(str))
        then:
        str.contains('TS1')
    }

    def testConstructor_Path_tsn() {
        when:
        mr_.putCurrentTestSuite('Test Suites/main/TS1')
        String str = mr_.toString()
        then:
        str.contains('main.TS1')
    }


    def testGetCurrentTestSuiteDirectory() {
        when:
        mr_.putCurrentTestSuite('Test Suites/main/TS1','20180530_130419')
        Path testSuiteDir = mr_.getCurrentTestSuiteDirectory()
        then:
        testSuiteDir == workdir_.resolve('Materials/main.TS1').resolve('20180530_130419').normalize()
    }
    
    def testGetMaterials_withArgs() {
        when:
        List<Material> list = mr_.getMaterials(new TSuiteName("Test Suites/main/TS1"),
            TSuiteTimestampImpl.newInstance("20180530_130419"))
        then:
        list.size() == 3
        //
        when:
        list = mr_.getMaterials(new TSuiteName("Test Suites/main/TS1"),
            TSuiteTimestampImpl.newInstance("20180530_130604"))
        then:
        list.size() == 6
        //
        when:
        list = mr_.getMaterials(new TSuiteName("Test Suites/main/TS2"),
            TSuiteTimestampImpl.newInstance("20180612_111256"))
        then:
        list.size() == 2
    }

    def testGetTestCaseDirectory() {
        when:
        mr_.putCurrentTestSuite('Test Suites/main/TS1','20180530_130419')
        Path testCaseDir = mr_.getTestCaseDirectory('Test Cases/main/TC1')
        then:
        testCaseDir == workdir_.resolve('Materials/main.TS1').resolve('20180530_130419').resolve('main.TC1').normalize()
    }

    def testResolveScreenshotPath() {
        when:
        mr_.putCurrentTestSuite('Test Suites/main/TS1','20180530_130419')
        Path path = mr_.resolveScreenshotPath('Test Cases/main/TC1', new URL('http://demoaut.katalon.com/'))
        then:
        path.getFileName().toString() == 'http%3A%2F%2Fdemoaut.katalon.com%2F(2).png'
    }


    def testMakeIndex() {
        when:
        mr_.putCurrentTestSuite('Test Suites/main/TS1','20180530_130419')
        Path index = mr_.makeIndex()
        then:
        Files.exists(index)
    }

    
    def testCreateMaterialPairs_TSuiteNameOnly() {
        when:
        List<MaterialPair> list = mr_.createMaterialPairs(new TSuiteName('TS1'))
        then:
        list.size() == 1
        when:
        MaterialPair mp = list.get(0)
        Material expected = mp.getExpected()
        Material actual = mp.getActual()
        then:
        expected.getPathRelativeToTSuiteTimestamp() == Paths.get('TC1/CURA_Healthcare_Service.png')
        actual.getPathRelativeToTSuiteTimestamp()   == Paths.get('TC1/CURA_Healthcare_Service.png')
    }
    

    /**
     * This will test deleteBaseDirContents() method.
     * This will create a fixture for its own.
     *
     * @throws IOException
     */
    def testDeleteBaseDirContents() throws IOException {
        setup:
        Path workdir = Paths.get("./build/tmp/MaterialRepository_DeleteBaseDirContents")
        if (!Files.exists(workdir)) {
            Files.createDirectories(workdir)
        }
        Helpers.copyDirectory(fixture_, workdir)
        //
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(workdir.resolve('Materials'))
        //
        when:
        mr.deleteBaseDirContents()
        List<String> contents = mr.getBaseDir().toFile().list()
        then:
        contents.size() == 0
    }

}

