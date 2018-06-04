package com.kazurayam.webtestingresultstorage

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.webtestingresultstorage.Helpers

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class SpecTemplate extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Screenshots")

    // fixture methods
    def setup() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(SpecTemplate.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testSomething() {
        setup:
        anything()
        when:
        anything()
        then:
        anything()
        cleanup:
        anything()
    }

    // helper methods
    def void anything() {}
}
