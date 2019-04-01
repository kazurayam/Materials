package com.kazurayam.materials.resolution

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TCaseName

import spock.lang.Specification

class PathResolutionLogBundleSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(PathResolutionLogBundleSpec.class)
    
    // fields
    private static Path fixtureDir_ = Paths.get("./src/test/fixture")
    private static Path specOutputDir_
    
    // fixture methods
    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(PathResolutionLogBundleSpec.class)}")
        if (! Files.exists(specOutputDir_)) {
            Files.createDirectories(specOutputDir_)
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSmoke() {
        setup:
            Path caseOutputDir = specOutputDir_.resolve('testConstructor')
            Path materials = caseOutputDir.resolve('Materials')
            Path monitor47NewsDir = materials.resolve('Monitor47News')
            Files.createDirectories(monitor47NewsDir)
            Helpers.copyDirectory(
                fixtureDir_.resolve('Materials').resolve('Monitor47News'),
                monitor47NewsDir)
        when:
            TCaseName tCaseName = new TCaseName('Test Cases/main/TC1')
            Path materialPath = materials.resolve('Monitor47News').
                                            resolve('20190123_153854').
                                                resolve(tCaseName.getValue()).
                                                    resolve('47NEWS_TOP.png').
                                                        normalize()
            PathResolutionLog resolution = new PathResolutionLogImpl(
                InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                tCaseName,
                materialPath
            )
            //
            resolution.setSubPath('')
            URL url = new URL('https://www.47news.jp/47NEWS_TOP.png')
            resolution.setUrl(url)
        then:
            resolution.getUrl() == url
        when:
            PathResolutionLogBundle bundle = new PathResolutionLogBundle()
            bundle.add(resolution)
        then:
            bundle.size() == 1
        when:
            List<PathResolutionLog> list = bundle.findByMaterialPath(materialPath)
        then:
            list.size()== 1
        when:
            PathResolutionLog last = bundle.findLastByMaterialPath(materialPath)
        then:
            last != null
            last.getMaterialPath() == materialPath
            last.getUrl()== url
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
            Path tSuiteResultPath = materials.resolve('Monitor47News').
                                            resolve('20190123_153854')
            Path materialPath = tSuiteResultPath.
                                    resolve(tCaseName.getValue()).
                                        resolve('47NEWS_TOP.png').normalize()
            PathResolutionLog resolution = new PathResolutionLogImpl(
                InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                tCaseName,
                materialPath
            )
            //
            resolution.setSubPath('')
            URL url = new URL('https://www.47news.jp/47NEWS_TOP.png')
            resolution.setUrl(url)
            //
            PathResolutionLogBundle bundle = new PathResolutionLogBundle()
            bundle.add(resolution)
            //
            Path serialized = tSuiteResultPath.resolve(PathResolutionLogBundle.SERIALIZED_FILE_NAME)
            OutputStream os = new FileOutputStream(serialized.toFile())
            Writer writer = new OutputStreamWriter(os, "UTF-8")
            bundle.serialize(writer)
        then:
            Files.exists(serialized)
        when:
            PathResolutionLogBundle deserialized = PathResolutionLogBundle.deserialize(serialized)
        then:
            deserialized != null
            
    }
}
