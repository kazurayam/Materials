package com.kazurayam.testresultstorage

import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
//@Ignore
class ScreenshotRepositoryFactorySpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(ScreenshotRepositoryFactorySpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testCreateInstance() {
        when:
        ScreenshotRepository scRepo = ScreenshotRepositoryFactory.createInstance(workdir, 'Test Suites/TS1')
        then:
        scRepo != null
        scRepo.toString().contains('TS1')
    }

    // helper methods

}
