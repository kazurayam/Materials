package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

final class ScreenshotRepository {

    private Path baseDir
    private List<TestSuiteResult> testSuiteResults

    private TSTimestamp currentTimestamp
    private TestSuiteName currentTestSuiteName
    private TestCaseName currentTestCaseName

    // ---------------------- constructors & initializer ----------------------

    /**
     *
     * @param basDir
     */
    ScreenshotRepository(Path baseDir) {
        this.init(baseDir)
    }

    /**
     *
     * @param basDir
     */
    ScreenshotRepository(String baseDirString) {
        Path baseDir = Paths.get(System.getProperty('user.dir')).resolve(baseDirString)
        this.init(baseDir)
    }

    /**
     * You are supposed to call this in the TestListener@BeforeTestSuite as follows:
     *
     * <PRE>
     * import java.nio.file.Path
     * import java.nio.file.Paths
     * import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
     * import katalonimport com.kms.katalon.core.configuration.RunConfiguration
     * import com.kazurayam.ksbackyard.screenshotsupport.ScreenshotRepository
     * ...
     * class TL {
     *     @BeforeTestSuite
     *     def beforeTestSuite(TestSuiteContext testSuiteContext) {
     *         ScreenshotRepository scRepos =
     *             new ScreenshotRepository('Screenshots', testSuiteContext.getTestSuiteId())
     *         GlobalVariable.SCREENSHOT_REPOSITORY = scRepos
     *         WebUI.comment(">>> got instance of ${scRepos.toString()}")
     *     }
     * </PRE>
     *
     * @param dirPath directory under which a directory named as BASE_DIR_NAME will be created.
     */
    ScreenshotRepository(String baseDirString, String testSuiteName) {
        this(baseDirString)
        this.currentTestSuiteName = testSuiteName
        this.currentTimestamp = new TSTimestamp()
        TestSuiteResult tsr = this.findOrNewTestSuiteResult(this.currentTestSuiteName, this.currentTimestamp)
    }

    ScreenshotRepository(Path baseDir, TestSuiteName testSuiteName) {
        this(baseDir)
        this.currentTestSuiteName = testSuiteName
        this.currentTimestamp = new TSTimestamp()
        TestSuiteResult tsr = this.findOrNewTestSuiteResult(this.currentTestSuiteName, this.currentTimestamp)
        this.addTestSuiteResult(tsr)
    }


    private void init(Path baseDir) {
        this.baseDir = baseDir
        this.testSuiteResults = loadTreeDebug(this.baseDir)
    }

    protected static List<TestSuiteResult> loadTreeDebug(Path baseDir) {
        return new ArrayList<TestSuiteResult>()
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
     * This loadTree method scans through the file system under the baseDir, construct a tree
     * and return it.
     *
     * @param baseDir
     * @returns the tree
     *
     */
    protected static List<TestSuiteResult> loadTree(Path baseDir) {
        if (baseDir == null) {
            throw new IllegalArgumentException('argument baseDir is null')
        }
        /*
        def tree = new HashMap<String, Map<TSTimestamp, TestSuiteResult>>()
        List<Path> testSuiteNamePaths = Files.list(baseDir)
                                 .filter({ p -> Files.isDirectory(p) })
                                 .collect(Collectors.toList())
        for (Path testSuiteNamePath : testSuiteNamePaths) {
            String testSuiteName = testSuiteNamePath.getFileName().toString()
            List<Path> tsTimestampsPaths = Files.list(testSuiteNamePath)
                                     .filter({ p -> Files.isDirectory(p) })
                                     .collect(Collectors.toList())
            for (Path tsTimestampPath : tsTimestampsPaths) {
                LocalDateTime ldt = TSTimestamp.parse(tsTimestampPath.getFileName().toString())
                if (ldt != null) {
                    TestSuiteResult tsr = new TestSuiteResult(baseDir, testSuiteName, new TSTimestamp(ldt))
                    tree.put(testSuiteName, tsr)
                    //
                    List<Path> testCaseNamePaths = Files.list(tsTimestampPath)
                                                    .filter({ p -> Files.isDirectory(p) })
                                                    .collect(Collectors.toList())
                    for (Path testCaseNamePath : testCaseNamePaths) {
                        String testCaseName = testCaseNamePath.getFileName().toString()
                        TestCaseResult tcr = tsr.findOrNewTestCaseResult(testCaseName)
                        //
                        List<Path> imageFilePaths = Files.list(testCaseNamePath)
                                                        .filter({ p -> Files.isRegularFile(p) })
                                                        .filter({ p -> p.getFileName().toString().endsWith('.png') })
                                                        .collect(Collectors.toList())
                        for (Path imageFilePath : imageFilePaths) {
                            List<String> values = TargetPage.parseScreenshotFileName(imageFilePath.toString())
                            if (values.size() > 0) {
                                String decodedUrl = values[0]
                                TargetPage targetPage = tcr.findOrNewTargetPage(decodedUrl)
                                //TODO
                            }
                        }
                    }
                } else {
                    System.err.println("${tsTimestampPath.getFileName()} is NOT in the format "
                        + "${TSTimestamp.DATE_TIME_PATTERN}, therefore ignored")
                }
            }
        }
        return tree
        */
    }

    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("${ScreenshotRepository.getName()}('${this.baseDir}')")
        return sb.toString()
    }



    // -------------------------- attribute getters & setters ------------------------
    Path getBaseDir() {
        return this.baseDir
    }

    void setCurrentTestSuiteName(TestSuiteName testSuiteName) {
       this.currentTestSuiteName = testSuiteName
    }

    TestSuiteName getCurrentTestSuiteName() {
        return this.currentTestSuiteName
    }

    void setCurrentTimestamp(TSTimestamp timestamp) {
        this.currentTimestamp = timestamp
    }

    TSTimestamp getCurrentTimestamp() {
        return this.currentTimestamp
    }

    void setCurrentTestCaseName(TestCaseName testCaseName) {
        this.currentTestCaseName = testCaseName
    }

    TestCaseName getCurrentTestCaseName() {
        return this.currentTestCaseName
    }


    // --------------------- create/add/get child nodes -----------------------
    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TestSuiteResult findOrNewTestSuiteResult(TestSuiteName testSuiteName, TSTimestamp timestamp) {
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
    TestSuiteResult getTestSuiteResult(TestSuiteName testSuiteName, TSTimestamp timestamp) {
        for (TestSuiteResult tsr : this.testSuiteResults) {
            if (tsr.getTestSuiteName() == testSuiteName && tsr.getTSTimestamp() == timestamp) {
                return tsr
            }
        }
        return null
    }


    // ----------------------------- helpers ----------------------------------
    void setCurrentTestCaseStatus(String testCaseStatus) {
        this.getCurrentTestCaseResult().setTestCaseStatus(testCaseStatus)
    }

    String getCurrentTestCaseStatus() {
        return this.getCurrentTestCaseResult().getTestCaseStatus()
    }

    TestSuiteResult getCurrentTestSuiteResult() {
        if (this.currentTestSuiteName != null) {
            if (this.currentTimestamp != null) {
                TestSuiteResult tsr = getTestSuiteResult(this.currentTestSuiteName, this.currentTimestamp)
                assert tsr != null
                return tsr
            } else {
                throw new IllegalStateException('currentTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTestSuiteName is not set')
        }
    }

    TestCaseResult getCurrentTestCaseResult() {
        if (currentTestCaseName != null) {
            TestSuiteResult tsr = this.getCurrentTestSuiteResult()
            assert tsr != null
            return tsr.getTestCaseResult(currentTestCaseName)
        }
        else {
            throw new IllegalStateException("currentTestCaseName is null")
        }
    }


}