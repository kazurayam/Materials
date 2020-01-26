package com.kazurayam.materials.metadata

import java.nio.file.StandardCopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.metadata.InvokedMethodName
import com.kazurayam.materials.metadata.MaterialMetadata
import com.kazurayam.materials.metadata.MaterialMetadataImpl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification

class MaterialMetadataSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialMetadataSpec.class)
    
    // fields
    private static Path fixtureDir_ = Paths.get("./src/test/fixture")
    private static Path specOutputDir_
    
    // fixture methods
    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialMetadataSpec.class)}")
        if (! Files.exists(specOutputDir_)) {
            Files.createDirectories(specOutputDir_)
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testConstructor() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve('testConstructor')
        Path materials = caseOutputDir.resolve('Materials')
        Files.createDirectories(materials)
        Helpers.copyDirectory(fixtureDir_.resolve('Materials'), materials)
        when:
        TCaseName tCaseName = new TCaseName('Test Cases/main/TC1')
        String materialPath = 
            materials.resolve('main.TS1')
                .resolve('20180530_130419')
                .resolve(tCaseName.getValue())
                .resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
                .normalize().toString()
        MaterialMetadata metadata = 
            new MaterialMetadataImpl(
                InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                tCaseName,
                materialPath)
        then:
        metadata != null
        metadata.getInvokedMethodName() == InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS
        metadata.getTCaseName() == tCaseName
        metadata.getMaterialPath() == materialPath
    }
    
    def testResolveScreenshotPathByUrlComponents() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve('testResolveScreenshotPathByUrlComponents')
        Path materials = caseOutputDir.resolve('Materials')
        Path monitor47NewsDir = materials.resolve('Monitor47News')
        Files.createDirectories(monitor47NewsDir)
        Helpers.copyDirectory(
            fixtureDir_.resolve('Materials').resolve('Monitor47News'),
            monitor47NewsDir)
        when:
        TCaseName tCaseName = new TCaseName('Test Cases/main/TC1')
        String materialPath = 
            materials.resolve('Monitor47News')
                .resolve('20100123_153854')
                .resolve(tCaseName.getValue())
                .resolve('47NEWS_TOP.png')
                .normalize().toString()
        MaterialMetadata metadata =
            new MaterialMetadataImpl(
                InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                tCaseName,
                materialPath)
        //
        metadata.setSubPath('')
        URL url = new URL('https://www.47news.jp/')
        metadata.setUrl(url)
        then:
        metadata.getSubPath() == ''
        metadata.getUrl() == url
        metadata.getFileName() == null
        when:
        String jsonText = metadata.toJsonText()
        logger_.debug("#testResolveScreenshotPathByUrlComponents jsonText=${jsonText}")
        then:
        jsonText != null
        when:
        JsonSlurper slurper = new JsonSlurper()
        def jsonObject = slurper.parseText(jsonText)
        then:
        jsonObject != null
        when:
        String pp = JsonOutput.prettyPrint(jsonText)
        logger_.debug("#testResolveScreenshotPathByUrlComponents pp=${pp}")
        then:
        pp != null
    }
    
    def testSerializeAndDeserialize() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve('testSerializeAndDeserialize')
        Path materials = caseOutputDir.resolve('Materials')
        Path monitor47NewsDir = materials.resolve('Monitor47News')
        Files.createDirectories(monitor47NewsDir)
        Helpers.copyDirectory(
            fixtureDir_.resolve('Materials').resolve('Monitor47News'),
            monitor47NewsDir)
        when:
        TCaseName tCaseName = new TCaseName('Test Cases/main/TC1')
        Path tSuiteResultPath = 
            materials.resolve('Monitor47News')
                .resolve('20190123_153854')
        String materialPath =
            tSuiteResultPath
                .resolve(tCaseName.getValue())
                .resolve('47NEWS_TOP.png')
                .normalize().toString()
        MaterialMetadata metadata =
            new MaterialMetadataImpl(
                InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                tCaseName,
                materialPath)
        metadata.setSubPath('')
        URL url = new URL('https://www.47news.jp/')
        metadata.setUrl(url)
        metadata.setExecutionProfileName('develop')
        //
        Path serialized = tSuiteResultPath.resolve(MaterialMetadataBundle.SERIALIZED_FILE_NAME)
        OutputStream os = new FileOutputStream(serialized.toFile())
        Writer writer = new OutputStreamWriter(os, "UTF-8")
        metadata.serialize(writer)
        then:
        Files.exists(serialized)
        when:
        JsonSlurper slurper = new JsonSlurper()
        def json = slurper.parseText(serialized.toFile().text)
        then:
        json.MaterialMetadata.TCaseName == "Test Cases/main/TC1"
        json.MaterialMetadata.InvokedMethodName == "resolveScreenshotPathByUrlPathComponents"
        json.MaterialMetadata.SubPath == ""
        json.MaterialMetadata.URL == "https://www.47news.jp/"
        json.MaterialMetadata.ExecutionProfileName == 'develop'
    }

    def testSerializeAndDeserializeWithSubPath() {
        setup:
        Path caseOutputDir = specOutputDir_.resolve('testSerializeAndDeserializeWithSubPath')
        Path materials = caseOutputDir.resolve('Materials')
        Path monitor47NewsDir = materials.resolve('Monitor47News')
        Files.createDirectories(monitor47NewsDir)
        Helpers.copyDirectory(
            fixtureDir_.resolve('Materials').resolve('Monitor47News'),
            monitor47NewsDir)
        // we need to modify fixture so that it has subpath.
        // Create a sub-directory and move png file into it.
        // Just after copied: 
        //    testOutput/MaterialMetadataSpec/testSerializeAndDeserializeWithSubPath/Materials/Monitor47News/20190123_153854/main.visit47NEWS/47NEWS_TOP.png
        //    testOutput/MaterialMetadataSpec/testSerializeAndDeserializeWithSubPath/Materials/Monitor47News/20190123_162053/main.visit47NEWS/47NEWS_TOP.png
        // Change it to:
        //    testOutput/MaterialMetadataSpec/testSerializeAndDeserializeWithSubPath/Materials/Monitor47News/20190123_153854/main.visit47NEWS/dir1/47NEWS_TOP.png
        //    testOutput/MaterialMetadataSpec/testSerializeAndDeserializeWithSubPath/Materials/Monitor47News/20190123_162053/main.visit47NEWS/dir1/47NEWS_TOP.png
        Path dir153854 = monitor47NewsDir.resolve('20190123_153854/main.visit47NEWS')
        Path dir162053 = monitor47NewsDir.resolve('20190123_162053/main.visit47NEWS')
        Path dir153854dir1 = dir153854.resolve('dir1')
        Path dir162053dir1 = dir162053.resolve('dir1')
        Files.createDirectories(dir153854dir1)
        Files.createDirectories(dir162053dir1)
        Files.move(dir153854.resolve('47NEWS_TOP.png'), dir153854dir1.resolve('47NEWS_TOP.png'), StandardCopyOption.REPLACE_EXISTING)
        Files.move(dir162053.resolve('47NEWS_TOP.png'), dir162053dir1.resolve('47NEWS_TOP.png'), StandardCopyOption.REPLACE_EXISTING)
        when:
        TCaseName tCaseName = new TCaseName('Test Cases/main/TC1')
        Path tSuiteResultPath = materials.resolve('Monitor47News').resolve('20190123_153854')
        String materialPath = 
            tSuiteResultPath
                .resolve(tCaseName.getValue())
                .resolve('dir1')
                .resolve('47NEWS_TOP.png')
                .normalize().toString()
        MaterialMetadata metadata = 
            new MaterialMetadataImpl(
                InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                tCaseName,
                materialPath)
        metadata.setSubPath('dir1')   // Bomb!
        metadata.setUrl(new URL('https://www.47news.jp/'))
        metadata.setExecutionProfileName('develop')
        //
        Path serialized = tSuiteResultPath.resolve(MaterialMetadataBundle.SERIALIZED_FILE_NAME)
        OutputStream os = new FileOutputStream(serialized.toFile())
        Writer writer = new OutputStreamWriter(os, "UTF-8")
        metadata.serialize(writer)
        then:
        Files.exists(serialized)
        when:
        JsonSlurper slurper = new JsonSlurper()
        def json = slurper.parseText(serialized.toFile().text)
        then:
        json.MaterialMetadata.TCaseName == "Test Cases/main/TC1"
        json.MaterialMetadata.InvokedMethodName == "resolveScreenshotPathByUrlPathComponents"
        json.MaterialMetadata.SubPath == "dir1"
        json.MaterialMetadata.URL == "https://www.47news.jp/"
        json.MaterialMetadata.ExecutionProfileName == 'develop'
	}
    
    @Ignore
    def testIgnoring() {}

}
