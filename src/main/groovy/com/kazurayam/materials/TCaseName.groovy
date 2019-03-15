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
    private String value_

    /**
     *
     * @param testCaseId
     */
    TCaseName(String testCaseId) {
        Objects.requireNonNull(testCaseId)
        id_ = testCaseId
        def s = testCaseId
        if (s.startsWith(prefix_)) {
            s = s.substring(prefix_.length())
        }
        s = s.replace('/', '.')
        value_ = s
    }

    /**
     *
     * @param path ./Material/main.TC1/yyyyMMdd_hhmmss/<TestCaseName> where TestCaseName is 'main.TC1' for example
     */
    TCaseName(Path path) {
        Objects.requireNonNull(path)
        value_ = path.getFileName().toString()
        id_ = prefix_ + value_.replace('.', '/')
    }

    String getId() {
        return id_
    }

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
