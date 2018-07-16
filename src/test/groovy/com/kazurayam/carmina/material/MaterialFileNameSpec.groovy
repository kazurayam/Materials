package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.util.regex.Matcher

class MaterialFileNameSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialFileNameSpec.class)

    def testFull() {
        when:
        MaterialFileName mfn = new MaterialFileName('http%3A%2F%2Fdemoaut.katalon.com%2F (1).png')
        then:
        mfn.parts[0]      == 'http%3A%2F%2Fdemoaut.katalon.com%2F (1).png'
        mfn.parts[1]      == 'http%3A%2F%2Fdemoaut.katalon.com%2F '
        mfn.parts[2]      == '(1)'
        mfn.parts[3]      == '.png'
        mfn.getSuffix()   == new Suffix(1)
        mfn.getFileType() == FileType.PNG
    }

    def testUnknowExtension() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc.defghij')
        then:
        mfn.parts[0]      == 'abc.defghij'
        mfn.parts[1]      == 'abc'
        mfn.parts[2]      == null
        mfn.parts[3]      == '.defghij'
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.UNSUPPORTED
    }

    def testWithoutExtension() {
        when:
        MaterialFileName mfn = new MaterialFileName('abcdef')
        then:
        mfn.parts[0]      == 'abcdef'
        mfn.parts[1]      == 'abcdef'
        mfn.parts[2]      == null
        mfn.parts[3]      == null
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.NULL
    }

    def testWithDualParenthis() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc (1)(2).txt')
        then:
        mfn.parts[0]      == 'abc (1)(2).txt'
        mfn.parts[1]      == 'abc (1)'
        mfn.parts[2]      == '(2)'
        mfn.parts[3]      == '.txt'
        mfn.getSuffix()   == new Suffix(2)
        mfn.getFileType() == FileType.TXT
    }

}
