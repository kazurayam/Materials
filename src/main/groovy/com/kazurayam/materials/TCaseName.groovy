package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *  @author kazurayam
 */
final class TCaseName implements Comparable<TCaseName> {

    static Logger logger_ = LoggerFactory.getLogger(TCaseName.class)

    static final String prefix_ = 'Test Cases/'

    private String id_
    private String abbreviatedId_
    private String value_

    /**
     * When testCaseId == 'Test Cases'
     * @param testCaseId e.g. 'Test Cases/test/com.kazurayam.visualtesting/AllTestRunner'
     */
    TCaseName(String testCaseId) {
        Objects.requireNonNull(testCaseId)
        id_ = testCaseId
        abbreviatedId_ = abbreviate(testCaseId)
        value_ = abbreviatedId_.replace('/', '.')
    }

    private String abbreviate(String id) {
        if (id.startsWith(prefix_)) {
            return id.substring(prefix_.length())
        } else {
            return id
        }
    }
    
    /**
     *
     * @param path ./Material/main.TC1/yyyyMMdd_hhmmss/<TestCaseName> where TestCaseName is 'main.TC1' for example
     */
    TCaseName(Path path) {
        Objects.requireNonNull(path)
        value_ = path.getFileName().toString()
        id_ = prefix_ + value_.replace('.', '/')
        abbreviatedId_ = abbreviate(id_)
    }
    

    /**
     * @return When given testCaseId is 'Test Cases/test/com.kazurayam.visualtesting/AllTestRunner',
     * then returns 'Test Cases/test/com.kazurayam.visualtesting/AllTestRunner'. 
     * Just the same as the contructor parameter
     * 
     */
    String getId() {
        return id_
    }

    /**
     * @return When given testCaseId is 'Test Cases/test/com.kazurayam.visualtesting/AllTestRunner',
     * then returns 'test/com.kazurayam.visualtesting/AllTestRunner'.
     * The prefix 'Test Cases/' is abbreviated.
     * 
     * @return
     */
    String getAbbreviatedId() {
        return abbreviatedId_
    }
    
    /**
     * @return When given testCaseId is 'Test Cases/test/com.kazurayam.visualtesting/AllTestRunner',
     * then returns 'test.com.kazurayam.visualtesting.AllTestRunner'.
     * The prefix 'Test Cases/' is abbreviated, and '/' is translated to '.'
     * 
     * The value is the name of directory under the 'Materials' directory:
     * Materials/<TSuiteName>/<TSuiteTimestamp>/<TCaseName>
     * 
     */
    String getValue() {
        return value_
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    String toString() {
        return this.toJsonText()
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"id": "')
        sb.append(id_)
        sb.append('",')
        sb.append('"abbreviatedId": "')
        sb.append(abbreviatedId_)
        sb.append('",')
        sb.append('"value": "')
        sb.append(value_)
        sb.append('"')
        sb.append('}')
        return sb.toString()
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TCaseName))
            return false
        TCaseName other = (TCaseName)obj
        return this.getValue().equals(other.getValue())
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }

    @Override
    int compareTo(TCaseName other) {
        return this.getValue().compareTo(other.getValue())
    }
}
