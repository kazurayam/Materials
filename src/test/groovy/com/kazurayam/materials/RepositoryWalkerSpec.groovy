package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.RepositoryFileScanner
import com.kazurayam.materials.RepositoryRoot
import com.kazurayam.materials.RepositoryVisitorSimpleImpl
import com.kazurayam.materials.RepositoryWalker

import groovy.json.JsonOutput
import spock.lang.Ignore
import spock.lang.Specification

class RepositoryWalkerSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryWalkerSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static RepositoryRoot repoRoot_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(RepositoryWalkerSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path materials = workdir_.resolve('Materials')
        RepositoryFileScanner scanner = new RepositoryFileScanner(materials)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods

    @Ignore
    def testPrintRepositoryRoot() {
        when:
        logger_.debug("#testPrintRepositoryRoot repoRoot_.toJson():\n" + JsonOutput.prettyPrint(repoRoot_.toJson()))
        then:
        false
    }

    def testWalkRepositoryRoot() {
        when:
        StringWriter sw = new StringWriter()
        RepositoryVisitorSimpleImpl rv = new RepositoryVisitorSimpleImpl(sw)
        RepositoryWalker.walkRepository(repoRoot_, rv)
        String output = sw.toString()
        then:
        output.contains('preVisitRepositoryRoot')
        output.contains('postVisitRepositoryRoot')
        output.contains('preVisitTSuiteResult')
        output.contains('postVisitTSuiteResult')
        output.contains('preVisitTCaseResult')
        output.contains('postVisitTCaseResult')
        output.contains('visitMaterial')
        //output.contains('visitMaterialFailed')    // this will not be the case normally
    }
}