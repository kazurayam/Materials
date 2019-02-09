package com.kazurayam.materials.model

import java.nio.file.Path
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteResult

/**
 *
 */
final class TCaseResult implements Comparable<TCaseResult> {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResult.class)

    private TSuiteResult parent_
    private TCaseName tCaseName_
    private Path tCaseDirectory_
    private List<Material> materials_
    private LocalDateTime lastModified_
    private long length_

    // --------------------- constructors and initializer ---------------------
    /**
     *
     * @param tCaseName
     */
    TCaseResult(TCaseName tCaseName) {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        tCaseName_ = tCaseName
        materials_ = new ArrayList<Material>()
        lastModified_ = LocalDateTime.MIN
        length_ = 0
    }

    // --------------------- properties getter & setters ----------------------
    TCaseResult setParent(TSuiteResult parent) {
        Objects.requireNonNull(parent, "parent must not be null")
        parent_ = parent
        tCaseDirectory_ = parent.getTSuiteTimestampDirectory().resolve(tCaseName_.getValue())
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

    TCaseResult setLastModified(LocalDateTime lastModified) {
        Objects.requireNonNull(lastModified, "lastModified must not be null")
        lastModified_ = lastModified
        return this
    }

    LocalDateTime getLastModified() {
        return lastModified_
    }
    
    TCaseResult setLength(long length) {
        length_ = length
        return this
    }
    
    long getLength() {
        return length_
    }

    // --------------------- create/add/get child nodes ----------------------

    List<Material> getMaterialList() {
        return materials_
    }

    List<Material> getMaterialList(Path dirpath, URL url, FileType fileType) {
        Objects.requireNonNull(dirpath)
        Objects.requireNonNull(url)
        Objects.requireNonNull(fileType)
        logger_.debug("#getMaterials subpath=${dirpath.toString()}, url=${url.toString()}, fileType=${fileType.toString()}")
        List<Material> list = new ArrayList<Material>()
        logger_.debug("#getMaterials materials_.size()=${materials_.size()}")
        for (Material mate : materials_) {
            logger_.debug("#getMaterials mate.getDirpath()=${mate.getDirpath()}, mate.getURL()=${mate.getURL()}, mate.getFileType()=${mate.getFileType()}, mate.getPath()=${mate.getPath()}}")
            if (mate.getDirpath() == dirpath &&
                mate.getURL().toString() == url.toString() &&
                mate.getFileType() == fileType) {
                list.add(mate)
            }
        }
        return Collections.unmodifiableList(list)
    }


    Material getMaterial(Path dirpath, URL url, Suffix suffix, FileType fileType) {
        Objects.requireNonNull(dirpath)
        Objects.requireNonNull(url)
        Objects.requireNonNull(suffix)
        Objects.requireNonNull(fileType)
        for (Material mate : materials_) {
            if (mate.getURL().toString() == url.toString() &&
                mate.getSuffix() == suffix &&
                mate.getFileType() == fileType) {
                return mate
            }
        }
        return null
    }


    Material getMaterial(Path subpathUnderTCaseResult) {
        Objects.requireNonNull(subpathUnderTCaseResult)
        if (parent_ == null) {
            throw new IllegalStateException("parent_ is null")
        }
        List<Material> materials = this.getMaterialList()
        //logger_.debug("#getMaterial materials.size()=${materials.size()}")
        for (Material mate : materials) {
            Path matePath = mate.getPath()
            Path subpath = this.getTCaseDirectory().relativize(matePath)
            logger_.debug("#getMaterial(Path) matePath=${matePath} subpath=${subpath} subpathUnderTCaseResult=${subpathUnderTCaseResult}")
            if (subpath.equals(subpathUnderTCaseResult)) {
                return mate
            }
        }
        return null
    }

    boolean addMaterial(Material material) {
        Objects.requireNonNull(material, "material must not be null")
        if (material.getParent() != this) {
            def msg = "material ${material.toJson()} does not have appropriate parent"
            logger_.error("#addMaterial ${msg}")
            throw new IllegalArgumentException(msg)
        }
        boolean found = false
        for (Material mate : materialList) {
            if (mate == material) {
                found = true
            }
        }
        if (!found) {
            materials_.add(material)
            // sort the list materials by Material#compareTo()
            Collections.sort(materials_)
        }
        return found
    }

    // -------------------------- helpers -------------------------------------
    Suffix allocateNewSuffix(Path subpath, URL url, FileType fileType) {
        Objects.requireNonNull(subpath)
        Objects.requireNonNull(url)
        Objects.requireNonNull(fileType)
        logger_.debug("#allocateNewSuffix subpath=${subpath.toString()}, url=${url.toString()}, fileType=${fileType.toString()}")
        List<Suffix> suffixList = new ArrayList<>()
        List<Material> mateList = this.getMaterialList(subpath, url, fileType)
        logger_.debug("#allocateNewSuffix mateList.size()=${mateList.size()}")
        for (Material mate : mateList) {
            suffixList.add(mate.getSuffix())
        }
        Collections.sort(suffixList)
        logger_.debug("#allocateNewSuffix suffixList is ${suffixList.toString()}")
        Suffix newSuffix = null
        for (Suffix su : suffixList) {
            int next = su.getValue() + 1
            newSuffix = new Suffix(next)
            if (!suffixList.contains(newSuffix)) {
                return newSuffix
            }
        }
        return newSuffix
    }

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
        return tCaseName_.equals(other.getTCaseName())
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
        sb.append('],')
        sb.append('"lastModified":"' + lastModified_.toString() + '"')
        sb.append('}}')
        return sb.toString()
    }

}


