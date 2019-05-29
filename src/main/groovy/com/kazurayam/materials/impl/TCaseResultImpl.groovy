package com.kazurayam.materials.impl

import java.nio.file.Path
import java.time.LocalDateTime
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.model.Suffix

/**
 *
 */
final class TCaseResultImpl extends TCaseResult implements Comparable<TCaseResult> {

    static Logger logger_ = LoggerFactory.getLogger(TCaseResult.class)

    private TSuiteResult parent_
    private TCaseName tCaseName_
    private Path tCaseDirectory_
    private List<Material> materials_
    private LocalDateTime lastModified_
    private long size_

    // --------------------- constructors and initializer ---------------------
    /**
     *
     * @param tCaseName
     */
    TCaseResultImpl(TCaseName tCaseName) {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        tCaseName_ = tCaseName
        materials_ = new ArrayList<Material>()
        lastModified_ = LocalDateTime.MIN
        size_ = 0
    }

    // --------------------- properties getter & setters ----------------------
    @Override
    TCaseResult setParent(TSuiteResult parent) {
        Objects.requireNonNull(parent, "parent must not be null")
        parent_ = parent
        tCaseDirectory_ = parent.getTSuiteTimestampDirectory().resolve(tCaseName_.getValue())
        return this
    }

    @Override
    TSuiteResult getParent() {
        return parent_
    }

    //@Override
    //TSuiteResult getTSuiteResult() {
    //    return this.getParent()
    //}

    @Override
    TCaseName getTCaseName() {
        return tCaseName_
    }

    @Override
    Path getTCaseDirectory() {
        if (tCaseDirectory_ != null) {
            return tCaseDirectory_.normalize()
        } else {
            return null
        }
    }

    @Override
    TCaseResult setLastModified(LocalDateTime lastModified) {
        Objects.requireNonNull(lastModified, "lastModified must not be null")
        lastModified_ = lastModified
        return this
    }

    @Override
    LocalDateTime getLastModified() {
        return lastModified_
    }
    
    @Override
    TCaseResult setSize(long size) {
        size_ = size
        return this
    }
    
    @Override
    long getSize() {
        return size_
    }

    // --------------------- create/add/get child nodes ----------------------

    /**
     * @return all of Material objects in the TCaseResult directory
     */
    @Override
    List<Material> getMaterialList() {
        return materials_
    }

    /**
     * @return select Material objects while matching the file name with the pattern.
     * The pattern is interpreted as a RegExp (case-insensitive) if the second arg is true,
     * otherwise the pattern is interpreted as the file name itself.
     * The 2nd argument is optional, default is false.
     */
    @Override
    List<Material> getMaterialList(String pattern, boolean isRegex = false) {
        Objects.requireNonNull(pattern, "pattern must not be null")
        List<Material> result = new ArrayList<>()
        if (isRegex) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            for (Material mate: this.materials_) {
                Matcher m = p.matcher(mate.getPath().getFileName().toString())
                if (m.find()) {
                    result.add(mate)
                }
            }
        } else {
            for (Material mate: this.materials_) {
                if (mate.getPath().getFileName().toString().equals(pattern)) {
                    result.add(mate)
                }
            }    
        }
        return result
    }
    
    //@Override
    List<Material> getMaterialList(String subpath, URL url, FileType fileType) {
        Objects.requireNonNull(subpath)
        Objects.requireNonNull(url)
        Objects.requireNonNull(fileType)
        logger_.debug("#getMaterials subpath=${subpath.toString()}, url=${url.toString()}, fileType=${fileType.toString()}")
        List<Material> list = new ArrayList<Material>()
        logger_.debug("#getMaterials materials_.size()=${materials_.size()}")
        for (Material mate : materials_) {
            logger_.debug("#getMaterials mate.getDirpath()=${mate.getSubpath()}, mate.getURL()=${mate.getURL()}, mate.getFileType()=${mate.getFileType()}, mate.getPath()=${mate.getPath()}}")
            if (mate.getSubpath() == subpath &&
                mate.getURL().toString() == url.toString() &&
                mate.getFileType() == fileType) {
                list.add(mate)
            }
        }
        return Collections.unmodifiableList(list)
    }

    @Override
    Material getMaterial(String subpath, URL url, Suffix suffix, FileType fileType) {
        Objects.requireNonNull(subpath)
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

    //@Override
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
            //logger_.debug("#getMaterial(Path) matePath=${matePath} subpath=${subpath} subpathUnderTCaseResult=${subpathUnderTCaseResult}")
            if (subpath.equals(subpathUnderTCaseResult)) {
                return mate
            }
        }
        return null
    }

    @Override
    boolean addMaterial(Material material) {
        Objects.requireNonNull(material, "material must not be null")
        if (material.getParent() != this) {
            def msg = "material ${material.toJsonText()} does not have appropriate parent"
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
    @Override
    Suffix allocateNewSuffix(String subpath, URL url, FileType fileType) {
        Objects.requireNonNull(subpath)
        Objects.requireNonNull(url)
        Objects.requireNonNull(fileType)
        logger_.debug("#allocateNewSuffix subpath=${subpath}, url=${url.toString()}, fileType=${fileType.toString()}")
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
        return this.toJsonText()
    }

    @Override
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TCaseResult":{')
        sb.append('"tCaseName":'   + this.getTCaseName().toString()   + ',')
        sb.append('"tCaseDir":"'    + Helpers.escapeAsJsonText(this.getTCaseDirectory().toString())    + '",')
        sb.append('"lastModified":"' + this.getLastModified().toString() + '",')
        sb.append('"length":' + this.getSize()+ ',')
        sb.append('"materials":[')
        def count = 0
        for (Material mate : materials_) {
            if (count > 0) { sb.append(',') }
            sb.append(mate.toJsonText())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }

}


