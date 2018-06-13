package com.kazurayam.carmina

import spock.lang.Specification

class TCaseNameSpec extends Specification {

    /**
     * 'Test Suites/TS1' ==> 'TS1'
     */
    def testStripParentDir() {
        setup:
        TSuiteName tsn = new TSuiteName('foo/bar/TS1')
        when:
        String name = tsn.getValue()
        then:
        name == 'TS1'
    }

    def testStripParentDir2() {
        setup:
        TSuiteName tsn = new TSuiteName('foo\\bar\\TS1')
        when:
        String name = tsn.getValue()
        then:
        name == 'TS1'
    }

    /**
     * '§A' ==> '%C2%A7A'
     */
    def testEncoding() {
        setup:
        TSuiteName tsn = new TSuiteName('§A')
        when:
        String name = tsn.getValue()
        then:
        name == '§A'
    }
}
