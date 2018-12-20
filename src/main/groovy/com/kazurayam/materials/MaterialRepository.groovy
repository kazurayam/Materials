package com.kazurayam.materials

import java.nio.file.Path

import com.kazurayam.materials.model.TSuiteTimestamp

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
    void putCurrentTestSuite(TSuiteName tSuiteName)
    void putCurrentTestSuite(String testSuiteId, String testSuiteTimestamp)
    void putCurrentTestSuite(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp)

    String getCurrentTestSuiteId()
    String getCurrentTestSuiteTimestamp()

    Path getBaseDir()
    Path getCurrentTestSuiteDirectory()

    Path getTestCaseDirectory(String testCaseId)

    /**
     *
     * @param testCaseId e.g., 'Test Cases/TC1'
     * @param url e.g., 'http://demoaut.katalon.com/'
     * @return
     */
    Path resolveScreenshotPath(String testCaseId, URL url)
    Path resolveScreenshotPath(TCaseName tCaseName, URL url)

    Path resolveScreenshotPath(String testCaseId, Path subpath, URL url)
    Path resolveScreenshotPath(TCaseName tCaseName, Path subpath, URL url)

    /**
     *
     * @param testCaseId
     * @param fileName
     * @return
     */
    Path resolveMaterialPath(String testCaseId, String fileName)
    Path resolveMaterialPath(TCaseName testCaseName, String fileName)

    /**
     *
     * @param testCaseId e.g., 'Test Cases/TC1'
     * @param subpath '.', 'foo' or 'foo/bar'
     * @param fileName 'myfile.xls'
     * @return
     */
    Path resolveMaterialPath(String testCaseId, Path subpath, String fileName)
    Path resolveMaterialPath(TCaseName testCaseName, Path subpath, String fileName)

    /**
     * Scan the <pre>[project dir]/Materials</pre> directory to create <pre>[project dir]/Materials/index.html</pre> file.
     * @return
     */
    Path makeIndex()


    /**
     * Scans the Materials directory to look up pairs of Material objects to compare.
     * 
     * This method perform the following search under the Materials directory
     * in order to identify which Material object to be included.
     * 
     * 1. selects all ./Materials/<tSuiteName>/yyyyMMdd_hhmmss directories with specified tSuiteName 
     * 2. among them, select the directory with the 1st latest timestamp. This one is regarded as "Actual one".
     * 3. among them, select the directory with the 2nd latest timestamp. This one is regarded as "Expected one".
     * 4. Scan the 2 directories chosen. Create a List of Material objects. 2 files which have the same path 
     *    under the yyyyMMdd_hhmmss directory will be packaged as a pair to form a MaterialPair object.
     * 
     * @return List<MaterialPair>
     */
    List<MaterialPair> createMaterialPairs(TSuiteName tSuiteName)
    
    /**
     *
     * @param directory delete descendant directories and files of the specified directory. The directory is retained.
     */
    void deleteBaseDirContents() throws IOException
}
