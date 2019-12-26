package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import com.kazurayam.materials.Helpers
import spock.lang.Specification

class MaterialStorageImplSpec extends Specification {

	private static Path workdir_
	private static String classShortName_ = Helpers.getClassShortName(MaterialStorageImplSpec.class)

	// fixture methods
	def setupSpec() {
		workdir_ = Paths.get("./build/tmp/testOutput/${classShortName_}")
		if (Files.exists(workdir_)) {
			Helpers.deleteDirectoryContents(workdir_)
		}
		Files.createDirectories(workdir_)
	}
	def setup() {}
	def cleanup() {}
	def cleanupSpec() {}
	
	// feature methods
	/**
	 * Reproduce the problem reported by
	 * https://forum.katalon.com/t/cannot-invoke-method-gettcaseresultlist-on-null-object/37890/
	 */
	def test_StorageDirWithIrregularContents() {
		expect:
		assert 1 != 1
	}
	
}
