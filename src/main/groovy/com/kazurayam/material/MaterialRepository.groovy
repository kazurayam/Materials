package com.kazurayam.material

import java.nio.file.Path

/**
 * MaterialRepository#resolveMaterial() method resolves Path to save your 'Material'
 * obtained during a run of WebDriver-based testing.
 *
 * 'Material' includes for example
 * - PNG files as screenshots of a Web page
 * - PDF files downloaded from a Web page
 * - JSON files responded by Rest API
 * - XML files responded by SOAP server
 *
 * The file name of a Material is defined as:
 * <pre>
 * &lt;encoded URL&gt;§&lt;suffix&gt;.&lt;file extension&gt;
 * </pre>
 *
 * for example
 * <pre>
 * http%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png
 * </pre>
 *
 * @author kazurayam
 *
 */
interface MaterialRepository {

    void putCurrentTestSuite(String testSuiteId)
    void putCurrentTestSuite(String testSuiteId, String testSuiteTimestamp)

    Path getBaseDir()
    Path getCurrentTestSuiteDirectory()
    Path getTestCaseDirectory(String testCaseId)

    /**
     *
     * @param testCaseId e.g., 'Test Cases/TC1'
     * @param url e.g., 'http://demoaut.katalon.com/'
     * @return
     */
    Path resolveScreenshotMaterialPath(String testCaseId, String url)

    /**
     *
     * @param testCaseId e.g., 'Test Cases/TC1'
     * @param fileName 'mymemo.txt'
     * @return
     */
    Path resolveMaterialPath(String testCaseId, String fileName)

    /**
     *
     * @param fileName e.g., 'smilechart.xls'
     * @return
     */
    int deleteFilesInDownloadsDir(String fileName)

    /**
     *
     * @param testCaseId e.g., 'Test Cases/TC1'
     * @param fileName e.g., 'smilechart.xls'
     * @return
     */
    Path importFileFromDownloadsDir(String testCaseId, String fileName)


    /**
     * Scan the <pre>[project dir]/Materials</pre> directory to create <pre>[project dir]/Materials/index.html</pre> file.
     * @return
     */
    Path makeIndex()
}
