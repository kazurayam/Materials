package com.kazurayam.markupbuilder

import com.kazurayam.markupbuilder.JsonToXmlConverter.KeyValue

import spock.lang.Ignore
import spock.lang.Specification

/**
 * Learning http://npnl.hatenablog.jp/entry/20110315/1300210292
 * 
 * @author kazurayam
 *
 */
class JsonToXmlConverterSpec extends Specification {
    
    def list = [
        new KeyValue("key1","value1"),
        new KeyValue("key2","value2"),
        new KeyValue("key3", [
            new KeyValue("key3-1","value3-1"),
            new KeyValue("key3-2","value3-2")
            ])
        ]

    def expect ='''<langs type="current">
  <key1>value1</key1>
  <key2>value2</key2>
  <key3>
    <key3-1>value3-1</key3-1>
    <key3-2>value3-2</key3-2>
  </key3>
</langs>'''

    @Ignore
    def testConvertListToJson() {
        expect:
        expect.equals(JsonToXmlConverter.convert(list))
    }
}


