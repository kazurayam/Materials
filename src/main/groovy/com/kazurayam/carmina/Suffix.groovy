package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Suffix {

    static Logger logger_ = LoggerFactory.getLogger(Suffix.class)

    static Suffix NULL = new Suffix('')

    private String value_

    Suffix(String value) {
        value_ = value
        if (value.contains(Material.MAGIC_DELIMITER)) {
            logger_.warn("value '${value}' contained one or more '${Material.MAGIC_DELIMITER}' which were stripped")
            value_ = value_.replace(Material.MAGIC_DELIMITER, '')
        }
        if (value.contains('/')) {
            logger_.warn("value '${value}' contained one or more '/' character(s) which were stripped")
            value_ = value_.replace('/', '')
        }
        if (value.contains('\\')) {
            logger_.warn("value '${value}' contained one or more '\\' character(s) which were stripped")
            value_ = value_.replace('\\', '')
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
    String toString() {
        return this.value_
    }

    String toJson() {
        return this.value_
    }
}
