package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Specification

class ImageDiffStatsSpec extends Specification {
    static Logger logger_ = LoggerFactory.getLogger(ImageDiffStatsSpec.class)
    def testToString() {
        when:
        String s = ImageDiffStats.ZERO.toString()
        String pp = JsonOutput.prettyPrint(s)
        logger_.debug("ImageDiffStats.ZERO.toString():\n${pp}")
        then:
        s.contains("0.0")
    }
}
