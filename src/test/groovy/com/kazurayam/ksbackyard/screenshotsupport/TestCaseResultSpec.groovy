package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
class TestCaseResultSpec extends Specification {

    // fields
    private static Path workdir

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TestCaseResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods


    // helper methods
}
