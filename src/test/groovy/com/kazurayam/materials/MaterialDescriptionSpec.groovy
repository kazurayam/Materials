package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import spock.lang.Specification

class MaterialDescriptionSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialDescriptionSpec.class)

    // fields

    // fixture methods
    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def test_toJsonText() {
        when:
        MaterialDescription md = new MaterialDescription("category\\text","description\\text")
        String json = md.toJsonText()
        println JsonOutput.prettyPrint(json)
        then:
        json.contains('category\\\\text')
        json.contains('description\\\\text')
        when:
        def slurper = new JsonSlurper()
        def obj = slurper.parseText(json)
        then:
        obj.category == 'category\\text'
        obj.description == 'description\\text'
    }

}
