package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author kazurayam
 *
 */
final class TSuiteName implements Comparable<TSuiteName> {
    
    static final TSuiteName NULL = new TSuiteName('')

    static Logger logger_ = LoggerFactory.getLogger(TSuiteName.class)

    static final String SUITELESS_DIRNAME = '_'

    static final TSuiteName SUITELESS = new TSuiteName(SUITELESS_DIRNAME)

    static final String prefix_ = 'Test Suites/'

    private String id_
    private String abbreviatedId_
    private String value_

    TSuiteName(String testSuiteId) {
        Objects.requireNonNull(testSuiteId)
        id_ = testSuiteId
        abbreviatedId_ = abbreviate(testSuiteId)
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
     * @param path ./Materials/<TSuiteName>/ where <TSuiteName> is 'main.TC1' for example
     */
    TSuiteName(Path path) {
        Objects.requireNonNull(path)
        value_ = path.getFileName().toString()
        id_ = prefix_ + value_.replace('.', '/')
        abbreviatedId_ = abbreviate(id_)
    }

    String getId() {
        return id_
    }

    String getAbbreviatedId() {
        return abbreviatedId_    
    }
    
    String getValue() {
        return value_
    }

    // -------------------- overriding Object properties ----------------------
    @Override
    String toString() {
        return this.toJsonText()
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"id": "')
        sb.append(this.getId())
        sb.append('",')
        sb.append('"abbreviatedId": "')
        sb.append(this.getAbbreviatedId())
        sb.append('",')
        sb.append('"value": "')
        sb.append(this.getValue())
        sb.append('"')
        sb.append('}')
        return sb.toString()
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TSuiteName))
            return false
        TSuiteName other = (TSuiteName)obj
        return this.getValue().equals(other.getValue())
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }

    @Override
    int compareTo(TSuiteName other) {
        return this.getValue().compareTo(other.getValue())
    }
}
