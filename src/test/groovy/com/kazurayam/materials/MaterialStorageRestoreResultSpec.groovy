package com.kazurayam.materials

import com.kazurayam.materials.impl.TSuiteResultImpl
import com.kazurayam.materials.impl.MaterialStorageRestoreResultImpl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

class MaterialStorageRestoreResultSpec extends Specification {
	
	// fields
	static Logger logger_ = LoggerFactory.getLogger(MaterialStorageSpec)
	
	def setupSpec() {}
	def setup() {}
	def cleanup() {}
	def cleanupSpec() {}
	
	def test_smoke() {
		setup:
		TSuiteName tsName = new TSuiteName('Test Suites/TS1')
		TSuiteTimestamp tsTimestamp = new TSuiteTimestamp('20190621_150102')
		TSuiteResult tSuiteResult = new TSuiteResultImpl(tsName, tsTimestamp)
		int count = 10
		when:
		MaterialStorageRestoreResult restoreResult = 
			new MaterialStorageRestoreResultImpl(tSuiteResult, count)
		then:
		restoreResult.getTSuiteResult().getTSuiteName() == tsName
		restoreResult.getTSuiteResult().getTSuiteTimestamp() == tsTimestamp
		restoreResult.getCount() == count
		
	}
}
