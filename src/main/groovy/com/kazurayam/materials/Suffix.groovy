package com.kazurayam.materials

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Given the following files in a directory:
 *
 * - smilechart (1).xls
 * - smilechart (2).xls
 * - smilechart(1).xls
 * - smilechart(2).xls
 * - smilechart(3).xls
 * - smilechart.xls
 *
 * We call the portions of file names (1) and (2) as 'Suffix'.
 *
 * Suffix is an additive to the original file name 'smilechart.xls'
 * to make the name unique in the directory
 * so that the a sibling file can be saved in the directory.
 *
 */
class Suffix implements Comparable<Suffix> {

    static Logger logger_ = LoggerFactory.getLogger(Suffix.class)

    static Suffix NULL = new Suffix(0)

    private int value_ = 0

    /**
     *
     * @param value
     */
    Suffix(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value should be greater than or equal to 0")
        }
        value_ = value
    }

    int getValue() {
        return value_
    }

    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Suffix)) { return false }
        Suffix other = (Suffix)obj
        return this.value_.equals(other.getValue())
    }

    @Override
    int hashCode() {
        return value_.hashCode()
    }

    @Override
    int compareTo(Suffix other) {
        return this.getValue().compareTo(other.getValue())
    }

    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        if (value_ == 0) {
            sb.append('')
        } else {
            sb.append('(')
            sb.append(value_)
            sb.append(')')
        }
        return sb.toString()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"Suffix":')
        sb.append('"')
        sb.append(this.toString())
        sb.append('"')
        sb.append('}')
        return sb.toString()
    }
}
