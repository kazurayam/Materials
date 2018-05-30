package com.kazurayam.ksbackyard.screenshotsupport

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class SpecTemplate extends Specification {

    // fields

    // fixture methods
    def setup() {}
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
