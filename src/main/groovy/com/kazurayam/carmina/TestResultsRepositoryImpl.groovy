package com.kazurayam.carmina

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.xml.MarkupBuilder

final class TestResultsRepositoryImpl implements TestResultsRepository {

    static Logger logger = LoggerFactory.getLogger(TestResultsRepositoryImpl.class)

    private Path baseDir
    private TSuiteName currentTSuiteName
    private TSuiteTimestamp currentTSuiteTimestamp
    private List<TSuiteResult> tSuiteResults

    static final String IMAGE_FILE_EXTENSION = '.png'

    // ---------------------- constructors & initializer ----------------------

    TestResultsRepositoryImpl(Path baseDir) {
        this(baseDir, TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
    }

    /**
     * You are supposed to call this in the TestListener@BeforeTestSuite as follows:
     *
     * <PRE>
     * import java.nio.file.Path
     * import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
     * import katalonimport com.kms.katalon.core.configuration.RunConfiguration
     * import ScreenshotRepositoryImpl
     * ...
     * class TL {
     *     @BeforeTestSuite
     *     def beforeTestSuite(TestSuiteContext testSuiteContext) {
     *         GlobalVariable.CURRENT_TESTSUITE_ID = testSuiteContext.getTestSuiteId()
     *         Path resultsDir = Paths.get(RunConfiguration.getProjectDir()).resolve('Results')
     *         Helpers.ensureDirs(resultsDir)
     *         TestResults trs =
     *             TestResultsFactory.createInstance(resultsDir, testSuiteContext.getTestSuiteId())
     *         GlobalVariable.TESTRESULTS = trs
     *     }
     * </PRE>
     *
     * @param baseDir
     * @param tsName
     */
    TestResultsRepositoryImpl(Path baseDir, TSuiteName tsName) {
        this(baseDir, tsName, new TSuiteTimestamp())
    }

    /**
     *
     * @param baseDir required
     * @param tsName required
     * @param tsTimestamp required
     */
    TestResultsRepositoryImpl(Path baseDir, TSuiteName tsName, TSuiteTimestamp tsTimestamp) {
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        this.baseDir = baseDir

        // load data from the local disk
        RepositoryScanner scanner = new RepositoryScanner(this.baseDir)
        scanner.scan()
        this.tSuiteResults = scanner.getTSuiteResults()

        // memorize the specified TestSuite
        this.currentTSuiteName = tsName
        this.currentTSuiteTimestamp = tsTimestamp

        // add the specified TestSuite
        TSuiteResult tsr = this.getTSuiteResult(this.currentTSuiteName, this.currentTSuiteTimestamp)
        if (tsr == null) {
            tsr = new TSuiteResult(tsName, tsTimestamp).setParent(baseDir)
        }
        this.addTSuiteResult(tsr)
    }

    // -------------------------- attribute getters & setters ------------------------
    Path getBaseDir() {
        return this.baseDir
    }

    TSuiteName getCurrentTSuiteName() {
        return this.currentTSuiteName
    }

    TSuiteTimestamp getCurrentTSuiteTimestamp() {
        return this.currentTSuiteTimestamp
    }

    // --------------------- create/add/get child nodes -----------------------

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        boolean found = false
        for (TSuiteResult tsr : this.tSuiteResults) {
            if (tsr == tSuiteResult) {
                found = true
            }
        }
        if (!found) {
            this.tSuiteResults.add(tSuiteResult)
        }
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        for (TSuiteResult tsr : this.tSuiteResults) {
            if (tsr.getTSuiteName() == tSuiteName && tsr.getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    // -------------------------- do the business -----------------------------
    /**
     *
     * @param testCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveMaterialFilePath(TCaseName testCaseName, URL url, Suffix suffix, FileType fileType) {
        TSuiteResult tSuiteResult = this.getCurrentTSuiteResult()
        assert tSuiteResult != null
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(testCaseName)
        if (tCaseResult == null) {
            tCaseResult = new TCaseResult(testCaseName).setParent(tSuiteResult)
            tSuiteResult.addTCaseResult(tCaseResult)
        }
        TargetURL targetURL = tCaseResult.getTargetURL(url)
        if (targetURL == null) {
            targetURL = new TargetURL(url).setParent(tCaseResult)
            tCaseResult.getTargetURLs().add(targetURL)
        }
        Material material = targetURL.getMaterial(suffix, fileType)
        if (material == null) {
            String fileName = Material.resolveMaterialFileName(url, suffix, fileType)
            Path materialPath = tCaseResult.getTCaseDir().resolve(fileName)
            material = new Material(materialPath, fileType).setParent(targetURL)

            // Here we create the parent directory for the material
            Helpers.ensureDirs(materialPath.getParent())
        }
        return material.getMaterialFilePath()
    }

    /**
     * returns a Path to save a screenshot of the URL in PNG format
     */
    @Override
    Path resolvePngFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), Suffix.NULL, FileType.PNG)
    }

    /**
     * return a Path to save a screenshot of the URL in PNG format appended with the suffix
     */
    @Override
    Path resolvePngFilePath(String testCaseId, String url, String suffix) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), new Suffix(suffix), FileType.PNG)
    }

    /**
     * returns a Path to save a JSON response from the URL
     */
    @Override
    Path resolveJsonFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), Suffix.NULL, FileType.JSON)
    }

    /**
     * return a Path to save a JSON response from the URL appended with the suffix
     */
    @Override
    Path resolveJsonFilePath(String testCaseId, String url, String suffix) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), new Suffix(suffix), FileType.JSON)
    }

    /**
     * returns a Path to save a XML response from the URL
     */
    @Override
    Path resolveXmlFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), Suffix.NULL, FileType.XML)
    }

    /**
     * return a Path to save a XML response from the URL appended with the suffix
     */
    @Override
    Path resolveXmlFilePath(String testCaseId, String url, String suffix) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), new Suffix(suffix), FileType.XML)
    }

    /**
     * returns a Path to save a XML response from the URL
     */
    @Override
    Path resolvePdfFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), Suffix.NULL, FileType.PDF)
    }

    /**
     * return a Path to save a XML response from the URL appended with the suffix
     */
    @Override
    Path resolvePdfFilePath(String testCaseId, String url, String suffix) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), new Suffix(suffix), FileType.PDF)
    }


    /**
     * create a Result.html file under the directory ${baseDir}/${Test Suite name}/${Test Suite timestamp}/
     * The Result.html file is an index to the Material files created by the TestResultsImpl at this time of execution
     *
     * @returns Path of the Results.html file
     */
    @Override
    Path report() throws IOException {
        if (currentTSuiteName != null && currentTSuiteTimestamp != null) {
            List<TSuiteResult> tsrList =
                this.tSuiteResults.stream()
                    .filter({tsr -> tsr.getTSuiteName() == currentTSuiteName && tsr.getTSuiteTimestamp() == currentTSuiteTimestamp })
                    .collect(Collectors.toList())
            if (tsrList.size() > 0) {
                TSuiteResult tsr = tsrList[0]
                Path html = tsr.getTSuiteTimestampDir().resolve("Result.html")
                Helpers.ensureDirs(tsr.getTSuiteTimestampDir())
                //
                createIndex(tsr, Files.newOutputStream(html))
                return html
            }
            return null
        }
        return null
    }


    // ----------------------------- helpers ----------------------------------

    TSuiteResult getCurrentTSuiteResult() {
        if (this.currentTSuiteName != null) {
            if (this.currentTSuiteTimestamp != null) {
                TSuiteResult tsr = getTSuiteResult(this.currentTSuiteName, this.currentTSuiteTimestamp)
                assert tsr != null
                return tsr
            } else {
                throw new IllegalStateException('currentTSuiteTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTSuiteName is not set')
        }
    }

    TCaseResult getTCaseResult(String testCaseId) {
        return this.getTCaseResult(new TCaseName(testCaseId))
    }

    TCaseResult getTCaseResult(TCaseName tCaseName) {
        if (tCaseName != null) {
            TSuiteResult tsr = this.getCurrentTSuiteResult()
            assert tsr != null
            return tsr.getTCaseResult(tCaseName)
        }
        else {
            throw new IllegalStateException("currentTcName is null")
        }
    }

    @Override
    void setTestCaseStatus(String testCaseId, String testCaseStatus) {
        this.getTCaseResult(testCaseId).setTestCaseStatus(testCaseStatus)
    }


    // ---------------------- overriding Object properties --------------------
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TestResultsImpl":{')
        sb.append('"baseDir":"' +
            Helpers.escapeAsJsonText(this.baseDir.toString()) + '",')
        sb.append('"currentTsName":"' +
            Helpers.escapeAsJsonText(this.currentTSuiteName.toString()) + '",')
        sb.append('"currentTsTimestamp":"' +
            Helpers.escapeAsJsonText(this.currentTSuiteTimestamp.toString()) + '",')
        sb.append('"tsResults":[')
        def counter = 0
        for (TSuiteResult tsr : this.tSuiteResults) {
            if (counter > 0) { sb.append(',') }
            sb.append(tsr.toJson())
            counter += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }


    // ---------------------------- lengthy private method --------------------
    /**
     * create and write a HTML to the Material files
     * @param os
     * @returns Path of the created Results.html file
     * @throws IOException
     */
    private static void createIndex(TSuiteResult tSuiteResult, OutputStream os) throws IOException {
        def writer = new OutputStreamWriter(os, 'UTF-8')
        def builder = new MarkupBuilder(writer)
        builder.doubleQuotes = true
        builder.html {
            head {
                meta('http-equiv':'X-UA-Compatible', content:'IE=edge')
                title("Katalon Studio Test Results ${tSuiteResult.getTSuiteName().toString()}/${tSuiteResult.getTSuiteTimestamp().toString()}")
                meta('charset':'utf-8')
                meta('name':'description', 'content':'')
                meta('name':'author', 'content':'')
                meta('name':'viewport', 'content':'width=device-width, initial-scale=1')
                link('rel':'stylesheet', 'href':'')
                mkp.comment('''[if lt IE 9]
<script src="//cdn.jsdelivr.net/html5shiv/3.7.2/html5shiv.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js"></script>
<![endif]''')
                link('rel':'shortcut icon', 'href':'')
                link('href':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css',
                    'rel':'stylesheet')

                /*
                style(type:'text/css') {
                    mkp.yieldUnescaped('''
                        <!--
                        body {
                            color      : #000000;
                            background : #E6DED9;
                            font-size  : 200%;
                        }
                        //-->
                    ''')
                }
                */
            }
            body() {
                mkp.comment('Place your content here')
                div('class':'container') {
                    h1('Katalon Studio Test Results')
                    h3("Test Suite : ${tSuiteResult.getTSuiteName().toString()}/${tSuiteResult.getTSuiteTimestamp().format()}")
                    // Slideshow
                    div('id':'carousel0', 'class':'carousel slide', 'data-ride':'carousel') {
                        ol('class':'carousel-indicators') {
                            // TODO このTSuiteResultのなかにScreenshotが百個もあったらどうしよう?
                            List<Material> mwList = tSuiteResult.getMaterials()
                            // TODO 画像じゃないPDFやJSONやXMLファイルを除外したい
                            def count = 0
                            for (Material mw: mwList) {
                                if (count == 0) {
                                    li('data-target':'#carousel0',
                                        'data-slide-to':"${count}",
                                        'class': 'active')
                                } else {
                                    li('data-target':'#carousel0',
                                        'data-slide-to':"${count}")
                                }
                                count += 1
                            }
                        }
                    }
                    div('class':'carousel-inner') {
                        List<Material> mwList = tSuiteResult.getMaterials()
                        def count = 0
                        for (Material mw: mwList) {
                            if (count == 0) {
                                div('class': 'item active') {

                                }
                            }
                            count += 1
                        }
                    }
                    //
                    List<TCaseResult> tcResults = tSuiteResult.getTCaseResults()
                    for (TCaseResult tcResult : tcResults) {
                        div('class':'row') {
                            div('class':'col-sm-12') {
                                h4("Test Case name : ${tcResult.getTCaseName().toString()}")
                                h4("Test Case status : ${tcResult.getTestCaseStatus()}")
                                List<TargetURL> targetURLs = tcResult.getTargetURLs()
                                for (TargetURL targetURL : targetURLs) {
                                    h5("URL : ${targetURL.getUrl().toExternalForm()}")
                                    List<Material> materials = targetURL.getMaterials()
                                    for (Material material : materials) {
                                        Path file = material.getMaterialFilePath()
                                        Path relative = tSuiteResult.getTSuiteTimestampDir().relativize(file).normalize()
                                        h6("src:${relative.toString()}")
                                        img(src:"${relative.toString().replace('\\','/').replace('%','%25')}",
                                            alt:"${targetURL.getUrl().toExternalForm()}",
                                            border:"0",
                                            width:"96%")
                                    }
                                }
                            }
                        }
                    }
                }
                mkp.comment('SCRIPTS')
                script('src':'https://code.jquery.com/jquery-1.12.4.min.js') {mkp.comment('')}
                script('src':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js') {mkp.comment('')}
                /*
                 mkp.comment('''
                     mkp is required to call helper methods such as yieldUnescaped, yield, comment
                 ''')
                 mkp.yield('testing MarkupBuilder'); br()
                 a(href:'http://d.hatena.ne.jp/fumokmm/', 'No Programming, No Life'); br()
                 a(href:'http://d.hatena.ne.jp/fumokmm/20090131/1233428513', 'MarkupBuilderでHTML生成を試してみた'); br()
                 mkp.yield('↑entry'); br()
                 */

            }
        }
        writer.close()
    }


}