package com.kazurayam.visualtesting

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class CommandRunnerSpec extends Specification {

    // fields
    static Logger logger = LoggerFactory.getLogger(CommandRunnerSpec.class)

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
