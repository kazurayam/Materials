package com.kazurayam.materials.metadata

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.MaterialCoreImpl
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import spock.lang.IgnoreRest
import spock.lang.Specification

class MaterialMetadataBundleCacheSpec extends Specification {

	static Logger logger_ = LoggerFactory.getLogger(MaterialMetadataBundleCache.class)
	
	// fields
	private static Path fixtureDir_ = Paths.get("./src/test/fixture_origin")
	private static Path specOutputDir_
    private static Path materials_
    private static RepositoryRoot repoRoot_
	
	// fixture methods
    def setupSpec() {
        specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialMetadataBundleCacheSpec.class)}")
        Files.createDirectories(specOutputDir_)
        Helpers.copyDirectory(fixtureDir_, specOutputDir_)
        materials_ = specOutputDir_.resolve("Materials")
        RepositoryFileScanner rfs = new RepositoryFileScanner(materials_)
        rfs.scan()
        repoRoot_ = rfs.getRepositoryRoot()
    }
	def setup() {}
	def cleanup() {}
	def cleanupSpec() {}
	
	// feature methods
    @IgnoreRest
    def test_toJsonText() {
        setup:
        MaterialMetadataBundleCache cache = new MaterialMetadataBundleCache()
        when:
        println "#test_toJsonText just constructed: ${JsonOutput.prettyPrint(cache.toJsonText())}"
        then:
        cache.toJsonText().contains("MaterialMetadataBundleCache")
        //
        when:
        List<Material> mlist111956 = repoRoot_.getMaterials(new TSuiteName("47news.chronos_capture"), new TSuiteTimestamp("20190404_111956"))
        assert mlist111956.size() == 1
        Path p111956 = materials_.resolve("47news.chronos_capture/20190404_111956/material-metadata-bundle.json")
        MaterialMetadataBundle mmb111956 = cache.get(p111956)
        println "#test_toJsonText added 111956 constructed: ${JsonOutput.prettyPrint(cache.toJsonText())}"
        then:
        cache.toJsonText().contains("111956")
        //
        when:
        List<Material> mlist112053 = repoRoot_.getMaterials(new TSuiteName("47news.chronos_capture"), new TSuiteTimestamp("20190404_112053"))
        assert mlist111956.size() == 1
        Path p112053 = materials_.resolve("47news.chronos_capture/20190404_112053/material-metadata-bundle.json")
        MaterialMetadataBundle mmb112053 = cache.get(p112053)
        println "#test_toJsonText added 1120536 constructed: ${JsonOutput.prettyPrint(cache.toJsonText())}"
        then:
        cache.toJsonText().contains("112053")
        // reproducing the problem
        cache.get(p111956).get(0).getMaterialPath() == "47news.chronos_capture/20190404_111956/47news.visitSite/top.png"
        
    }
    
	def testSmoke() {
		setup:
		MaterialMetadataBundleCache cache = new MaterialMetadataBundleCache()
		
		// check the case of 20190404_111956
		when:
		List<Material> mlist111956 = repoRoot_.getMaterials(new TSuiteName("47news.chronos_capture"), new TSuiteTimestamp("20190404_111956"))
		assert mlist111956.size() == 1
		Path p111956 = materials_.resolve("47news.chronos_capture/20190404_111956/material-metadata-bundle.json")
		MaterialMetadataBundle mmb111956 = cache.get(p111956)
		then:
		mmb111956 != null
		when:
		String matepath111956 = "47news.chronos_capture/20190404_111956/47news.visitSite/top.png"
		MaterialMetadata meta111956 = mmb111956.findLastByMaterialPath(matepath111956)
		then:
		meta111956 != null
		meta111956.getExecutionProfileName() == "product"
		
		// check the case of 20190404_112053
		when:
		List<Material> mlist112053 = repoRoot_.getMaterials(new TSuiteName("47news.chronos_capture"), new TSuiteTimestamp("20190404_112053"))
		assert mlist111956.size() == 1
		Path p112053 = materials_.resolve("47news.chronos_capture/20190404_112053/material-metadata-bundle.json")
		MaterialMetadataBundle mmb112053 = cache.get(p112053)
		then:
		mmb112053 != null
		when:
		MaterialMetadata meta112053 = mmb112053.findLastByMaterialPath("47news.chronos_capture/20190404_112053/47news.visitSite/top.png")
		then:
		meta112053 != null
		meta112053.getExecutionProfileName() == "develop"
		
		// check the case of 20190404_111956 once again; cache should work
		when:
		mmb111956 = cache.get(p111956)
		then:
		mmb111956 != null
		when:
		println "cache=${JsonOutput.prettyPrint(cache.toJsonText())}"
		println "mmb111956=${JsonOutput.prettyPrint(mmb111956.toJsonText())}"
		meta111956 = mmb111956.findLastByMaterialPath(matepath111956)
		then:
		meta111956 != null	// once upon a time, this statement failed
		meta111956.getExecutionProfileName() == "product"
		
	}
}
