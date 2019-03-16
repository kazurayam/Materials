package com.kazurayam.markupbuilder

import groovy.xml.MarkupBuilder

/**
 * http://d.hatena.ne.jp/yamap_55/20121221/1356095233
 * 
 * @author urayamakazuaki
 *
 */
class ModularizedXmlBuilder {
    
    /**
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
     * @return
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
    
    static String generate3() {
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        def c = { n ->
            car {
                name(n)
            }
        }
        c.delegate = mb
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
