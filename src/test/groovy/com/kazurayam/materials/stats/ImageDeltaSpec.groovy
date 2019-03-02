package com.kazurayam.materials.stats

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.TSuiteTimestamp

import spock.lang.Specification

class ImageDeltaSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDeltaSpec.class)

    // fields
    private TSuiteTimestamp a
    private TSuiteTimestamp b
    private double          d
    
    // fixture methods
    def setupSpec() {}
    def setup() {
        a = new TSuiteTimestamp("20190216_204329")
        b = new TSuiteTimestamp("20190216_064354")
        d = 18.62
    }
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testEquals_normal() {
        setup:
        TSuiteTimestamp otherA = new TSuiteTimestamp("20190216_204329")
        TSuiteTimestamp otherB = new TSuiteTimestamp("20190216_064354")
        double          otherD = 18.62
        when:
        ImageDelta imageDelta = new ImageDelta(a, b, d)
        ImageDelta otherDelta = new ImageDelta(otherA, otherB, otherD)
        then:
        imageDelta.equals(otherDelta)
        otherDelta.equals(imageDelta)
    }
    
    def testEquals_differentA() {
        setup:
        TSuiteTimestamp otherA = new TSuiteTimestamp()
        TSuiteTimestamp otherB = new TSuiteTimestamp("20190216_064354")
        double          otherD = 18.62
        when:
        ImageDelta imageDelta = new ImageDelta(a, b, d)
        ImageDelta otherDelta = new ImageDelta(otherA, otherB, otherD)
        then:
        ! imageDelta.equals(otherDelta)
        ! otherDelta.equals(imageDelta)
    }
    
    def testEquals_differentB() {
        setup:
        TSuiteTimestamp otherA = new TSuiteTimestamp("20190216_204329")
        TSuiteTimestamp otherB = new TSuiteTimestamp()
        double          otherD = 18.62
        when:
        ImageDelta imageDelta = new ImageDelta(a, b, d)
        ImageDelta otherDelta = new ImageDelta(otherA, otherB, otherD)
        then:
        ! imageDelta.equals(otherDelta)
        ! otherDelta.equals(imageDelta)
    }
    
    def testEquals_differentD() {
        setup:
        TSuiteTimestamp otherA = new TSuiteTimestamp("20190216_204329")
        TSuiteTimestamp otherB = new TSuiteTimestamp("20190216_064354")
        double          otherD = 0.0
        when:
        ImageDelta imageDelta = new ImageDelta(a, b, d)
        ImageDelta otherDelta = new ImageDelta(otherA, otherB, otherD)
        then:
        ! imageDelta.equals(otherDelta)
        ! otherDelta.equals(imageDelta)
    }
    
    def testHashCode() {
        when:
        ImageDelta imageDelta = new ImageDelta(a, b, d)
        then:
        imageDelta.hashCode() == -1219059235
    }
    
    
}
