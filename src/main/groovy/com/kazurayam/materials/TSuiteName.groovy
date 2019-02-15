package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * I made the constructor of this class public. It was my mistake. I regret it.
 * The public constructor new TSuiteName("TS1") is already used by the applications. For example:
 *     https://github.com/kazurayam/VisualTestingInKatalonStudio/blob/master/Scripts/Main/ImageDiff/Script1535336589503.groovy
 * 
 * I regret it that I published the public constructor. Instead, I should have make the constructor private, and
 * add a static factory method
 *     public static TSuiteName newInstance()
 * 
 * If I did it, I could 
 * (1) add a class com.kazurayam.materials.model.TSuiteNameImpl and
 * (2) move the implementation code of getId() and getValue() there.
 * (3) change the com.kazurayam.materials.TSuiteName class to be skeltal. 
 *     - apply the Composite pattern; it contains a private instance of TSuiteNameImpl
 *     - delegates getId() to the TSuiteNameImpl.getId()
 *     - delegates getValue() to the TSuiteNameImpl.getValue()
 *
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
    private String value_

    TSuiteName(String testSuiteId) {
        Objects.requireNonNull(testSuiteId)
        id_ = testSuiteId
        def s = testSuiteId
        if (s.startsWith(prefix_)) {
            s = s.substring(prefix_.length())
        }
        s = s.replace('/', '.')
        value_ = s
    }

    /**
     *
     * @param path ./Materials/<TSuiteName>/ where <TSuiteName> is 'main.TC1' for example
     */
    TSuiteName(Path path) {
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

    // -------------------- overriding Object properties ----------------------
    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"id": "')
        sb.append(this.getId())
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
