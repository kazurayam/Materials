package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.carmina.Helpers
import com.kazurayam.carmina.TestResultsRepository
import com.kazurayam.carmina.TestResultsRepositoryFactory

import spock.lang.Specification
//@Ignore
class TestResultsRepositoryFactorySpec extends Specification {

    // fields
    static Logger logger = LoggerFactory.getLogger(TestResultsRepositoryFactorySpec.class)

    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestResultsRepositoryFactorySpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testCreateInstance() {
        when:
        TestResultsRepository trs = TestResultsRepositoryFactory.createInstance(workdir, 'Test Suites/TS1')
        then:
        trs != null
        trs.toString().contains('TS1')
    }

    // helper methods

}
