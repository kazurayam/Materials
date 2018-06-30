package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Suffix implements Comparable<Suffix> {

    static Logger logger_ = LoggerFactory.getLogger(Suffix.class)

    static Suffix NULL = new Suffix('')

    private String value_

    /**
     * The following characters will be stripped out of the value
     *
     *   /  (a solidus character)
     *   \  (a reverse solidus character)
     *   §  (a section character)
     *   .  (a dot character)
     *
     * @param value
     */
    Suffix(String value) {
        value_ = value
        value_ = stripChars(value_, '/')
        value_ = stripChars(value_, '\\')
        value_ = stripChars(value_, Material.MAGIC_DELIMITER)
        value_ = stripChars(value_, '.')
    }

    private String stripChars(String source, String ch) {
        if (source.contains(ch)) {
            logger_.warn("Suffix:'${source}' contained one or more '${ch}' character(s) which were stripped")
            return source.replace(ch, '')
        } else {
            return source
        }
    }

    String getValue() {
        return this.value_
    }

    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Suffix)) { return false }
        Suffix other = (Suffix)obj
        return this.value_ == other.getValue()
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
        return this.value_
    }

    String toJson() {
        return this.value_
    }
}
