package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ScreenshotRepository {

    // Singleton pattern is applied
    private static ScreenshotRepository instance

    private static Logger log = LoggerFactory.getLogger(ScreenshotRepository.class)

    private Path baseDirPath
    private Map<String, Map<Timestamp, TestSuiteResult>> testSuiteResults

    private String currentTestSuiteId
    private Timestamp currentTimestamp
    private String currentTestCaseId

    final static String BASE_DIR_NAME = 'Screenshots'

    static ScreenshotRepository getInstance() {
        return getInstance(Paths.get(System.getProperty('user.dir')))
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
     *         Path projectDir = Paths.get(RunConfiguration.getProjectDir())
     *         String testSuiteId = testSuiteContext.getTestSuiteId()
     *         ScreenshotRepository scRepos = ScreenshotRepository.getInstance(projectDir, testSuiteId)
     *         WebUI.comment(">>> got instance of ${scRepos.toString()}")
     *         ...
     * </PRE>
     *
     * @param dirPath You should pass the Katalon project directory here.
     * @return singleton instance
     */
    static ScreenshotRepository getInstance(Path dirPath) {
        if (instance == null) {
            Path p = dirPath.resolve(BASE_DIR_NAME)
            instance = new ScreenshotRepository(p)
        }
        log.info("returning ${instance.toString()}")
        return instance
    }

    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("${ScreenshotRepository.getName()}('${this.baseDirPath}')")
        return sb.toString()
    }

    /**
     * Signleton pattern is applied. The constructor is hidden intentionally.
     *
     * @param baseDirPath
     */
    private ScreenshotRepository(Path baseDirPath) {
        this.baseDirPath = baseDirPath
        this.testSuiteResults = new HashMap<String, Map<Timestamp, TestSuiteResult>>()
    }

    Path getBaseDirPath() {
        return this.baseDirPath
    }

    void setCurrentTestSuiteId(String testSuiteId) {
        this.currentTestSuiteId = testSuiteId
    }

    String getCurrentTestSuiteId() {
        return this.currentTestSuiteId
    }

    void setCurrentTimestamp(Timestamp timestamp) {
        this.currentTimestamp = timestamp
    }

    Timestamp getCurrentTimestamp() {
        return this.currentTimestamp
    }

    void setCurrentTestCaseId(String testCaseId) {
        this.currentTestCaseId = testCaseId
    }

    String getCurrentTestCaseId() {
        return this.currentTestCaseId
    }

    TestSuiteResult getCurrentTestSuiteResult() {
        if (this.currentTestSuiteId != null) {
            if (this.currentTimestamp != null) {
                return findOrNewTestSuiteResult(this.currentTestSuiteId, this.currentTimestamp)
            } else {
                throw new IllegalStateException('currentTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTestSuiteId is not set')
        }
    }

    TestSuiteResult findOrNewTestSuiteResult(String testSuiteId, Timestamp timestamp) {
        TestSuiteResult tsr
        if (this.testSuiteResults.containsKey(testSuiteId)) {
            Map<Timestamp, TestSuiteResult> series = this.testSuiteResults.get(testSuiteId)
            if (series.containsKey(timestamp)) {
                tsr = series.get(timestamp)
            } else {
                tsr = new TestSuiteResult(this, testSuiteId, timestamp)
                series.put(timestamp, tsr)
            }
        } else {
            tsr = new TestSuiteResult(this, testSuiteId, timestamp)
            Map<Timestamp, TestSuiteResult> series = new HashMap<Timestamp, TestSuiteResult>()
            series.put(timestamp, tsr)
            this.testSuiteResults.put(testSuiteId, series)
        }
        return tsr
    }

    TestCaseResult getCurrentTestCaseResult() {
        if (currentTestCaseId) {
            return this.getCurrentTestSuiteResult().findOrNewTestCaseResult(currentTestCaseId)
        }
        else {
            throw new IllegalStateException("currentTestCaseId is null")
        }
    }
}