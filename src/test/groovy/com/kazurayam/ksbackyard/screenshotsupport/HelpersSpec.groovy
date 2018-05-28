package com.kazurayam.ksbackyard.screenshotsupport

import spock.lang.Specification

/**
 * http://spockframework.org/spock/docs/1.0/spock_primer.html
 */
class HelpersSpec extends Specification {

    // fields

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {}
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
        ret == 4   // ファイルシステムを指定してください
        cleanup:
        println(args)
        println("out=${out.toString()}")
        println("err=${err.toString()}")
    }

    def testRunImagemagickCommandConvertHelp() {
        setup:
        OutputStream out = new ByteArrayOutputStream()
        OutputStream err = new ByteArrayOutputStream()

        String[] args = ['convert', '-h'] as String[]
        when:
        int ret = Helpers.runImagemagickCommand(args, out, err)
        then:
        ret == 0   // ファイルシステムを指定してください
        cleanup:
        println(args)
        println("out=${out.toString()}")
        println("err=${err.toString()}")
    }

    // helper methods

}
