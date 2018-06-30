package com.kazurayam.carmina.material

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class TCaseResult implements Comparable<TCaseResult> {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResult.class)

    private TSuiteResult parent_
    private TCaseName tCaseName_
    private Path tCaseDirectory_
    private List<Material> materials_

    // --------------------- constructors and initializer ---------------------
    /**
     *
     * @param tCaseName
     */
    TCaseResult(TCaseName tCaseName) {
        tCaseName_ = tCaseName
        materials_ = new ArrayList<Material>()
    }

    // --------------------- properties getter & setters ----------------------
    TCaseResult setParent(TSuiteResult parent) {
        parent_ = parent
        tCaseDirectory_ = parent.getTSuiteTimestampDirectory().resolve(tCaseName_.toString())
        return this
    }

    TSuiteResult getParent() {
        return parent_
    }

    TSuiteResult getTSuiteResult() {
        return this.getParent()
    }

    TCaseName getTCaseName() {
        return tCaseName_
    }

    Path getTCaseDirectory() {
        return tCaseDirectory_.normalize()
    }

    // --------------------- create/add/get child nodes ----------------------

    List<Material> getMaterials() {
        return materials_
    }

    Material getMaterial(URL url, Suffix suffix, FileType fileType) {
        for (Material mate : materials_) {
            if (mate.getURL().toString() == url.toString() &&
                mate.getSuffix() == suffix &&
                mate.getFileType() == fileType) {
                return mate
            }
        }
        return null
    }

    void addMaterial(Material material) {
        if (material.getParent() != this) {
            def msg = "material ${material.toJson()} does not have appropriate parent"
            logger_.error("#addMaterial ${msg}")
            throw new IllegalArgumentException(msg)
        }
        boolean found = false
        for (Material mate : materials) {
            if (mate.getURL().toString() == material.getURL().toString() &&
                mate.getSuffix() == material.getSuffix() &&
                mate.getFileType() == material.getFileType()) {
                found = true
            }
        }
        if (!found) {
            materials_.add(material)
            // sort the list materials by Material#compareTo()
            Collections.sort(materials_)
        }
    }

    // -------------------------- helpers -------------------------------------

    // ------------------ overriding Object properties ------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) {
        //    return true
        //}
        if (!(obj instanceof TCaseResult)) {
            return false
        }
        TCaseResult other = (TCaseResult) obj
        return tCaseName_ == other.getTCaseName()
    }

    @Override
    int hashCode() {
        return tCaseName_.hashCode()
    }

    @Override
    int compareTo(TCaseResult other) {
        return tCaseName_.compareTo(other.getTCaseName())
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TCaseResult":{')
        sb.append('"tCaseName":"'   + Helpers.escapeAsJsonText(tCaseName_.toString())   + '",')
        sb.append('"tCaseDir":"'    + Helpers.escapeAsJsonText(tCaseDirectory_.toString())    + '",')
        sb.append('"materials":[')
        def count = 0
        for (Material mate : materials_) {
            if (count > 0) { sb.append(',') }
            sb.append(mate.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

    String toBootstrapTreeviewData() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"text":"' + Helpers.escapeAsJsonText(tCaseName_.toString())+ '",')
        sb.append('"nodes":[')
        def mate_count = 0
        for (Material material : materials_) {
            if (mate_count > 0) {
                sb.append(',')
            }
            sb.append(material.toBootstrapTreeviewData())
            mate_count += 1
        }
        sb.append(']')
        sb.append('}')
        return sb.toString()
    }
}


