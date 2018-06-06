package com.kazurayam.kstestresults

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.stream.Collectors

import groovy.xml.MarkupBuilder

final class TestResultsImpl implements TestResults {

    private Path baseDir
    private TsName currentTsName
    private TsTimestamp currentTsTimestamp
    private List<TsResult> tsResults

    static final String IMAGE_FILE_EXTENSION = '.png'

    // ---------------------- constructors & initializer ----------------------

    TestResultsImpl(Path baseDir) {
        this(baseDir, null, null)
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
    TestResultsImpl(Path baseDir, TsName tsName) {
        this(baseDir, tsName, new TsTimestamp())
    }

    TestResultsImpl(Path baseDir, TsName tsName, TsTimestamp tsTimestamp) {
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        this.baseDir = baseDir
        this.tsResults = scan(this.baseDir)
        //
        if (tsName != null && tsTimestamp != null) {
            this.currentTsName = tsName
            this.currentTsTimestamp = tsTimestamp
            TsResult tsr = this.findOrNewTsResult(this.currentTsName, this.currentTsTimestamp)
            this.addTsResult(tsr)
        }
    }

    /**
     * Here I assume that I have a file system tree like:
     *
     * ./Screenshots/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png
     *
     * The format is as follows:
     *
     * <baseDir>/<Test Suite Name>/<Timestamp>/<Test Case Name>/<image file>
     *
     * This load method scans through the file system under the baseDir, construct a tree
     * and return it.
     *
     * @param baseDir
     * @returns the tree
     *
     */
    static List<TsResult> scan(Path baseDir) {
        List<TsResult> tsResults_work = new ArrayList<TsResult>()
        List<Path> tsNamePaths =
                Files.list(baseDir)
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path tsNamePath : tsNamePaths) {
            TsName tsName = new TsName(tsNamePath.getFileName().toString())
            List<Path> tsTimestampPaths =
                    Files.list(tsNamePath)
                            .filter( { p -> Files.isDirectory(p) })
                            .collect(Collectors.toList())
            for (Path tsTimestampPath : tsTimestampPaths) {
                LocalDateTime ldt = TsTimestamp.parse(tsTimestampPath.getFileName().toString())
                if (ldt != null) {
                    TsTimestamp tsTimestamp = new TsTimestamp(ldt)
                    TsResult tsr = new TsResult(baseDir, tsName, tsTimestamp)
                    tsResults_work.add(tsr)
                    //System.out.println("tsr=${tsr}")
                    List<TcResult> tcResults = scanTsResult(tsr)
                    for (TcResult tcr : tcResults) {
                        tsr.addTcResult(tcr)
                    }
                } else {
                    // ignore directories not in the format of yyyyMMdd_hhmmss
                }
            }
        }
        return tsResults_work
    }

    private static List<TcResult> scanTsResult(TsResult tsr) {
        List<TcResult> tcResults = new ArrayList<TcResult>()
        List<Path> tcDirs =
                Files.list(tsr.getTsTimestampDir())
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path tcDir : tcDirs) {
            TcResult tcr =
                    new TcResult(tsr,
                            new TcName(tcDir.getFileName().toString()))
            tcResults.add(tcr)
            List<Path> materialFilePaths =
                    Files.list(tcDir)
                            .filter({ p -> Files.isRegularFile(p) })
                    //.filter({ p -> p.getFileName().endsWith(IMAGE_FILE_EXTENSION) })
                            .collect(Collectors.toList())
            for (Path materialFilePath : materialFilePaths) {
                List<String> elements =
                        TargetURL.parseMaterialFileName(materialFilePath.getFileName().toString())
                if (0 < elements.size() && elements.size() <= 2) {
                    TargetURL targetURL = new TargetURL(tcr, new URL(elements[0]))
                    tcr.addTargetURL(targetURL)
                    MaterialWrapper sw = new MaterialWrapper(targetURL, materialFilePath)
                    targetURL.addMaterialWrapper(sw)
                }
            }
        }
        return tcResults
    }


    // -------------------------- attribute getters & setters ------------------------
    Path getBaseDir() {
        return this.baseDir
    }

    TsName getCurrentTestSuiteName() {
        return this.currentTsName
    }

    TsTimestamp getCurrentTestSuiteTimestamp() {
        return this.currentTsTimestamp
    }


    // --------------------- create/add/get child nodes -----------------------
    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TsResult findOrNewTsResult(TsName tsName, TsTimestamp tsTimestamp) {
        TsResult tsr = this.getTsResult(tsName, tsTimestamp)
        if (tsr == null) {
            tsr = new TsResult(this.baseDir, tsName, tsTimestamp)
        }
        return tsr
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    void addTsResult(TsResult tsResult) {
        boolean found = false
        for (TsResult tsr : this.tsResults) {
            if (tsr == tsResult) {
                found = true
            }
        }
        if (!found) {
            this.tsResults.add(tsResult)
        }
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TsResult getTsResult(TsName tsName, TsTimestamp tsTimestamp) {
        for (TsResult tsr : this.tsResults) {
            if (tsr.getTsName() == tsName && tsr.getTsTimestamp() == tsTimestamp) {
                return tsr
            }
        }
        return null
    }

    // -------------------------- do the business -----------------------------
    @Override
    Path resolveMaterialFilePath(String testCaseId, String url, FileExtension ext) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), '', ext)
    }

    @Override
    Path resolveMaterialFilePath(String testCaseId, String url, String suffix, FileExtension ext) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), suffix, ext)
    }

    /**
     * returns a Path to save a screenshot of the URL in PNG format
     */
    @Override
    Path resolveScreenshotFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), FileExtension.PNG)
    }

    /**
     * return a Path to save a screenshot of the URL appended with the suffix in PNG format
     */
    @Override
    Path resolveScreenshotFilePath(String testCaseId, String url, String suffix) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), suffix, FileExtension.PNG)
    }

    /**
     *
     * @param testCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveMaterialFilePath(TcName testCaseName, URL url, String suffix, FileExtension ext) {
        TsResult currentTestSuiteResult = this.getCurrentTsResult()
        assert currentTestSuiteResult != null
        TcResult tcr = currentTestSuiteResult.findOrNewTcResult(testCaseName)
        if (tcr != null) {
            MaterialWrapper sw = tcr.findOrNewTargetURL(url).findOrNewMaterialWrapper(suffix, ext)
            Path screenshotFilePath = sw.getMaterialFilePath()
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
        if (currentTsName != null && currentTsTimestamp != null) {
            List<TsResult> tsrList =
                this.tsResults.stream()
                    .filter({tsr -> tsr.getTsName() == currentTsName && tsr.getTsTimestamp() == currentTsTimestamp })
                    .collect(Collectors.toList())
            if (tsrList.size() > 0) {
                TsResult tsr = tsrList[0]
                Path html = tsr.getTsTimestampDir().resolve("Result.html")
                Helpers.ensureDirs(tsr.getTsTimestampDir())
                //
                createIndex(tsr, Files.newOutputStream(html))
                return html
            }
        }
    }


    // ----------------------------- helpers ----------------------------------

    TsResult getCurrentTsResult() {
        if (this.currentTsName != null) {
            if (this.currentTsTimestamp != null) {
                TsResult tsr = getTsResult(this.currentTsName, this.currentTsTimestamp)
                assert tsr != null
                return tsr
            } else {
                throw new IllegalStateException('currentTsTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTsName is not set')
        }
    }

    TcResult getTcResult(String testCaseId) {
        return this.getTcResult(new TcName(testCaseId))
    }

    TcResult getTcResult(TcName tcName) {
        if (tcName != null) {
            TsResult tsr = this.getCurrentTsResult()
            assert tsr != null
            return tsr.getTcResult(tcName)
        }
        else {
            throw new IllegalStateException("currentTcName is null")
        }
    }

    @Override
    void setTcStatus(String testCaseId, String testCaseStatus) {
        this.getTcResult(testCaseId).setTcStatus(testCaseStatus)
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
            Helpers.escapeAsJsonText(this.currentTsName.toString()) + '",')
        sb.append('"currentTsTimestamp":"' +
            Helpers.escapeAsJsonText(this.currentTsTimestamp.toString()) + '",')
        sb.append('"tsResults":[')
        def counter = 0
        for (TsResult tsr : this.tsResults) {
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
    private void createIndex(TsResult tsResult, OutputStream os) throws IOException {
        def writer = new OutputStreamWriter(os, 'UTF-8')
        def builder = new MarkupBuilder(writer)
        builder.doubleQuotes = true
        builder.html {
            head {
                meta('http-equiv':'X-UA-Compatible', content:'IE=edge')
                title("Katalon Studio Test Results ${tsResult.getTsName().toString()}/${tsResult.getTsTimestamp().toString()}")
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

                    List<TcResult> tcResults = tsResult.getTcResults()
                    for (TcResult tcResult : tcResults) {
                        div('class':'row') {
                            div('class':'col-sm-12') {
                            List<TargetURL> targetURLs = tcResult.getTargetURLs()
                                for (TargetURL targetURL : targetURLs) {
                                    List<MaterialWrapper> materialWrappers = targetURL.getMaterialWrappers()
                                    for (MaterialWrapper materialWrapper : materialWrappers) {
                                        Path file = materialWrapper.getMaterialFilePath()
                                        Path relative = tsResult.getTsTimestampDir().relativize(file).normalize()
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