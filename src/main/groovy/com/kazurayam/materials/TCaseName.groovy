package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * I made the constructor of this class public. It was my mistake. I regret it.
 * The public constructor new TCaseName("TC1") is already used by the applications. For example:
 *     https://github.com/kazurayam/VisualTestingInKatalonStudio/blob/master/Scripts/Main/ImageDiff/Script1535336589503.groovy
 * 
 * I regret it that I published the public constructor. Instead, I should have make the constructor private, and
 * add a static factory method
 *     public static TCaseName newInstance()
 * 
 * If I did it, I could 
 * (1) add a class com.kazurayam.materials.model.TCaseNameImpl and
 * (2) move the implementation code of getId() and getValue() there.
 * (3) change the com.kazurayam.materials.TCaseName class to be skeltal.
 *     - apply the Composite pattern; it contains a private instance of TCaseNameImpl
 *     - delegates getId() to the TCaseNameImpl.getId()
 *     - delegates getValue() to the TCaseNameImpl.getValue()
 * 
 * @author kazurayam
 *
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
