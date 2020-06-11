package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class TExecutionProfileSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TExecutionProfileSpec.class)

    // fields
    private static Path workdir_
    //private static Path fixture_ = Paths.get("./src/test/fixture/Materials")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TExecutionProfileSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
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

    def test_constructor_with_Path() {
        setup:
        Path dir = workdir_.resolve("Materials").resolve("￥／：＊？”＜＞｜aB愛")
        when:
        TExecutionProfile obj = new TExecutionProfile(dir)
        then:
        assert obj != null
        assert obj.getName() == "\\/:*?\"<>|aB愛"
    }
}
