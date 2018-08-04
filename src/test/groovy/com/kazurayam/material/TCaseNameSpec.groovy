package com.kazurayam.material

import com.kazurayam.material.TSuiteName

import spock.lang.Specification

class TCaseNameSpec extends Specification {

    /**
     * 'Test Cases/TC1' -> 'TC1'
     */
    def testChompPrefix() {
        setup:
        TCaseName tcn = new TCaseName('Test Cases/TC1')
        when:
        String name = tcn.getValue()
        then:
        name == 'TC1'
    }

    /**
     * 'Test Cases/main/TC1' -> 'main.TC1'
     */
    def testFlattenSubdirectory() {
        setup:
        TCaseName tcn = new TCaseName('Test Cases/main/TC1')
        when:
        String name = tcn.getValue()
        then:
        name == 'main.TC1'
    }

    /**
     * ignore whitespaces
     *
     * 'Test Cases/foo bar/baz TC1' -> 'foobar.bazTC1'
     */
    def testIgnoreWhiteSpaces() {
        setup:
        TCaseName tcn = new TCaseName('Test Cases/foo bar /baz TC1 ')
        when:
        String name = tcn.getValue()
        then:
        name == 'foobar.bazTC1'
    }


    /**
     * Non Latin characters
     *
     * 'Test Cases/main/テスト1' -> 'main.テスト1'
     */
    def testNonLatinCharacters() {
        setup:
        TCaseName tcn = new TCaseName('Test Cases/main/テスト1')
        when:
        String name = tcn.getValue()
        then:
        name == 'main.テスト1'
    }
}
