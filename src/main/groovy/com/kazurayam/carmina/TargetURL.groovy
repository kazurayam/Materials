package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TargetURL {

    static Logger logger_ = LoggerFactory.getLogger(TargetURL.class)

    private TCaseResult parent_
    private URL url_
    private List<Material> materials_

    // ---------------------- constructors & initializers ---------------------
    TargetURL(URL url) {
        url_ = url
        materials_ = new ArrayList<Material>()
    }

    TargetURL setParent(TCaseResult parent) {
        parent_ = parent
        return this
    }

    // --------------------- properties getter & setter -----------------------
    TCaseResult getParent() {
        return this.getTCaseResult()
    }

    TCaseResult getTCaseResult() {
        return parent_
    }

    URL getUrl() {
        return url_
    }

    // --------------------- create/add/get child nodes -----------------------


    Material getMaterial(Suffix suffix, FileType fileType) {
        /*
        String encodedUrl = URLEncoder.encode(url_.toExternalForm(), 'UTF-8')
        Path p
        if (suffix != Suffix.NULL) {
            p = parent_.getTCaseDirectory().resolve(
                "${encodedUrl}${Material.MAGIC_DELIMITER}${suffix.toString()}.${fileType.getExtension()}"
                )
        } else {
            p = parent_.getTCaseDirectory().resolve(
                "${encodedUrl}.${fileType.getExtension()}"
                )
        }
        logger_.debug("#getMaterial(Suffix,FileType) p=${p.toString()}")
        return this.getMaterial(p)
        */
        logger_.debug("#getMaterial materials_.size()=${materials.size()}")
        for (Material mate : materials_) {
            logger_.debug("#getMaterial" +
                " mate.getSuffix()=${mate.getSuffix().toString()} arg.suffix=${suffix.toString()}" +
                " mate.getFileType()=${mate.getFileType().toString()} arg.fileType=${fileType.toString()}")
            if (mate.getSuffix() == suffix && mate.getFileType() == fileType) {
                return mate
            }
        }
        return null
    }

    Material getMaterial(Path materialFilePath) {
        for (Material mw : materials_) {
            if (mw.getMaterialFilePath() == materialFilePath) {
                return mw
            }
        }
        return null
    }

    List<Material> getMaterials() {
        return materials_
    }

    void addMaterial(Material material) {
        boolean found = false
        for (Material mw : materials_) {
            if (mw == material) {
                found = true
            }
        }
        if (!found) {
            materials_.add(material)
        }
    }


    // --------------------- helpers ------------------------------------------



    // ------------------------ overriding Object properties ------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TargetURL)) { return false }
        TargetURL other = (TargetURL)obj
        if (parent_ == other.getTCaseResult()
            && url_ == other.getUrl()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTCaseResult().hashCode()
        result = prime * result + this.getUrl().hashCode()
        return result
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TargetURL":{')
        if (url_ != null) {
            sb.append('"url":"' + Helpers.escapeAsJsonText(url_.toExternalForm()) + '",')
        }
        sb.append('"materials":[')
        def count = 0
        for (Material mw : materials_) {
            if (count > 0) {
                sb.append(',')
            }
            sb.append(mw.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}