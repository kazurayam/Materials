package com.kazurayam.kstestresults

import spock.lang.Specification

class FileTypeSpec extends Specification {

    def testToString() {
        expect:
        FileType.PNG.toString() == 'png'
    }

    def testGetExtension() {
        expect:
        FileType.PNG.getExtension() == 'png'
    }

    def testGetMimeType() {
        expect:
        FileType.PNG.getMimeType() == 'image/png'
    }

    def testGetByExtension() {
        expect:
        FileType.getByExtension('png') == FileType.PNG
    }

    def testGetByMimeType() {
        expect:
        FileType.getByMimeType('image/png') == FileType.PNG
    }

}
