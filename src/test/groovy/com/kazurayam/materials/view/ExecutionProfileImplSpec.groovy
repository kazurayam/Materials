package com.kazurayam.materials.view

import spock.lang.Specification

import static org.junit.Assert.assertEquals
import com.kazurayam.materials.ExecutionProfile
import org.junit.Test

class ExecutionProfileImplSpec extends Specification {

    def test_getNameInPathSafeChars() {
        setup:
        String unsafeChars = "\\/:*?\"<>|aB愛"
        String expected = "￥／：＊？”＜＞｜aB愛"
        ExecutionProfile instance = new ExecutionProfileImpl(unsafeChars)
        when:
        String actual = instance.getNameInPathSafeChars()
        then:
        assert expected == actual
    }
}
