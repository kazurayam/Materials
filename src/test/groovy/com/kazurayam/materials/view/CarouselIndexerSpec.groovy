package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers

import spock.lang.Specification

class CarouselIndexerSpec extends Specification {
    
    static Logger logger = LoggerFactory.getLogger(CarouselIndexerSpec.class)
    
    // fields
    static Path specOutputDir
    static Path fixtureDir
    
    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get('.')
        Path testOutputDir = projectDir.resolve('./build/tmp/testOutput')
        specOutputDir = testOutputDir.resolve("${Helpers.getClassShortName(CarouselIndexerSpec.class)}")
        if (specOutputDir.toFile().exists()) {
            Helpers.deleteDirectoryContents(specOutputDir)
        }
        fixtureDir = projectDir.resolve('src').resolve('test').resolve('fixture')
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testSmoke() {
        setup:
        Path caseOutputDir = specOutputDir.resolve('testSmoke')
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(fixtureDir, caseOutputDir)
        CarouselIndexer indexer = makeIndexer(caseOutputDir)
        when:
        indexer.execute()
        Path index = indexer.getOutput()
        then:
        Files.exists(index)
        when:
        String content = index.toFile().text
        then:
        content.contains('<html')
    }
    
    /**
     * helper to make a CarouselIndexer object
     * @param caseOutputDir
     * @return a CarouselIndexer object
     */
    private CarouselIndexer makeIndexer(Path caseOutputDir) {
        Path materialsDir = caseOutputDir.resolve('Materials')
        Path reportsDir   = caseOutputDir.resolve('Reports')
        CarouselIndexer indexer = new CarouselIndexer()
        indexer.setBaseDir(materialsDir)
        indexer.setReportsDir(reportsDir)
        Path index = materialsDir.resolve('index.html')
        indexer.setOutput(index)
        return indexer
    }
}
