package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TargetURL {

    static Logger logger = LoggerFactory.getLogger(TargetURL.class)

    private TCaseResult parent
    private URL url
    private List<Material> materials

    // ---------------------- constructors & initializers ---------------------
    TargetURL(URL url) {
        this.url = url
        this.materials = new ArrayList<Material>()
    }

    TargetURL setParent(TCaseResult parent) {
        this.parent = parent
        return this
    }

    // --------------------- properties getter & setter -----------------------
    TCaseResult getParent() {
        return this.getTCaseResult()
    }

    TCaseResult getTCaseResult() {
        return this.parent
    }

    URL getUrl() {
        return this.url
    }

    // --------------------- create/add/get child nodes -----------------------


    Material getMaterial(Suffix suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        Path p
        if (suffix != Suffix.NULL) {
            p = this.parent.getTCaseDir().resolve(
                "${encodedUrl}${Material.MAGIC_DELIMITER}${suffix.toString()}.${fileType.getExtension()}"
                )
        } else {
            p = this.parent.getTCaseDir().resolve(
                "${encodedUrl}.${fileType.getExtension()}"
                )
        }
        logger.debug("#getMaterial(Suffix,FileType) p=${p.toString()}")
        return this.getMaterial(p)
    }

    Material getMaterial(Path materialFilePath) {
        for (Material mw : this.materials) {
            if (mw.getMaterialFilePath() == materialFilePath) {
                return mw
            }
        }
        return null
    }

    List<Material> getMaterials() {
        return this.materials
    }

    void addMaterial(Material material) {
        boolean found = false
        for (Material mw : this.materials) {
            if (mw == material) {
                found = true
            }
        }
        if (!found) {
            this.materials.add(material)
        }
    }


    // --------------------- helpers ------------------------------------------



    // ------------------------ overriding Object properties ------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TargetURL)) { return false }
        TargetURL other = (TargetURL)obj
        if (this.parent == other.getTCaseResult()
            && this.url == other.getUrl()) {
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
        if (url != null) {
            sb.append('"url":"' + Helpers.escapeAsJsonText(url.toExternalForm()) + '",')
        }
        sb.append('"materials":[')
        def count = 0
        for (Material mw : materials) {
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