package com.kazurayam.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import spock.lang.Ignore
import spock.lang.Specification

class IndexerByVisitorPatternImplSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(IndexerByVisitorPatternImplSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(IndexerByVisitorPatternImplSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    @Ignore
    def testSmoke() {
        setup:
        IndexerByVisitorPatternImpl indexer = new IndexerByVisitorPatternImpl()
        Path materials = workdir_.resolve('Materials')
        indexer.setBaseDir(materials)
        Path index = materials.resolve('index.html')
        indexer.setOutput(index)
        when:
        indexer.execute()
        then:
        Files.exists(index)
        when:
        String content = index.toFile().text
        then:
        content.contains('<html')
    }

    def testHtmlFragmentsOfMaterialsAsModal() {
        setup:
        Path materials = workdir_.resolve('Materials')
        RepositoryFileScanner scanner = new RepositoryFileScanner(materials)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        StringWriter htmlFragments = new StringWriter()
        def htmlVisitor = new IndexerByVisitorPatternImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlFragments)
        RepositoryWalker.walkRepository(repoRoot, htmlVisitor)
        when:
        String content = htmlFragments.toString()
        logger_.debug("#testHtmlFragmentsOfMaterialsAsModal content=${content}")
        then:
        content.contains('Hello World')
    }

    def testBootstrapTreeviewData() {
        setup:
        Path materials = workdir_.resolve('Materials')
        RepositoryFileScanner scanner = new RepositoryFileScanner(materials)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        StringWriter jsonSnippet = new StringWriter()
        def jsonVisitor = new IndexerByVisitorPatternImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(jsonSnippet)
        RepositoryWalker.walkRepository(repoRoot, jsonVisitor)
        when:
        String content = jsonSnippet.toString()
        logger_.debug("#testBootstrapTreeviewData content=${JsonOutput.prettyPrint(content)}")
        then:
        content.contains('Hello World')
    }

    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
