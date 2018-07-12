package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        ft == FileType.NULL
    }

    def testParseFileNameForSuffix_atoz() {
        when:
        Suffix suffix = MaterialFileNameFormatter.parseFileNameForSuffix('a§atoz.png')
        then:
        suffix == new Suffix('atoz')
    }

    def testParseFileNameForSuffix_Nihonngo() {
        when:
        Suffix suffix = MaterialFileNameFormatter.parseFileNameForSuffix('a§あ.png')
        then:
        suffix == new Suffix('あ')
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
        suffix = MaterialFileNameFormatter.parseFileNameForSuffix('a§b§c.png')
        then:
        suffix == new Suffix('c')
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

    def testParseFileNameForURL_Malformed() {
        when:
        URL url = MaterialFileNameFormatter.parseFileNameForURL('this_is_unexpected_file_name.png')
        then:
        url == null
    }

}
