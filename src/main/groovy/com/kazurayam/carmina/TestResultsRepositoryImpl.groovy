package com.kazurayam.carmina

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.stream.Collectors

import groovy.xml.MarkupBuilder

final class TestResultsRepositoryImpl implements TestResultsRepository {

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
        this.tSuiteResults = scanBaseDir(this.baseDir)
        //
        this.currentTSuiteName = tsName
        this.currentTSuiteTimestamp = tsTimestamp
        TSuiteResult tsr = this.findOrNewTsResult(this.currentTSuiteName, this.currentTSuiteTimestamp)
        this.addTsResult(tsr)
    }

    /**
     * ./Results/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png
     * ./Results/TS1/20180530_130604/TC1/1/http%3A%2F%2Fdemoaut.katalon.com%2F.png
     * ./Results/TS1/20180530_130604/TC1/2/http%3A%2F%2Fdemoaut.katalon.com%2F.png
     *
     * <baseDir>/<Test Suite Name>/<Timestamp>/<Test Case Name>/<subpath>
     *
     * @param baseDir
     * @returns the tree
     *
     */
    static List<TSuiteResult> scanBaseDir(Path baseDir) {
        List<TSuiteResult> tsResults_work = new ArrayList<TSuiteResult>()
        List<Path> tsNamePaths =
                Files.list(baseDir)
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path tsNamePath : tsNamePaths) {
            TSuiteName tsName = new TSuiteName(tsNamePath.getFileName().toString())
            List<Path> tsTimestampPaths =
                    Files.list(tsNamePath)
                            .filter( { p -> Files.isDirectory(p) })
                            .collect(Collectors.toList())
            for (Path tsTimestampPath : tsTimestampPaths) {
                LocalDateTime ldt = TSuiteTimestamp.parse(tsTimestampPath.getFileName().toString())
                if (ldt != null) {
                    TSuiteTimestamp tsTimestamp = new TSuiteTimestamp(ldt)
                    TSuiteResult tsr = new TSuiteResult(tsName, tsTimestamp).setParent(baseDir)
                    tsResults_work.add(tsr)
                    //System.out.println("tsr=${tsr}")
                    List<TCaseResult> tcResults = scanTsResult(tsr)
                    for (TCaseResult tcr : tcResults) {
                        tsr.addTcResult(tcr)
                    }
                } else {
                    // ignore directories not in the format of yyyyMMdd_hhmmss
                }
            }
        }
        return tsResults_work
    }

    private static List<TCaseResult> scanTsResult(TSuiteResult tsr) {
        List<TCaseResult> tcResults = new ArrayList<TCaseResult>()
        List<Path> tcDirs =
                Files.list(tsr.getTsTimestampDir())
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path tcDir : tcDirs) {
            TCaseResult tcr =
                    new TCaseResult(new TCaseName(tcDir.getFileName().toString())).setParent(tsr)
            tcResults.add(tcr)
            List<Path> materialFilePaths =
                    Files.list(tcDir)
                            .filter({ p -> Files.isRegularFile(p) })
                    //.filter({ p -> p.getFileName().endsWith(IMAGE_FILE_EXTENSION) })
                            .collect(Collectors.toList())
            for (Path materialFilePath : materialFilePaths) {
                String materialFileName = materialFilePath.getFileName()
                FileType ft = MaterialWrapper.parseFileNameForFileType(materialFileName)
                String suffix = MaterialWrapper.parseFileNameForSuffix(materialFileName)
                URL url = MaterialWrapper.parseFileNameForURL(materialFileName)
                TargetURL targetURL = new TargetURL(url).setParent(tcr)
                tcr.addTargetURL(targetURL)
                MaterialWrapper mw = new MaterialWrapper(materialFilePath, ft).setParent(targetURL)
                targetURL.addMaterialWrapper(mw)
            }
        }
        return tcResults
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
    TSuiteResult findOrNewTsResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        TSuiteResult tsr = this.getTsResult(tSuiteName, tSuiteTimestamp)
        if (tsr == null) {
            tsr = new TSuiteResult(tSuiteName, tSuiteTimestamp).setParent(baseDir)
        }
        return tsr
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    void addTsResult(TSuiteResult tSuiteResult) {
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
    TSuiteResult getTsResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        for (TSuiteResult tsr : this.tSuiteResults) {
            if (tsr.getTSuiteName() == tSuiteName && tsr.getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    // -------------------------- do the business -----------------------------
    @Override
    Path resolveMaterialFilePath(String testCaseId, String url, FileType fileType) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), '', fileType)
    }

    @Override
    Path resolveMaterialFilePath(String testCaseId, String url, String suffix, FileType fileType) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), suffix, fileType)
    }

    /**
     * returns a Path to save a screenshot of the URL in PNG format
     */
    @Override
    Path resolvePngFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), '', FileType.PNG)
    }

    /**
     * return a Path to save a screenshot of the URL appended with the suffix in PNG format
     */
    @Override
    Path resolvePngFilePath(String testCaseId, String url, String suffix) {
        this.resolveMaterialFilePath(new TCaseName(testCaseId), new URL(url), suffix, FileType.PNG)
    }

    /**
     *
     * @param testCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveMaterialFilePath(TCaseName testCaseName, URL url, String suffix, FileType fileType) {
        TSuiteResult currentTestSuiteResult = this.getCurrentTSuiteResult()
        assert currentTestSuiteResult != null
        TCaseResult tcr = currentTestSuiteResult.findOrNewTCaseResult(testCaseName)
        if (tcr != null) {
            MaterialWrapper mw = tcr.findOrNewTargetURL(url).findOrNewMaterialWrapper(suffix, fileType)
            Path screenshotFilePath = mw.getMaterialFilePath()
            Helpers.ensureDirs(screenshotFilePath.getParent())
            return screenshotFilePath
        } else {
            throw new IllegalArgumentException("testCase ${testCaseName} is not found")
        }
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
                Path html = tsr.getTsTimestampDir().resolve("Result.html")
                Helpers.ensureDirs(tsr.getTsTimestampDir())
                //
                createIndex(tsr, Files.newOutputStream(html))
                return html
            }
        }
    }


    // ----------------------------- helpers ----------------------------------

    TSuiteResult getCurrentTSuiteResult() {
        if (this.currentTSuiteName != null) {
            if (this.currentTSuiteTimestamp != null) {
                TSuiteResult tsr = getTsResult(this.currentTSuiteName, this.currentTSuiteTimestamp)
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
    private void createIndex(TSuiteResult tSuiteResult, OutputStream os) throws IOException {
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
                            List<MaterialWrapper> mwList = tSuiteResult.getMaterialWrappers()
                            // TODO 画像じゃないPDFやJSONやXMLファイルを除外したい
                            def count = 0
                            for (MaterialWrapper mw: mwList) {
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
                        List<MaterialWrapper> mwList = tSuiteResult.getMaterialWrappers()
                        def count = 0
                        for (MaterialWrapper mw: mwList) {
                            if (count == 0) {
                                div('class': 'item active') {

                                }
                            } else {

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
                                    List<MaterialWrapper> materialWrappers = targetURL.getMaterialWrappers()
                                    for (MaterialWrapper materialWrapper : materialWrappers) {
                                        Path file = materialWrapper.getMaterialFilePath()
                                        Path relative = tSuiteResult.getTsTimestampDir().relativize(file).normalize()
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