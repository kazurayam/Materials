package com.kazurayam.material

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.nio.file.Path
/**
 *
 * @author kazurayam
 *
 */
class JUnitReportWrapper {

    static Logger logger_ = LoggerFactory.getLogger(JUnitReportWrapper.class)

    static final DocumentBuilderFactory dbFactory_
    static {
        dbFactory_ = DocumentBuilderFactory.newInstance()
        dbFactory_.setNamespaceAware(true)
    }
    private Document document_
    private XPath xpath_

    JUnitReportWrapper(Path path) {
        this(path.toFile())
    }

    JUnitReportWrapper(File file) {
        DocumentBuilder db = dbFactory_.newDocumentBuilder()
        Document document = db.parse(file)
        init(document)
    }

    JUnitReportWrapper(Document document) {
        init(document)
    }

    private void init(Document document) {
        document_ = document
        xpath_ = XPathFactory.newInstance().newXPath()
    }
    /**
     *
     * @param testSuiteId e.g., 'Test Suites/main/TS1'
     * @return String e.g., 'PASSED: 1, FAILED: 1, ERROR: 0', returns null if the Test Suite not found
     */
    String getTestSuiteSummary(String testSuiteId) {
        String location = "/testsuites/testsuite[@id='${testSuiteId}']"
        Node testSuiteNode = (Node)xpath_.evaluate(location, document_, XPathConstants.NODE)
        if (testSuiteNode != null) {
            Integer tests    = xpath_.evaluate("/testsuites/testsuite[@id='${testSuiteId}']/@tests", document_).toInteger()
            Integer failures = xpath_.evaluate("/testsuites/testsuite[@id='${testSuiteId}']/@failures", document_).toInteger()
            Integer errors   = xpath_.evaluate("/testsuites/testsuite[@id='${testSuiteId}']/@errors", document_).toInteger()
            StringBuilder sb = new StringBuilder()
            sb.append("EXECUTED:${tests + failures + errors},FAILED:${failures},ERROR:${errors}")
            return sb.toString()
        } else {
            logger_.debug("#getTestSuiteSummary testSuiteId='${testSuiteId}' is not found in the document")
            return null
        }
    }

    /**
     *
     * @param testCaseId e.g, 'Test Cases/main/TC1'
     * @return 'PASSED' or 'FAILED', returns '' if the Test Case not found
     */
    String getTestCaseStatus(String testCaseId) {
        String location = "/testsuites/testsuite/testcase[@name='${testCaseId}']"
        Node testCaseNode = (Node)xpath_.evaluate(location, document_, XPathConstants.NODE)
        if (testCaseNode != null) {
            String status = xpath_.evaluate("/testsuites/testsuite/testcase[@name='${testCaseId}']/@status", document_)
            return status
        } else {
            logger_.debug("#getTestCaseStatus testCaseId='${testCaseId}' is not found in the document")
        }
    }
}
