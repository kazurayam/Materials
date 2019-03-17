package com.kazurayam.learninggroovy

/**
 * http://npnl.hatenablog.jp/entry/20110315/1300210292
 * 
 * @author kazurayam
 */
class JsonToXmlConverter {
    
    static String convert(List list) {
        StringWriter sw = new StringWriter()
        def xml = new groovy.xml.MarkupBuilder(sw)
        xml.doubleQuotes = true
        xml.langs(type: "current") {
            list.each{
                switch (it.value){
                    case String : "${it.key}"(it.value)
                      break
                    default : "${it.key}"(it.value)
                }
    
            }
        }
        return sw.toString();
    }
    
    static String recursiveConvert(List list) {
        
    }

    static class KeyValue {
        def key
        def value
        KeyValue(key, value){
            this.key = key
            this.value = value
        }
    }
}