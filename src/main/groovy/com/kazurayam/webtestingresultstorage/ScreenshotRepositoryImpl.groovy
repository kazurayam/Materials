package com.kazurayam.webtestingresultstorage

import groovy.json.JsonBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.stream.Collectors

final class ScreenshotRepositoryImpl implements ScreenshotRepository {

    private Path baseDir
    private TestSuiteName currentTestSuiteName
    private TestSuiteTimestamp currentTestSuiteTimestamp
    private List<TestSuiteResult> testSuiteResults

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
    ScreenshotRepositoryImpl(Path baseDir, TestSuiteName testSuiteName) {
        this.baseDir = baseDir
        this.testSuiteResults = scan(this.baseDir)
        //
        this.currentTestSuiteName = testSuiteName
        this.currentTestSuiteTimestamp = new TestSuiteTimestamp()
        TestSuiteResult tsr = this.findOrNewTestSuiteResult(this.currentTestSuiteName, this.currentTestSuiteTimestamp)
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
    static List<TestSuiteResult> scan(Path baseDir) {
        List<TestSuiteResult> testSuiteResults = new ArrayList<TestSuiteResult>()
        List<Path> testSuiteNamePaths =
                Files.list(baseDir)
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path testSuiteNamePath : testSuiteNamePaths) {
            TestSuiteName testSuiteName = new TestSuiteName(testSuiteNamePath.getFileName().toString())
            List<Path> timestampPaths =
                    Files.list(testSuiteNamePath)
                            .filter( { p -> Files.isDirectory(p) })
                            .collect(Collectors.toList())
            for (Path timestampPath : timestampPaths) {
                LocalDateTime ldt = TestSuiteTimestamp.parse(timestampPath.getFileName().toString())
                if (ldt != null) {
                    TestSuiteTimestamp testSuiteTimestamp = new TestSuiteTimestamp(ldt)
                    TestSuiteResult tsr = new TestSuiteResult(baseDir, testSuiteName, testSuiteTimestamp)
                    testSuiteResults.add(tsr)
                    //System.out.println("TestSuiteResult ${tsr}")
                    List<TestCaseResult> testCaseResults = scanTestSuiteResult(tsr)
                    for (TestCaseResult tcr : testCaseResults) {
                        tsr.addTestCaseResult(tcr)
                    }
                } else {
                    // ignore directories not in the format of yyyyMMdd_hhmmss
                }
            }
        }
        return testSuiteResults
    }

    private static List<TestCaseResult> scanTestSuiteResult(TestSuiteResult tsr) {
        List<TestCaseResult> testCaseResults = new ArrayList<TestCaseResult>()
        List<Path> testCaseDirectories =
                Files.list(tsr.getTestSuiteTimestampDir())
                        .filter({ p -> Files.isDirectory(p) })
                        .collect(Collectors.toList())
        for (Path testCaseDirectory : testCaseDirectories) {
            TestCaseResult tcr =
                    new TestCaseResult(tsr,
                            new TestCaseName(testCaseDirectory.getFileName().toString()))
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

    TestSuiteName getCurrentTestSuiteName() {
        return this.currentTestSuiteName
    }

    TestSuiteTimestamp getCurrentTestSuiteTimestamp() {
        return this.currentTestSuiteTimestamp
    }


    // --------------------- create/add/get child nodes -----------------------
    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TestSuiteResult findOrNewTestSuiteResult(TestSuiteName testSuiteName, TestSuiteTimestamp timestamp) {
        TestSuiteResult tsr = this.getTestSuiteResult(testSuiteName, timestamp)
        if (tsr == null) {
            tsr = new TestSuiteResult(this.baseDir, testSuiteName, timestamp)
        }
        return tsr
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    void addTestSuiteResult(TestSuiteResult testSuiteResult) {
        boolean found = false
        for (TestSuiteResult tsr : this.testSuiteResults) {
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
    TestSuiteResult getTestSuiteResult(TestSuiteName testSuiteName, TestSuiteTimestamp timestamp) {
        for (TestSuiteResult tsr : this.testSuiteResults) {
            if (tsr.getTestSuiteName() == testSuiteName && tsr.getTestSuiteTimestamp() == timestamp) {
                return tsr
            }
        }
        return null
    }

    // -------------------------- do the business -----------------------------
    @Override
    Path resolveScreenshotFilePath(String testCaseId, String url) {
        this.resolveScreenshotFilePath(new TestCaseName(testCaseId), new URL(url), '')
    }

    @Override
    Path resolveScreenshotFilePath(String testCaseId, String url, String postFix) {
        this.resolveScreenshotFilePath(new TestCaseName(testCaseId), new URL(url), postFix)
    }

    /**
     *
     * @param testCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveScreenshotFilePath(TestCaseName testCaseName, URL url, String postFix) {
        TestSuiteResult currentTestSuiteResult = this.getCurrentTestSuiteResult()
        assert currentTestSuiteResult != null
        TestCaseResult tcr = currentTestSuiteResult.findOrNewTestCaseResult(testCaseName)
        if (tcr != null) {
            ScreenshotWrapper sw = tcr.findOrNewTargetPage(url).getScreenshotWrapper(postFix)
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

    TestSuiteResult getCurrentTestSuiteResult() {
        if (this.currentTestSuiteName != null) {
            if (this.currentTestSuiteTimestamp != null) {
                TestSuiteResult tsr = getTestSuiteResult(this.currentTestSuiteName, this.currentTestSuiteTimestamp)
                assert tsr != null
                return tsr
            } else {
                throw new IllegalStateException('currentTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTestSuiteName is not set')
        }
    }

    TestCaseResult getTestCaseResult(String testCaseId) {
        return this.getTestCaseResult(new TestCaseName(testCaseId))
    }

    TestCaseResult getTestCaseResult(TestCaseName testCaseName) {
        if (testCaseName != null) {
            TestSuiteResult tsr = this.getCurrentTestSuiteResult()
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
        def json = new JsonBuilder()
        json (
                ["baseDir":this.baseDir.toString()],
                ["currentTestSuiteName":this.currentTestSuiteName.toString()],
                ["currentTestSuiteTimestamp":this.currentTestSuiteTimestamp.format()],
                //["testSuiteResults": this.testSuiteResults]
        )
        return json.toString()
    }

}