package com.kazurayam.carmina.material

import java.util.regex.Matcher

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Ignore
import spock.lang.Specification

class MaterialFileNameFormatterSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialFileNameFormatterSpec.class)

    def testParseFileNameForFileType_png() {
        when:
        FileType ft = MaterialFileNameFormatter.parseFileNameForFileType('a.png')
        then:
        ft == FileType.PNG
    }

    def testParseFileNameForFileType_none() {
        when:
        FileType ft = MaterialFileNameFormatter.parseFileNameForFileType('a')
        then:
        ft == FileType.NULL
    }

    def testParseFileNameForFileType_unknown() {
        when:
        FileType ft = MaterialFileNameFormatter.parseFileNameForFileType('a.foo')
        then:
        ft == FileType.UNSUPPORTED
    }

    def testPTN_SUFFIX_withoutLeadingWhiteSpace() {
        when:
        Matcher m = MaterialFileNameFormatter.PTN_SUFFIX.matcher('abc(2)')
        then:
        m.matches() == true
        m.group(1) == 'abc'
        m.group(2) == '2'
    }

    def testPTN_SUFFIX_withLeadingWhiteSpace() {
        when:
        Matcher m = MaterialFileNameFormatter.PTN_SUFFIX.matcher('abc (2)')
        then:
        m.matches() == true
        m.group(1) == 'abc '   // note a white space is included in the group(1)
        m.group(2) == '2'
    }

    def testPTN_SUFFIX_MaxInt() {
        when:
        Matcher m = MaterialFileNameFormatter.PTN_SUFFIX.matcher('abc (2147483647)')
        then:
        m.find() == true
        m.group(1) == 'abc '
        m.group(2) == '2147483647'
    }

    def testParseFileNameForSuffix_1() {
        when:
        Suffix suffix = MaterialFileNameFormatter.parseFileNameForSuffix('abc (1).png')
        then:
        suffix == new Suffix(1)
        suffix.toString() == '(1)'
        suffix.toString() != ' (1)'
    }

    def testParseFileNameForSuffix_withoutLeadingWhiteSpace() {
        when:
        Suffix suffix = MaterialFileNameFormatter.parseFileNameForSuffix('a(3).png')
        then:
        suffix == new Suffix(3)
        suffix.toString() == '(3)'
    }

    def testParseFileNameForSuffix_withLeadingWhiteSpace() {
        when:
        Suffix suffix = MaterialFileNameFormatter.parseFileNameForSuffix('a (3).png')
        then:
        suffix == new Suffix(3)
        suffix.toString() == '(3)'
    }

    def testParseFileNameForSuffix_none() {
        when:
        Suffix suffix = MaterialFileNameFormatter.parseFileNameForSuffix('a.png')
        then:
        suffix == Suffix.NULL
        //
        when:
        suffix = MaterialFileNameFormatter.parseFileNameForSuffix('foo')
        then:
        suffix == Suffix.NULL
        //
        when:
        suffix = MaterialFileNameFormatter.parseFileNameForSuffix('a(b).png')
        then:
        suffix == Suffix.NULL
    }

    def testParseFileNameForBody_withoutSuffix_nonURL() {
        when:
        String fileNameBody = MaterialFileNameFormatter.parseFileNameForBody('abc def .png')
        then:
        fileNameBody == 'abc def '
    }

    def testParseFileNameForBody_withSuffix_nonURL() {
        when:
        String fileNameBody = MaterialFileNameFormatter.parseFileNameForBody('abc def (2).png')
        then:
        fileNameBody == 'abc def '
    }

    def testParseFileNameForBody_withoutSuffix_validURL() {
        when:
        String fileNameBody = MaterialFileNameFormatter.parseFileNameForBody('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        fileNameBody == 'http%3A%2F%2Fdemoaut.katalon.com%2F'
    }

    def testParseFileNameForBody_withSuffix_validURL() {
        when:
        String fileNameBody = MaterialFileNameFormatter.parseFileNameForBody('http%3A%2F%2Fdemoaut.katalon.com%2F(1).png')
        then:
        fileNameBody == 'http%3A%2F%2Fdemoaut.katalon.com%2F'
    }

    def testParseFileNameForBody_leadingWhitespace_withSuffix_validURL() {
        when:
        String fileNameBody = MaterialFileNameFormatter.parseFileNameForBody('http%3A%2F%2Fdemoaut.katalon.com%2F (1).png')
        then:
        fileNameBody == 'http%3A%2F%2Fdemoaut.katalon.com%2F '
    }

    def testParseFileNameForURL_http() {
        when:
        URL url = MaterialFileNameFormatter.parseFileNameForURL('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        url.toString() == new URL('http://demoaut.katalon.com/').toString()
    }

    def testParseFileNameForURL_https() {
        when:
        URL url = MaterialFileNameFormatter.parseFileNameForURL('https%3A%2F%2Fwww.google.com%2F.png')
        then:
        url.toString() == new URL('https://www.google.com/').toString()
    }

    def testParseFileNameForURL_withSuffix() {
        when:
        URL url = MaterialFileNameFormatter.parseFileNameForURL('https%3A%2F%2Fwww.google.com%2F (1).png')
        then:
        url.toString() == new URL('https://www.google.com/').toString()
    }

    def testParseFileNameForURL_Malformed() {
        when:
        URL url = MaterialFileNameFormatter.parseFileNameForURL('this_is_unexpected_file_name.png')
        then:
        url == null
    }


    def testFormat() {
        when:
        String fileName = MaterialFileNameFormatter.format(
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        then:
        fileName.toString().contains('http%3A%2F%2Fdemoaut.katalon.com%2F(1).png')
    }
    
    def testFormatEncoded() {
        when:
        String fileName = MaterialFileNameFormatter.formatEncoded(
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        then:
        fileName.toString().contains('http%253A%252F%252Fdemoaut.katalon.com%252F(1).png')
    }
}
