package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class TExecutionProfileSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TExecutionProfileSpec.class)

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
        TExecutionProfile ep = new TExecutionProfile('develop')
        when:
        String name = ep.getName()
        then:
        name == 'develop'
    }

    def test_getNameInPathSafeChars() {
        setup:
        String unsafeChars = "\\/:*?\"<>|aB愛"
        String expected = "￥／：＊？”＜＞｜aB愛"
        TExecutionProfile instance = new TExecutionProfile(unsafeChars)
        when:
        String actual = instance.getNameInPathSafeChars()
        then:
        assert expected == actual
    }
}
