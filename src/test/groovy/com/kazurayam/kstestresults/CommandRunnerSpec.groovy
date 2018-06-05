package com.kazurayam.kstestresults

import spock.lang.Specification

class CommandRunnerSpec extends Specification {

    // fields

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {}
    def cleanupSpec() {}

    // feature methods
    def testRunCommand() {
        when:
        String[] args = ['java', '-version']
        OutputStream out = new ByteArrayOutputStream()
        OutputStream err = new ByteArrayOutputStream()
        CommandRunner.runCommand(args, out, err)
        def outString = out.toString('UTF-8')
        def errString = err.toString('UTF-8')
        System.out.println("${outString}")
        System.err.println("${errString}")
        then:
        outString.length() == 0
        errString.contains('java version')
    }

    // helper methods

}
