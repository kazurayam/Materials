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

	/**
	 * When testSuiteId is given as "Test Suites/main/TS1", then
	 * getId()            returns "Test Suites/main/TS1" which is equal to the given arg
	 * getAbbreviatedId() returns "main/TS1"
	 * getValue()         returns "main.TS1"
	 * 
	 * @param testSuiteId for example "Test Suites/main/TS1"
	 */
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

    /**
     *
     * @return e.g., 'Test Suites/main/TS1'
     */
    String getId() {
        return id_
    }

    /**
     *
     * @return e.g, 'main/TS1'
     */
    String getAbbreviatedId() {
        return abbreviatedId_    
    }

    /**
     *
     * @return e.g, 'main.TS1'
     */
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
    boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TSuiteName))
            return false
        TSuiteName other = (TSuiteName)obj
        return this.getValue() == other.getValue()
    }

    @Override
    int hashCode() {
        return this.getValue().hashCode()
    }

    @Override
    int compareTo(TSuiteName other) {
        return this.getValue().compareTo(other.getValue())
    }
}
