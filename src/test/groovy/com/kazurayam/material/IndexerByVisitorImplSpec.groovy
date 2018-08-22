package com.kazurayam.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.material.IndexerByVisitorImpl.RepositoryVisitorGeneratingBootstrapTreeviewData as JSONVisitor
import com.kazurayam.material.IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal as HTMLVisitor

import groovy.json.JsonOutput
import spock.lang.Ignore
import spock.lang.Specification

class IndexerByVisitorImplSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(IndexerByVisitorImplSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static Path materials_
    private static RepositoryRoot repoRoot_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(IndexerByVisitorImplSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        materials_ = workdir_.resolve('Materials')
        RepositoryFileScanner scanner = new RepositoryFileScanner(materials_)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    @Ignore
    def testSmoke() {
        setup:
        IndexerByVisitorImpl indexer = new IndexerByVisitorImpl()
        indexer.setBaseDir(materials_)
        Path index = materials_.resolve('index.html')
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

    /* --------- testing IndexerByVisitorImpl.RepositoryVisitorGeneratingBootstrapTreeviewData as JSONVisitor ----- */
    def testBootstrapTreeviewData() {
        setup:
        StringWriter jsonSnippet = new StringWriter()
        def jsonVisitor = new JSONVisitor(jsonSnippet)
        RepositoryWalker.walkRepository(repoRoot_, jsonVisitor)
        when:
        String content = jsonSnippet.toString()
        logger_.debug("#testBootstrapTreeviewData content=${JsonOutput.prettyPrint(content)}")
        then:
        content.contains("foo/ http://demoaut.katalon.com/ PNG")
    }

    /* -------- testing IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal --------- */
    def testHtmlFragmentsOfMaterialsAsModal() {
        setup:
        StringWriter htmlFragments = new StringWriter()
        def htmlVisitor = new HTMLVisitor(htmlFragments)
        RepositoryWalker.walkRepository(repoRoot_, htmlVisitor)
        when:
        String content = htmlFragments.toString()
        logger_.debug("#testHtmlFragmentsOfMaterialsAsModal content=${content}")
        then:
        content.contains('foo/bar/')
    }

    def testEscapeHtml() {
        expect:
        HTMLVisitor.escapeHtml("This is a test") == 'This&nbsp;is&nbsp;a&nbsp;test'
        HTMLVisitor.escapeHtml("&") == '&amp;'
        HTMLVisitor.escapeHtml("<") == '&lt;'
        HTMLVisitor.escapeHtml(">") == '&gt;'
        HTMLVisitor.escapeHtml('"') == '&quot;'
        HTMLVisitor.escapeHtml(" ") == '&nbsp;'
        HTMLVisitor.escapeHtml("©") == '&copy;'
        HTMLVisitor.escapeHtml("<xml>") == '&lt;xml&gt;'
    }


    @Ignore
    def testIgnoring() {}

    // helper methods
    def void anything() {}
}
