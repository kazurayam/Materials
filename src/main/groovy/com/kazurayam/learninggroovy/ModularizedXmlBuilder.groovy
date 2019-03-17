package com.kazurayam.learninggroovy

import groovy.xml.MarkupBuilder

/**
 * http://d.hatena.ne.jp/yamap_55/20121221/1356095233
 * 
 * @author urayamakazuaki
 *
 */
class ModularizedXmlBuilder {
    
    /**
     * The methods here generates the following string in XML using
     * Groovy's MarkupBuilder.
     * <PRE>
     * <root>
  <id>1</id>
  <user>
    <name>yamada</name>
    <car>
      <name>car1</name>
    </car>
  </user>
  <company>
    <name>kaisha</name>
    <car>
      <name>car2</name>
    </car>
  </company>
</root>
     * </PRE>
     * I want to find out a way to modularize the method generate1, which is
     * monolithic. I could do it using closures and delegate.
     */
    static String generate1() {
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.root(){
            id(1)
            user {
                name("yamada")
                car{
                    name("car1")
                }
            }
            company {
                name("kaisha")
                car {
                    name("car2")
                }
            }
        }
        return sw.toString()
    }
    
    static String generate2() {
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.root(){
            def c = { n ->
                car {
                    name(n)
                }
            }
            id(1)
            user {
                name("yamada")
                c("car1")
            }
            company {
                name("kaisha")
                c("car2")
            }
        }
        return sw.toString()
    }
    
    /**
     * Groovy's MarkupBuidler is nice. It is even more powerfull if you
     * modularize the code by closures. Callee closure is linked with
     * caller closure by setting the caller reference to the delegate 
     * property to the callee.
     * This test case shows an example of this practice.
     */
    static String generate3() {
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        def c = { n ->
            car {
                name(n)
            }
        }
        c.delegate = mb   // Here is the magic.
        //
        mb.root(){
            id(1)
            user {
                name("yamada")
                c("car1")
            }
            company {
                name("kaisha")
                c("car2")
            }
        }
        return sw.toString()
    }
}
