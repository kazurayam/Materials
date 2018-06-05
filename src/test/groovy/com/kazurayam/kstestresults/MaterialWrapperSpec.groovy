package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class MaterialWrapperSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    // fixture methods
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialWrapperSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }

    // feature methods
    def testToJson() {
        setup:
        TsName tsn = new TsName('TS1')
        TcName tcn = new TcName('TC1')
        TsTimestamp tstamp = new TsTimestamp('20180530_130419')
        TestResultsImpl wtrs = new TestResultsImpl(workdir, tsn)
        TsResult tsr = wtrs.getTsResult(tsn, tstamp)
        TcResult tcr = tsr.findOrNewTcResult(tcn)
        assert tcr != null
        TargetURL tp = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        when:
        MaterialWrapper mw = tp.findOrNewMaterialWrapper('', FileExtension.PNG)
        def str = mw.toString()
        System.out.println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"MaterialWrapper":{"materialFilePath":"')
        str.contains(Helpers.escapeAsJsonText(mw.getMaterialFilePath().toString()))
        str.endsWith('"}}')
    }
}
