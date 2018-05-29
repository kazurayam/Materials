package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ScreenshotRepository implements IScreenshotRepository {

    final static String BASE_DIR_NAME = 'Screenshots'

    private static ScreenshotRepository instance

    private static Logger log = LoggerFactory.getLogger(ScreenshotRepository.class)

    private Path baseDirPath
    private String testSuiteId
    private LocalDateTime timestamp
    private TestSuiteResult testSuiteResult
    private String currentTestCaseId

    static ScreenshotRepository getInstance() {
        return getInstance('executed_indivisually')
    }

    static ScreenshotRepository getInstance(String testSuiteId) {
        return getInstance(Paths.get(System.getProperty('user.dir')), testSuiteId)
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
     * @param projectDir You should pass the project directory here.
     * @param testSuiteId
     * @return
     */
    static ScreenshotRepository getInstance(Path katalonProjectDirPath, String testSuiteId) {
        if (instance == null) {
            Path p = katalonProjectDirPath.resolve(BASE_DIR_NAME)
            instance = new ScreenshotRepository(p, testSuiteId)
        }
        log.info("returning ${instance.toString()}")
        return instance
    }

    String toString() {
        return "${ScreenshotRepository.getName()}('${this.baseDirPath}','${this.testSuiteId}')"
    }

    private ScreenshotRepository(Path baseDirPath, String testSuiteId) {
        this.baseDirPath = baseDirPath
        this.testSuiteId = testSuiteId
        this.timestamp = LocalDateTime.now()
        this.testSuiteResult = new TestSuiteResult(this, testSuiteId, timestamp)
        this.currentTestCaseId = null
    }

    @Override
    Path getBaseDirPath() {
        return this.baseDirPath
    }

    @Override
    TestSuiteResult getTestSuiteResult() {
        return this.testSuiteResult
    }

    @Override
    void setCurrentTestCaseId(String testCaseId) {
        this.currentTestCaseId = testCaseId
    }

    protected String getCurrentTestCaseId() {
        return this.currentTestCaseId
    }

    protected TestCaseResult getCurrentTestCaseResult() {
        if (currentTestCaseId) {
            return this.testSuiteResult.findOrNewTestCaseResult(currentTestCaseId)
        }
        else {
            throw new IllegalStateException("currentTestCaseId is null")
        }
    }

    @Override
    Path resolveScreenshotFilePath(String pageUrl) {
        TestCaseResult tcResult = this.getCurrentTestCaseResult()
        if (tcResult == null) {
            throw new IllegalStateException("currentTestCaseId is not set")
        }
        else {
            return tcResult.findOrNewTargetPage(pageUrl).createScreenshotWrapper().getScreenshotFilePath()
        }
    }

    @Override
    TestCaseResult findOrNewTestCaseResult(String testCaseId) {
        return this.testSuiteResult.findOrNewTestCaseResult(testCaseId)
    }

    @Override
    void buildReport() throws IOException {
        throw new UnsupportedOperationException("TODO")
    }

    @Override
    void buildArchive(OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("TODO")
    }

}