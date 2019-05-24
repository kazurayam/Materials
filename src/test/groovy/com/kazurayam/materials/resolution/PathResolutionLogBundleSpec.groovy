package com.kazurayam.materials.resolution

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp

import groovy.json.JsonSlurper

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
            String materialPath = materials.resolve('Monitor47News').
                                            resolve('20190123_153854').
                                                resolve(tCaseName.getValue()).
                                                    resolve('47NEWS_TOP.png').
                                                        normalize().toString()
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
            Path reports = caseOutputDir.resolve('Reports')
            Files.createDirectories(reports)
            Path materials = caseOutputDir.resolve('Materials')
            Path monitor47NewsDir = materials.resolve('Monitor47News')
            Files.createDirectories(monitor47NewsDir)
            Helpers.copyDirectory(
                fixtureDir_.resolve('Materials').resolve('Monitor47News'),
                monitor47NewsDir)
            MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        when:
			TSuiteResult tsr = mr.getTSuiteResult(TSuiteResultId.newInstance(
													new TSuiteName('Monitor47News'),
													new TSuiteTimestamp('20190123_153854')))
			TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/visit47NEWS'))
			assert tcr != null
			assert tcr.getMaterialList().size() > 0
			Material mate = tcr.getMaterialList().get(0)
			PathResolutionLog resolution = new PathResolutionLogImpl(
                InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                tcr.getTCaseName(),
                mate.getHrefRelativeToRepositoryRoot()
            )
            //
            resolution.setSubPath('')
            URL url = new URL('https://www.47news.jp/47NEWS_TOP.png')
            resolution.setUrl(url)
            //
            PathResolutionLogBundle bundle = new PathResolutionLogBundle()
            bundle.add(resolution)
            //
            Path serialized = tsr.getTSuiteTimestampDirectory().resolve(PathResolutionLogBundle.SERIALIZED_FILE_NAME)
            OutputStream os = new FileOutputStream(serialized.toFile())
            Writer writer = new OutputStreamWriter(os, "UTF-8")
            bundle.serialize(writer)
        then:
            Files.exists(serialized)
        when:
            PathResolutionLogBundle deserialized = PathResolutionLogBundle.deserialize(serialized)
        then:
            deserialized != null
		when:
			// now we will check the content of the deserialized 'path-resolution-log-bundle.json'
			// we will be specifically interested in the MaterialPath property to be relative to the baseDir of MaterialRepository instance
			Path serialized2 = tsr.getTSuiteTimestampDirectory().resolve('serialized2.json')
			OutputStream os2 = new FileOutputStream(serialized2.toFile())
			Writer writer2 = new OutputStreamWriter(os2, "UTF-8")
			bundle.serialize(writer2)
		then:
			Files.exists(serialized2)
		when:  // the MaterialPath is expected to be relative to the baseDir of MaterialRepository, = the 'Materials' directory
			def jsonObject = new JsonSlurper().parse(serialized2.toFile())
			String mp = jsonObject.PathResolutionLogBundle[0].PathResolutionLog.MaterialPath
		then:
			// should not be "build\\tmp\\testOutput\\PathResolutionLogBundleSpec\\testSerializeAndDeserialize\\Materials\\Monitor47News\\20190123_153854\\main.TC1\\47NEWS_TOP.png"
			// should rather be "Monitor47News\\20190123_153854\\main.TC1\\47NEWS_TOP.png"
			mp.startsWith('Monitor47News')
    }
}
