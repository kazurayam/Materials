package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification
//@Ignore
class TestMaterialsRepositoryFactorySpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(TestMaterialsRepositoryFactorySpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(TestMaterialsRepositoryFactorySpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testCreateInstance() {
        when:
        TestMaterialsRepository tmr = TestMaterialsRepositoryFactory.createInstance(workdir_)
        tmr.putCurrentTestSuite('Test Suites/TS1')
        then:
        tmr != null
        tmr.toString().contains('TS1')
    }

    // helper methods

}
