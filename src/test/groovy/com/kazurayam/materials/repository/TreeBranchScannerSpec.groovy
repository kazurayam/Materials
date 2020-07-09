package com.kazurayam.materials.repository

import com.kazurayam.materials.TExecutionProfile

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import groovy.json.JsonOutput
import spock.lang.Specification

class TreeBranchScannerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TreeBranchScannerSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TreeBranchScannerSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}


    // feature methods
    def testScan() {
        setup:
        Path casedir = workdir_.resolve("testScan")
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        // scan for the directories "Materials/SuiteName/ExecutionProfile/TSuiteTimestamp"
        // but leave TCaseResults and Materials unrecognized
        TreeTrunkScanner tts = new TreeTrunkScanner(materialsDir)
        when:
        tts.scan()
        RepositoryRoot repoRoot = tts.getRepositoryRoot()
        List<TSuiteResult> tSuiteResults = repoRoot.getTSuiteResults()
        // we want to further scan the filee tree
        // for the branches(screenshots etc) under the yyyyMMdd_hhmmss directory
        for (tsr in tSuiteResults) {
            TreeBranchScanner tbs = new TreeBranchScanner(repoRoot)
            tbs.scan(tsr)
        }
        then:
        repoRoot.getTCaseResult().size() > 0

    }

}
