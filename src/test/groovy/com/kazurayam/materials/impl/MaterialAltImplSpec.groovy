package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TCaseResult

import spock.lang.Specification

class MaterialAltImplSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialAltImplSpec.class)

    private static Path fixtureDir
    private static Path specOutputDir
    

    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(MaterialAltImpl.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def test_core_constructor() {
        setup:
            Path caseOutputDir = specOutputDir.resolve('test_core_constructor')
            Path materials = caseOutputDir.resolve('Materials')
            Files.createDirectories(materials)
            Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        when:
            String jsonText = '''{
    "Material": {
        "url": "null",
        "suffix": "",
        "fileType": {
            "FileType": {
                "extension": "png",
                "mimeTypes": [
                    "image/png"
                ]
            }
        },
        "path": "build/tmp/testOutput/ComparisonResultBundleSpec/test_deserializeToJsonObject/Materials/47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png",
        "lastModified": "2019-02-22T22:22:11"
     }
'''
            Path baseDir = 
            Material mate = new MaterialAltImpl(materials, jsonText)
        then:
            mate != null
    }
    
    def test_constructor_with_URL() {
        when:
            Path dirpath =
            URL url =
            Suffix suffix = 
            FileType fileType = 
            Material mate = new MaterialAltImpl(dirpath, url, suffix, fileType)
        then:
            mate != null
    }
    
    def test_constructor_with_TCaseResult() {
        when:
            TCaseResult parent = 
            Path filePath = 
            Material mate = new MaterialAltImpl(parent, filePath)
        then:
            mate != null
    }
}
