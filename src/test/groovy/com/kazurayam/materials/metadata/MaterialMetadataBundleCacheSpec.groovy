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

import spock.lang.Specification

class MaterialMetadataBundleCacheSpec extends Specification {

	static Logger logger_ = LoggerFactory.getLogger(MaterialMetadataBundleCache.class)
	
	// fields
	private static Path fixtureDir_ = Paths.get("./src/test/fixture_origin")
	private static Path specOutputDir_
	
	// fixture methods
	def setupSpec() {
		specOutputDir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialMetadataBundleCacheSpec.class)}")
		Files.createDirectories(specOutputDir_)
	}
	def setup() {}
	def cleanup() {}
	def cleanupSpec() {}
	
	// feature methods
	def testSmoke() {
		setup:
		Path caseOutputDir = specOutputDir_.resolve("testSmoke")
		Helpers.copyDirectory(fixtureDir_, caseOutputDir)
		Path materials = caseOutputDir.resolve("Materials")
		RepositoryFileScanner rfs = new RepositoryFileScanner(materials)
		rfs.scan()
		RepositoryRoot repoRoot = rfs.getRepositoryRoot()
		//
		MaterialMetadataBundleCache cache = new MaterialMetadataBundleCache()
		
		// check the case of 20190404_111956
		when:
		List<Material> mlist111956 = repoRoot.getMaterials(new TSuiteName("47news.chronos_capture"), new TSuiteTimestamp("20190404_111956"))
		assert mlist111956.size() == 1
		Path p111956 = materials.resolve("47news.chronos_capture/20190404_111956/material-metadata-bundle.json")
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
		List<Material> mlist112053 = repoRoot.getMaterials(new TSuiteName("47news.chronos_capture"), new TSuiteTimestamp("20190404_112053"))
		assert mlist111956.size() == 1
		Path p112053 = materials.resolve("47news.chronos_capture/20190404_112053/material-metadata-bundle.json")
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
