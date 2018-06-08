package com.kazurayam.carmina

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.Helpers
import com.kazurayam.carmina.TestResults
import com.kazurayam.carmina.TestResultsFactory

import spock.lang.Specification
//@Ignore
class TestResultsFactorySpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsFactorySpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def cleanupSpec() {}

    // feature methods
    def testCreateInstance() {
        when:
        TestResults trs = TestResultsFactory.createInstance(workdir, 'Test Suites/TS1')
        then:
        trs != null
        trs.toString().contains('TS1')
    }

    // helper methods

}
