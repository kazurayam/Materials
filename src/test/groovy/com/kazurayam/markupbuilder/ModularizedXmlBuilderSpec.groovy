package com.kazurayam.markupbuilder

import spock.lang.Ignore
import spock.lang.Specification

class ModularizedXmlBuilderSpec extends Specification {
    
    @Ignore
    def test_generate1() {
        when:
        String s = ModularizedXmlBuilder.generate1()
        then:
        s == ""
    }
    
    @Ignore
    def test_generate2() {
        when:
        String s = ModularizedXmlBuilder.generate2()
        then:
        s == ""
    }
    
    def test_generate3() {
        when:
        String s = ModularizedXmlBuilder.generate3()
        then:
        s == ""
    }
    
}
