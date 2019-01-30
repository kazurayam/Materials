package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.model.TCaseResult
import com.kazurayam.materials.model.TSuiteResult
import com.kazurayam.materials.model.repository.RepositoryFileScanner
import com.kazurayam.materials.model.repository.RepositoryRoot
import com.kazurayam.materials.model.repository.RepositoryWalker
import com.kazurayam.materials.view.IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal
import com.kazurayam.materials.view.IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal as HTMLVisitor

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
    def testWalkWithJSONVisitor() {
        setup:
        StringWriter jsonSnippet = new StringWriter()
        IndexerByVisitorImpl.RepositoryVisitorGeneratingBootstrapTreeviewData jsonVisitor =
            new IndexerByVisitorImpl.RepositoryVisitorGeneratingBootstrapTreeviewData(jsonSnippet)
        when:
        // now walk the repository to generate a json text
        RepositoryWalker.walkRepository(repoRoot_, jsonVisitor)
        String content = jsonSnippet.toString()
        logger_.debug("#testBootstrapTreeviewData content=${JsonOutput.prettyPrint(content)}")
        then:
        content.contains("foo/ http://demoaut.katalon.com/ PNG")
    }


     def testJSONVisitorVisitMaterial() {
         setup:
         StringWriter jsonSnippet = new StringWriter()
         IndexerByVisitorImpl.RepositoryVisitorGeneratingBootstrapTreeviewData jsonVisitor =
             new IndexerByVisitorImpl.RepositoryVisitorGeneratingBootstrapTreeviewData(jsonSnippet)
         //
         TSuiteResult tsr = repoRoot_.getTSuiteResult(
             new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
         TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
         Material mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
         when:
         // now test visitMaterial() method
         def result = jsonVisitor.visitMaterial(mate)
         def str = jsonSnippet.toString()
         logger_.debug("#testJsonVisitorVisitMaterial \n${JsonOutput.prettyPrint(str)}")
         then:
         str.startsWith('{"text":"')
         str.contains('http://demoaut.katalon.com/')
         str.endsWith('"}')
     }


    /* -------- testing IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal --------- */
    def testWalkWithHTMLVisitor() {
        setup:
        StringWriter htmlFragments = new StringWriter()
        def htmlVisitor = new IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlFragments)
        when:
        RepositoryWalker.walkRepository(repoRoot_, htmlVisitor)
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

    def testHTMLVisitorMarkupInModalWindow() {
        setup:
        StringWriter htmlSnippet = new StringWriter()
        IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal htmlVisitor =
            new IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlSnippet)
        //
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        Material mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        when:
        String markup = htmlVisitor.markupInModalWindow(mate)
        logger_.debug("#testHTMLVisitorMarkupInModalWindow markup=\n${markup}")
        then:
        markup.contains('<img')
        markup.contains('class="img-fluid"')
        markup.contains(FileType.PNG.getExtension())
    }


    def testToHtmlAsModalWindow_PNG() {
        setup:
        StringWriter htmlSnippet = new StringWriter()
        IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal htmlVisitor =
            new IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlSnippet)
        //
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        Material mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        when:
        def result = htmlVisitor.visitMaterial(mate)
        def str = htmlSnippet.toString()
        logger_.debug("#testToHtmlAsModalWindow_PNG str=${str}")
        then:
        str.startsWith('<div')
        str.contains('<img')
        str.contains(mate.getEncodedHrefRelativeToRepositoryRoot())
    }


    def testToHtmlAsModalWindow_miscellaneousImages() {
        setup:
        StringWriter htmlSnippet = new StringWriter()
        IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal htmlVisitor =
            new IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlSnippet)
        //
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS1'), TSuiteTimestamp.newInstance('20180530_130604'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        expect:
        tcr != null
        tcr.getMaterials().size() == 5
        when:
        htmlSnippet.getBuffer().setLength(0)
        def mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        def result = htmlVisitor.visitMaterial(mate)
        def str = htmlSnippet.toString()
        then:
        str.contains('<img')
        str.contains('.png')
        //
        when:
        htmlSnippet.getBuffer().setLength(0)
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.BMP)
        result = htmlVisitor.visitMaterial(mate)
        str = htmlSnippet.toString()
        then:
        str.contains('<img')
        str.contains('.bmp')
        //
        when:
        htmlSnippet.getBuffer().setLength(0)
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.GIF)
        result = htmlVisitor.visitMaterial(mate)
        str = htmlSnippet.toString()
        then:
        str.contains('<img')
        str.contains('.gif')
        //
        when:
        htmlSnippet.getBuffer().setLength(0)
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.JPEG)
        result = htmlVisitor.visitMaterial(mate)
        str = htmlSnippet.toString()
        then:
        str.contains('<img')
        str.contains('.jpeg')
        //
        when:
        htmlSnippet.getBuffer().setLength(0)
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.JPG)
        result = htmlVisitor.visitMaterial(mate)
        str = htmlSnippet.toString()
        then:
        str.contains('<img')
        str.contains('.jpg')
        //
    }


    def testToHtmlAsModalWindow_CSV() {
        setup:
        StringWriter htmlSnippet = new StringWriter()
        IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal htmlVisitor =
            new IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlSnippet)
        //
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS3'), TSuiteTimestamp.newInstance('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        expect:
        tcr != null
        //
        when:
        String url = 'https://fixturedownload.com/download/csv/fifa-world-cup-2018/japan'
        Material mate = tcr.getMaterial(Paths.get('.'), new URL(url), Suffix.NULL, FileType.CSV)
        def result = htmlVisitor.visitMaterial(mate)
        def str = htmlSnippet.toString()
        then:
        str.contains('3,28/06/2018&nbsp;17:00,Volgograd&nbsp;Stadium,Japan,Poland,Group&nbsp;H,')
    }


    def testToHtmlAsModalWindow_PDF() {
        setup:
        StringWriter htmlSnippet = new StringWriter()
        IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal htmlVisitor =
            new IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlSnippet)
        //
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
            new TSuiteName('Test Suites/main/TS3'), TSuiteTimestamp.newInstance('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        expect:
        tcr != null
        when:
        String url = 'http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf'
        Material mate = tcr.getMaterial(Paths.get('.'), new URL(url), Suffix.NULL, FileType.PDF)
        def result = htmlVisitor.visitMaterial(mate)
        def str = htmlSnippet.toString()
        then:
        str.contains('<object')
        str.contains('type="application/pdf"')
    }


    def testToHtmlAsModalWindow_XLSX() {
        setup:
        StringWriter htmlSnippet = new StringWriter()
        IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal htmlVisitor =
            new IndexerByVisitorImpl.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(htmlSnippet)
        //
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS3'),
                                                    TSuiteTimestamp.newInstance('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        expect:
        tcr != null
        when:
        String url = 'https://fixturedownload.com/download/xlsx/fifa-world-cup-2018/japan'
        Material mate = tcr.getMaterial(Paths.get('.'), new URL(url), Suffix.NULL, FileType.XLSX)
        def result = htmlVisitor.visitMaterial(mate)
        def str = htmlSnippet.toString()
        //logger_.debug("#testToHtmlAsModalWindow_XLSX str=${str}")
        then:
        str.contains('<a ')
        str.contains('Download')
    }


}
