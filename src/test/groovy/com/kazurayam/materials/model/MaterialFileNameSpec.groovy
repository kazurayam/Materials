package com.kazurayam.materials.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.model.FileType
import com.kazurayam.materials.model.MaterialFileName
import com.kazurayam.materials.model.Suffix

import spock.lang.Specification

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
        mfn.getFileName() == 'http%3A%2F%2Fdemoaut.katalon.com%2F (1).png'
        mfn.getURL().toString()      == new URL('http://demoaut.katalon.com/').toString()
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
        mfn.getFileName() == 'abc.defghij'
        mfn.getURL()      == null
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
        mfn.getFileName() == 'abcdef'
        mfn.getURL()      == null
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
        mfn.getFileName() == 'abc (1)(2).txt'
        mfn.getURL()      == null
        mfn.getSuffix()   == new Suffix(2)
        mfn.getFileType() == FileType.TXT
    }

    def 'a.png'() {
        when:
        MaterialFileName mfn = new MaterialFileName('a.png')
        then:
        mfn.parts[0]      == 'a.png'
        mfn.parts[1]      == 'a'
        mfn.parts[2]      == null
        mfn.parts[3]      == '.png'
        mfn.getFileName() == 'a.png'
        mfn.getURL()      == null
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.PNG
    }

    def 'a'() {
        when:
        MaterialFileName mfn = new MaterialFileName('a')
        then:
        mfn.parts[0]      == 'a'
        mfn.parts[1]      == 'a'
        mfn.parts[2]      == null
        mfn.parts[3]      == null
        mfn.getFileName() == 'a'
        mfn.getURL()      == null
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.NULL
     }

    def 'a.foo'() {
        when:
        MaterialFileName mfn = new MaterialFileName('a.foo')
        then:
        mfn.parts[0]      == 'a.foo'
        mfn.parts[1]      == 'a'
        mfn.parts[2]      == null
        mfn.parts[3]      == '.foo'
        mfn.getFileName() == 'a.foo'
        mfn.getURL()      == null
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.UNSUPPORTED
    }

    def 'abc(2)'() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc(2)')
        then:
        mfn.parts[0]      == 'abc(2)'
        mfn.parts[1]      == 'abc'
        mfn.parts[2]      == '(2)'
        mfn.parts[3]      == null
        mfn.getFileName() == 'abc(2)'
        mfn.getURL()      == null
        mfn.getSuffix()   == new Suffix(2)
        mfn.getFileType() == FileType.NULL
    }


    def 'abc (2)'() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc (2)')
        then:
        mfn.parts[0]      == 'abc (2)'
        mfn.parts[1]      == 'abc '
        mfn.parts[2]      == '(2)'
        mfn.parts[3]      == null
        mfn.getFileName() == 'abc (2)'
        mfn.getURL()      == null
        mfn.getSuffix()   == new Suffix(2)
        mfn.getFileType() == FileType.NULL
    }

    /**
     * 2147483647 maximum value of int
     */
    def 'abc (2147483647)'() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc (2147483647)')
        then:
        mfn.parts[0]      == 'abc (2147483647)'
        mfn.parts[1]      == 'abc '
        mfn.parts[2]      == '(2147483647)'
        mfn.parts[3]      == null
        mfn.getFileName() == 'abc (2147483647)'
        mfn.getURL()      == null
        mfn.getSuffix()   == new Suffix(2147483647)
        mfn.getFileType() == FileType.NULL
    }

    def 'abc(-2)'() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc(-2)')
        then:
        mfn.parts[0]      == 'abc(-2)'
        mfn.parts[1]      == 'abc(-2)'
        mfn.parts[2]      == null
        mfn.parts[3]      == null
        mfn.getFileName() == 'abc(-2)'
        mfn.getURL()      == null
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.NULL
    }

    def 'abc(d)'() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc(d)')
        then:
        mfn.parts[0]      == 'abc(d)'
        mfn.parts[1]      == 'abc(d)'
        mfn.parts[2]      == null
        mfn.parts[3]      == null
        mfn.getFileName() == 'abc(d)'
        mfn.getURL()      == null
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.NULL
    }

    def 'abc def .png'() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc def .png')
        then:
        mfn.parts[0]      == 'abc def .png'
        mfn.parts[1]      == 'abc def '
        mfn.parts[2]      == null
        mfn.parts[3]      == '.png'
        mfn.getFileName() == 'abc def .png'
        mfn.getURL()      == null
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.PNG
    }

    def 'abc def (2).png'() {
        when:
        MaterialFileName mfn = new MaterialFileName('abc def (2).png')
        then:
        mfn.parts[0]      == 'abc def (2).png'
        mfn.parts[1]      == 'abc def '
        mfn.parts[2]      == '(2)'
        mfn.parts[3]      == '.png'
        mfn.getFileName() == 'abc def (2).png'
        mfn.getURL()      == null
        mfn.getSuffix()   == new Suffix(2)
        mfn.getFileType() == FileType.PNG
    }

    def testHttps() {
        when:
        MaterialFileName mfn = new MaterialFileName('https%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        mfn.parts[0]      == 'https%3A%2F%2Fdemoaut.katalon.com%2F.png'
        mfn.parts[1]      == 'https%3A%2F%2Fdemoaut.katalon.com%2F'
        mfn.parts[2]      == null
        mfn.parts[3]      == '.png'
        mfn.getFileName() == 'https%3A%2F%2Fdemoaut.katalon.com%2F.png'
        mfn.getURL().toString()      == new URL('https://demoaut.katalon.com/').toString()
        mfn.getSuffix()   == Suffix.NULL
        mfn.getFileType() == FileType.PNG
    }


    def testFormat() {
        when:
        String fileName = MaterialFileName.format(
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        then:
        fileName.toString().contains('http%3A%2F%2Fdemoaut.katalon.com%2F(1).png')
    }

    def testFormatEncoded() {
        when:
        String fileName = MaterialFileName.formatEncoded(
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        then:
        fileName.toString().contains('http%253A%252F%252Fdemoaut.katalon.com%252F(1).png')
    }

}
