package com.kazurayam.kstestresults

import spock.lang.Specification

class FileExtensionSpec extends Specification {

    def testToString() {
        expect:
        FileExtension.PNG.toString() == 'png'
    }

    def testGetExtension() {
        expect:
        FileExtension.PNG.getExtension() == 'png'
    }

    def testGetMimeType() {
        expect:
        FileExtension.PNG.getMimeType() == 'image/png'
    }

    def testGetByExtension() {
        expect:
        FileExtension.getByExtension('png') == FileExtension.PNG
    }

    def testGetByMimeType() {
        expect:
        FileExtension.getByMimeType('image/png') == FileExtension.PNG
    }

}
