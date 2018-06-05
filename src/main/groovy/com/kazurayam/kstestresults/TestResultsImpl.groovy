package com.kazurayam.kstestresults

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.stream.Collectors

final class TestResultsImpl implements TestResults {

    private Path baseDir
    private TsName currentTsName
    private TsTimestamp currentTsTimestamp
    private List<TsResult> tsResults

    static final String IMAGE_FILE_EXTENSION = '.png'

    // ---------------------- constructors & initializer ----------------------

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
        this.baseDir = baseDir
        this.tsResults = scan(this.baseDir)
        //
        this.currentTsName = tsName
        this.currentTsTimestamp = new TsTimestamp()
        TsResult tsr = this.findOrNewTsResult(this.currentTsName, this.currentTsTimestamp)
        this.addTsResult(tsr)
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
    Path resolveMaterialFilePath(String testCaseId, String url) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), '')
    }

    @Override
    Path resolveMaterialFilePath(String testCaseId, String url, String postFix) {
        this.resolveMaterialFilePath(new TcName(testCaseId), new URL(url), postFix)
    }

    /**
     *
     * @param testCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveMaterialFilePath(TcName testCaseName, URL url, String postFix) {
        TsResult currentTestSuiteResult = this.getCurrentTsResult()
        assert currentTestSuiteResult != null
        TcResult tcr = currentTestSuiteResult.findOrNewTcResult(testCaseName)
        if (tcr != null) {
            MaterialWrapper sw = tcr.findOrNewTargetURL(url).findOrNewMaterialWrapper(postFix)
            Path screenshotFilePath = sw.getMaterialFilePath()
            Helpers.ensureDirs(screenshotFilePath.getParent())
            return screenshotFilePath
        } else {
            throw new IllegalArgumentException("testCase ${testCaseName} is not found")
        }
    }

    @Override
    void report() {
        //throw new UnsupportedOperationException("TODO")
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

}