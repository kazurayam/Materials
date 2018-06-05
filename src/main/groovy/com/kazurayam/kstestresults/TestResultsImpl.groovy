package com.kazurayam.kstestresults

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.stream.Collectors

final class TestResultsImpl implements TestResults {

    private Path baseDir
    private TsName currentTestSuiteName
    private TsTimestamp currentTestSuiteTimestamp
    private List<TsResult> testSuiteResults

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
     *         Path screenshotsDir = Paths.get(RunConfiguration.getProjectDir()).resolve('Screenshots')
     *         ScreenshotRepository scRepo =
     *             ScreenshotRepositoryFactory.createInstance(screenshotsDir, testSuiteContext.getTestSuiteId())
     *         GlobalVariable.SCREENSHOT_REPOSITORY = scRepo
     *     }
     * </PRE>
     *
     * @param dirPath directory under which a directory named as BASE_DIR_NAME will be created.
     */
    TestResultsImpl(Path baseDir, TsName testSuiteName) {
        this.baseDir = baseDir
        this.testSuiteResults = scan(this.baseDir)
        //
        this.currentTestSuiteName = testSuiteName
        this.currentTestSuiteTimestamp = new TsTimestamp()
        TsResult tsr = this.findOrNewTestSuiteResult(this.currentTestSuiteName, this.currentTestSuiteTimestamp)
        this.addTestSuiteResult(tsr)
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
        List<TsResult> testSuiteResults = new ArrayList<TsResult>()
        List<Path> testSuiteNamePaths =
                Files.list(baseDir)
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path testSuiteNamePath : testSuiteNamePaths) {
            TsName testSuiteName = new TsName(testSuiteNamePath.getFileName().toString())
            List<Path> timestampPaths =
                    Files.list(testSuiteNamePath)
                            .filter( { p -> Files.isDirectory(p) })
                            .collect(Collectors.toList())
            for (Path timestampPath : timestampPaths) {
                LocalDateTime ldt = TsTimestamp.parse(timestampPath.getFileName().toString())
                if (ldt != null) {
                    TsTimestamp testSuiteTimestamp = new TsTimestamp(ldt)
                    TsResult tsr = new TsResult(baseDir, testSuiteName, testSuiteTimestamp)
                    testSuiteResults.add(tsr)
                    //System.out.println("TestSuiteResult ${tsr}")
                    List<TcResult> testCaseResults = scanTestSuiteResult(tsr)
                    for (TcResult tcr : testCaseResults) {
                        tsr.addTestCaseResult(tcr)
                    }
                } else {
                    // ignore directories not in the format of yyyyMMdd_hhmmss
                }
            }
        }
        return testSuiteResults
    }

    private static List<TcResult> scanTestSuiteResult(TsResult tsr) {
        List<TcResult> testCaseResults = new ArrayList<TcResult>()
        List<Path> testCaseDirectories =
                Files.list(tsr.getTestSuiteTimestampDir())
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path testCaseDirectory : testCaseDirectories) {
            TcResult tcr =
                    new TcResult(tsr,
                            new TcName(testCaseDirectory.getFileName().toString()))
            testCaseResults.add(tcr)
            List<Path> imageFilePaths =
                    Files.list(testCaseDirectory)
                            .filter({ p -> Files.isRegularFile(p) })
                    //.filter({ p -> p.getFileName().endsWith(IMAGE_FILE_EXTENSION) })
                            .collect(Collectors.toList())
            for (Path imageFilePath : imageFilePaths) {
                List<String> fileNameElements =
                        TargetPage.parseScreenshotFileName(imageFilePath.getFileName().toString())
                if (0 < fileNameElements.size() && fileNameElements.size() <= 2) {
                    TargetPage targetPage = new TargetPage(tcr, new URL(fileNameElements[0]))
                    tcr.addTargetPage(targetPage)
                    //System.out.println("TargetPage ${targetPage}")
                    ScreenshotWrapper sw = new ScreenshotWrapper(targetPage, imageFilePath)
                    targetPage.addScreenshotWrapper(sw)
                    //System.out.println("ScreenshotWrapper ${sw}")
                    //System.out.println("loaded image file ${imageFilePath.toString()}")
                }
            }
        }
        return testCaseResults
    }


    // -------------------------- attribute getters & setters ------------------------
    Path getBaseDir() {
        return this.baseDir
    }

    TsName getCurrentTestSuiteName() {
        return this.currentTestSuiteName
    }

    TsTimestamp getCurrentTestSuiteTimestamp() {
        return this.currentTestSuiteTimestamp
    }


    // --------------------- create/add/get child nodes -----------------------
    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TsResult findOrNewTestSuiteResult(TsName testSuiteName, TsTimestamp timestamp) {
        TsResult tsr = this.getTestSuiteResult(testSuiteName, timestamp)
        if (tsr == null) {
            tsr = new TsResult(this.baseDir, testSuiteName, timestamp)
        }
        return tsr
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    void addTestSuiteResult(TsResult testSuiteResult) {
        boolean found = false
        for (TsResult tsr : this.testSuiteResults) {
            if (tsr == testSuiteResult) {
                found = true
            }
        }
        if (!found) {
            this.testSuiteResults.add(testSuiteResult)
        }
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TsResult getTestSuiteResult(TsName testSuiteName, TsTimestamp timestamp) {
        for (TsResult tsr : this.testSuiteResults) {
            if (tsr.getTestSuiteName() == testSuiteName && tsr.getTestSuiteTimestamp() == timestamp) {
                return tsr
            }
        }
        return null
    }

    // -------------------------- do the business -----------------------------
    @Override
    Path resolveScreenshotFilePath(String testCaseId, String url) {
        this.resolveScreenshotFilePath(new TcName(testCaseId), new URL(url), '')
    }

    @Override
    Path resolveScreenshotFilePath(String testCaseId, String url, String postFix) {
        this.resolveScreenshotFilePath(new TcName(testCaseId), new URL(url), postFix)
    }

    /**
     *
     * @param testCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveScreenshotFilePath(TcName testCaseName, URL url, String postFix) {
        TsResult currentTestSuiteResult = this.getCurrentTestSuiteResult()
        assert currentTestSuiteResult != null
        TcResult tcr = currentTestSuiteResult.findOrNewTestCaseResult(testCaseName)
        if (tcr != null) {
            ScreenshotWrapper sw = tcr.findOrNewTargetPage(url).findOrNewScreenshotWrapper(postFix)
            Path screenshotFilePath = sw.getScreenshotFilePath()
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

    TsResult getCurrentTestSuiteResult() {
        if (this.currentTestSuiteName != null) {
            if (this.currentTestSuiteTimestamp != null) {
                TsResult tsr = getTestSuiteResult(this.currentTestSuiteName, this.currentTestSuiteTimestamp)
                assert tsr != null
                return tsr
            } else {
                throw new IllegalStateException('currentTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTestSuiteName is not set')
        }
    }

    TcResult getTestCaseResult(String testCaseId) {
        return this.getTestCaseResult(new TcName(testCaseId))
    }

    TcResult getTestCaseResult(TcName testCaseName) {
        if (testCaseName != null) {
            TsResult tsr = this.getCurrentTestSuiteResult()
            assert tsr != null
            return tsr.getTestCaseResult(testCaseName)
        }
        else {
            throw new IllegalStateException("currentTestCaseName is null")
        }
    }

    @Override
    void setTestCaseStatus(String testCaseId, String testCaseStatus) {
        this.getTestCaseResult(testCaseId).setTestCaseStatus(testCaseStatus)
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
        sb.append('"currentTestSuiteName":"' + 
            Helpers.escapeAsJsonText(this.currentTestSuiteName.toString()) + '",')
        sb.append('"currentTestSuiteTimestamp":"' + 
            Helpers.escapeAsJsonText(this.currentTestSuiteTimestamp.toString()) + '",')
        sb.append('"testSuiteResults":[')
        def counter = 0
        for (TsResult tsr : this.testSuiteResults) {
            if (counter > 0) { sb.append(',') }
            sb.append(tsr.toJson())
            counter += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}