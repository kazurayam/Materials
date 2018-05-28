package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification

/**
 * http://spockframework.org/spock/docs/1.0/spock_primer.html
 */
class HelpersSpec extends Specification {

    // fields
    private static Path workdir
    private static Path resourcesdir

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        resourcesdir = Paths.get("./src/test/resources")
        workdir = Paths.get("./build/tmp/${Helpers.getName()}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
    }
    def cleanupSpec() {}

    // feature methods
    def testRunImagemagickCommandConvertNoArg() {
        setup:
        OutputStream out = new ByteArrayOutputStream()
        OutputStream err = new ByteArrayOutputStream()

        String[] args = ['convert'] as String[]
        when:
        int ret = Helpers.runImagemagickCommand(args, out, err)
        then:
        ret == 1
        cleanup:
        System.out.println("out=${out.toString()}")
        System.err.println("err=${err.toString()}")
    }

    def testRunImagemagickCommand_step1() {
        setup:
        Path photo1 = resourcesdir.resolve('photo1.png')
        Path mask   = workdir.resolve('mask.png')
        OutputStream out = new ByteArrayOutputStream()
        OutputStream err = new ByteArrayOutputStream()

        String[] args = ['convert',
            "${photo1.toString()}",
            '-fill', 'white', '-colorize', '100%',
            "${mask.toString()}"
            ] as String[]
        when:
        int ret = Helpers.runImagemagickCommand(args, out, err)
        then:
        ret == 0
        cleanup:
        System.out.println("out=${out.toString()}")
        System.err.println("err=${err.toString()}")
    }

    // helper methods

}
