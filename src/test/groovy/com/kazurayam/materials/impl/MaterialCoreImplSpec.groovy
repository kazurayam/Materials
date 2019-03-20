package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialCore

import spock.lang.Specification

class MaterialCoreImplSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialCoreImplSpec.class)

    private static Path fixtureDir
    private static Path specOutputDir
    

    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(MaterialCoreImplSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testSmoke() {
        setup:
            Path caseOutputDir = specOutputDir.resolve('testSmoke')
            Path materials = caseOutputDir.resolve('Materials').normalize()
            Files.createDirectories(materials)
            Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        when:
            String jsonText = '''
{
    "Material": {
        "path": "build/tmp/testOutput/MaterialCoreImplSpec/testSmoke/Materials/47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png"
     }
}
'''
            MaterialCore matec = new MaterialCoreImpl(materials, jsonText)
        then:
            matec != null
            matec.getBaseDir().equals(materials)
            matec.getPath().equals(
                Paths.get("build/tmp/testOutput/MaterialCoreImplSpec/testSmoke/Materials/47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png"))
            matec.getPathRelativeToRepositoryRoot().equals(
                Paths.get("47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png"))
    // ../47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png
    }
    
}
