package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Suffix {

    static Logger logger = LoggerFactory.getLogger(Suffix.class)

    static Suffix NULL = new Suffix('')

    private String value

    Suffix(String value) {
        this.value = value
        if (value.contains(Material.MAGIC_DELIMITER)) {
            logger.warn("value '${value}' contained one or more '${Material.MAGIC_DELIMITER}' which were stripped")
            this.value = this.value.replace(Material.MAGIC_DELIMITER, '')
        }
        if (value.contains('/')) {
            logger.warn("value '${value}' contained one or more '/' character(s) which were stripped")
            this.value = this.value.replace('/', '')
        }
        if (value.contains('\\')) {
            logger.warn("value '${value}' contained one or more '\\' character(s) which were stripped")
            this.value = this.value.replace('\\', '')
        }
    }

    String getValue() {
        return this.value
    }

    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Suffix)) { return false }
        Suffix other = (Suffix)obj
        return this.value == other.getValue()
    }

    @Override
    int hashCode() {
        return value.hashCode()
    }

    @Override
    String toString() {
        return this.value
    }

    String toJson() {
        return this.value
    }
}
