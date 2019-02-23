package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.view.ExecutionProfileImpl

import spock.lang.Specification

class ExecutionProfileSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(ExecutionProfileSpec.class)

    // fields
    //private static Path workdir_
    //private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    // fixture methods
    def setupSpec() {
        //workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(ExecutionProfileSpec.class)}")
        //if (!workdir_.toFile().exists()) {
        //    workdir_.toFile().mkdirs()
        //}
        //Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetName() {
        setup:
        ExecutionProfile ep = ExecutionProfileImpl.newInstance('develop')
        when:
        String name = ep.getName()
        then:
        name == 'develop'
    }

}
