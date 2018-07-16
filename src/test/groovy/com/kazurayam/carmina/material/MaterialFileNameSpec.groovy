package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.util.regex.Matcher

class MaterialFileNameSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialFileNameSpec.class)

    def testParse_full() {
        when:
        List<String> groups = MaterialFileName.parse('http%3A%2F%2Fdemoaut.katalon.com%2F (1).png')
        then:
        groups[1] == 'http%3A%2F%2Fdemoaut.katalon.com%2F '
        groups[2] == '(1)'
        groups[3] == '1'
        groups[4] == '.png'
        groups[5] == 'png'
    }

    def testParse_withoutExtension() {
        when:
        List<String> groups = MaterialFileName.parse('foo')
        then:
        groups[1] == 'foo'
        groups[2] == null
        groups[3] == null
        groups[4] == null
        groups[5] == null
    }

    def testParse_bodyPlusExtension() {
        when:
        List<String> groups = MaterialFileName.parse('foo.png1')
        then:
        groups[1] == 'foo'
        groups[2] == null
        groups[3] == null
        groups[4] == '.png1'
        groups[5] == 'png1'
    }

    def testParse_unpairingParenthis() {
        when:
        List<String> groups = MaterialFileName.parse('foo(9.png')
        then:
        groups[1] == 'foo(9'
        groups[2] == null
        groups[3] == null
        groups[4] == '.png'
        groups[5] == 'png'
    }



}
