package com.kazurayam.carmina.material

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

    Path resolveMaterial(String testCaseId, String url, FileType fileType)
    Path resolveMaterial(String testCaseId, String url, String suffix, FileType fileType)

    Path makeIndex()
}
