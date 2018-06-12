package com.kazurayam.carmina

import com.kazurayam.carmina.FileType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class FileTypeSpec extends Specification {

    static Logger logger = LoggerFactory.getLogger(FileTypeSpec.class)

    def testToString() {
        expect:
        FileType.PNG.toString() == '{"FileType":{"extension":"png","mimeType":"image/png"}}'
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
