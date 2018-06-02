package com.kazurayam.ksbackyard.screenshotsupport

import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class ScreenshotRepositoryFactorySpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${ScreenshotRepositoryFactorySpec.getName()}")
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
        ScreenshotRepository scRepo = ScreenshotRepositoryFactory.createInstance(workdir, 'TS1')
        then:
        scRepo != null
    }

    // helper methods

}
