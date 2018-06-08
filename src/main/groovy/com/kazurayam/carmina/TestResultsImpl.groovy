package com.kazurayam.carmina

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
        this(baseDir, TsName.SUITELESS, TsTimestamp.TIMELESS)
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

    /**
     *
     * @param baseDir required
     * @param tsName required
     * @param tsTimestamp required
     */
    TestResultsImpl(Path baseDir, TsName tsName, TsTimestamp tsTimestamp) {
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        this.baseDir = baseDir
        this.tsResults = scanBaseDir(this.baseDir)
        //
        this.currentTsName = tsName
        this.currentTsTimestamp = tsTimestamp
        TsResult tsr = this.findOrNewTsResult(this.currentTsName, this.currentTsTimestamp)
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
    static List<TsResult> scanBaseDir(Path baseDir) {
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
                    new TcResult(new TcName(tcDir.getFileName().toString())).setParent(tsr)
            tcResults.add(tcr)
            List<Path> materialFilePaths =
                    Files.list(tcDir)
                            .filter({ p -> Files.isRegularFile(p) })
                    //.filter({ p -> p.getFileName().endsWith(IMAGE_FILE_EXTENSION) })
                            .collect(Collectors.toList())
            for (Path materialFilePath : materialFilePaths) {
                String urlPart = this.identifyURLpart(materialFilePath)
                String suffix = this.identifySuffix(materialFilePath)
                FileType ft = this.identifyFileType(materialFilePath)
                URL url = new URL(URLDecoder.decode(urlPart, 'UTF-8'))
                TargetURL targetURL = new TargetURL(url).setParent(tcr)
                tcr.addTargetURL(targetURL)
                MaterialWrapper mw = new MaterialWrapper(materialFilePath, ft).setParent(targetURL)
                targetURL.addMaterialWrapper(mw)
            }
        }
        return tcResults
    }

    /**
     * check the file name extension (.png, .pdf, etc) and identify the FileType
     * @param p
     * @return
     */
    static FileType identifyFileType(Path p) {
        String fileName = p.getFileName().toString().trim()
        if (fileName.lastIndexOf('.') < 0) {
            return FileType.OCTET
        } else {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1)
            def ft = FileType.getByExtension(ext)
            if (ft != null) {
                return ft
            } else {
                return FileType.OCTET
            }
        }
    }

    /**
     * if p is /temp/abd.de.fg then return 'de' which is enclosed by a pair of dot(.) characters
     *
     * @param p
     * @return
     */
    static String identifySuffix(Path p) {
        String fileName = p.getFileName().toString().trim()
        List<String> tokens = Arrays.asList(fileName.split('\\.'))
        Collections.reverse(tokens)
        if (tokens.size() >= 3) {
            //   /temp/a.b.c.png => ['png', 'c', 'b', 'a'] => 'c'
            //   /temp/a.1.png => ['png', '1', 'a'] =>'1'
            return tokens.get(1)
        } else {
            //   /temp/a.png => ['png', 'a'] => ''
            //   /temp/a => ['a']
            return ''
        }
    }

    static String identifyURLpart(Path p) {
        String fileName = p.getFileName().toString().trim()
        List<String> tokens = Arrays.asList(fileName.split('\\.'))
        Collections.reverse(tokens)
        if (tokens.size() >= 3) {
            //   /temp/a.b.c.png => ['png', 'c', 'b', 'a']
            //   /temp/a.1.png => ['png', '1', 'a']
            List<String> sublist = tokens.subList(2, tokens.size())
            Collections.reverse(sublist)
            String[] sarray = sublist.toArray()
            return String.join('.', sarray)
        } else if (tokens.size() == 2) {
            //   /temp/a.png => ['png', 'a']
            return tokens[1]
        } else if (tokens.size() == 1) {
            //   /temp/a => ['a']
            return tokens[0]
        } else {
            return ''
        }
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
    Path resolveMaterialFilePath(String testCaseId, String url, FileType ext) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), '', ext)
    }

    @Override
    Path resolveMaterialFilePath(String testCaseId, String url, String suffix, FileType ext) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), suffix, ext)
    }

    /**
     * returns a Path to save a screenshot of the URL in PNG format
     */
    @Override
    Path resolvePngFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), '', FileType.PNG)
    }

    /**
     * return a Path to save a screenshot of the URL appended with the suffix in PNG format
     */
    @Override
    Path resolvePngFilePath(String testCaseId, String url, String suffix) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), suffix, FileType.PNG)
    }

    /**
     *
     * @param testCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveMaterialFilePath(TcName testCaseName, URL url, String suffix, FileType ext) {
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
                    h3("Test Suite : ${tsResult.getTsName().toString()}/${tsResult.getTsTimestamp().format()}")
                    // Slideshow
                    div('id':'carousel0', 'class':'carousel slide', 'data-ride':'carousel') {
                        ol('class':'carousel-indicators') {
                            // TODO このTsResultのなかにScreenshotが百個もあったらどうしよう?
                            List<MaterialWrapper> mwList = tsResult.getMaterialWrappers()
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
                        List<MaterialWrapper> mwList = tsResult.getMaterialWrappers()
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
                    List<TcResult> tcResults = tsResult.getTcResults()
                    for (TcResult tcResult : tcResults) {
                        div('class':'row') {
                            div('class':'col-sm-12') {
                                h4("Test Case name : ${tcResult.getTcName().toString()}")
                                h4("Test Case status : ${tcResult.getTcStatus()}")
                                List<TargetURL> targetURLs = tcResult.getTargetURLs()
                                for (TargetURL targetURL : targetURLs) {
                                    h5("URL : ${targetURL.getUrl().toExternalForm()}")
                                    List<MaterialWrapper> materialWrappers = targetURL.getMaterialWrappers()
                                    for (MaterialWrapper materialWrapper : materialWrappers) {
                                        Path file = materialWrapper.getMaterialFilePath()
                                        Path relative = tsResult.getTsTimestampDir().relativize(file).normalize()
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