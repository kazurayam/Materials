package com.kazurayam.materials.view

import com.kazurayam.materials.TSuiteResultId

import java.nio.file.Files
import java.nio.file.Path

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Node

/**
 *
 * @author kazurayam
 *
 */
final class JUnitReportWrapper {

    static Logger logger_ = LoggerFactory.getLogger(JUnitReportWrapper.class)

    static final DocumentBuilderFactory dbFactory_
    static {
        dbFactory_ = DocumentBuilderFactory.newInstance()
        dbFactory_.setNamespaceAware(true)
    }
    private Document document_
    private XPath xpath_
    private File file_
    
	JUnitReportWrapper(Path path) {
        this(path.toFile())
    }

    JUnitReportWrapper(File file) {
        Objects.requireNonNull(file)
        this.file_ = file
        DocumentBuilder db = dbFactory_.newDocumentBuilder()
        Document document = db.parse(file)
        init(document)
    }

    JUnitReportWrapper(Document document) {
        Objects.requireNonNull(document)
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
        Objects.requireNonNull(testSuiteId)
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
            logger_.warn("#getTestSuiteSummary testSuiteId='${testSuiteId}' is not found in the file ${file_}")
            return null
        }
    }

    /**
     *
     * @param testCaseId e.g, 'Test Cases/main/TC1'
     * @return 'PASSED' or 'FAILED'. will return '----' if the testCaseId is not found in the JUnit_Repoert.xml
     */
    String getTestCaseStatus(String testCaseId) {
        Objects.requireNonNull(testCaseId)
        String location = "/testsuites/testsuite/testcase[@name='${testCaseId}']"
        Node testCaseNode = (Node)xpath_.evaluate(location, document_, XPathConstants.NODE)
        if (testCaseNode != null) {
            String status = xpath_.evaluate(location + "/@status", document_)
            return status
        } else {
            String msg = "#getTestCaseStatus testCaseId='${testCaseId}' is not found in the file ${file_}"
            logger_.warn(msg)
            return '----'
        }
    }
}
