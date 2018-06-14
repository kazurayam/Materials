package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TargetURL {

    static Logger logger = LoggerFactory.getLogger(TargetURL.class)

    private TCaseResult parent
    private URL url
    private List<MaterialWrapper> materialWrappers

    // ---------------------- constructors & initializers ---------------------
    TargetURL(URL url) {
        this.url = url
        this.materialWrappers = new ArrayList<MaterialWrapper>()
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


    static Path resolveMaterialWrapperPath(TCaseResult tCaseResult, URL url, Suffix suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        Path p
        if (suffix != Suffix.NULL) {
            p = tCaseResult.getTCaseDir().resolve(
                "${encodedUrl}${MaterialWrapper.MAGIC_DELIMITER}${suffix.toString()}.${fileType.getExtension()}"
                )
        } else {
            p = tCaseResult.getTCaseDir().resolve(
                "${encodedUrl}.${fileType.getExtension()}"
                )
        }
        return p
    }
    
    
    /*
    MaterialWrapper findOrNewMaterialWrapper(Suffix suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        Path p
        if (suffix != Suffix.NULL) {
            p = this.parent.getTCaseDir().resolve(
                "${encodedUrl}${MaterialWrapper.MAGIC_DELIMITER}${suffix.toString()}.${fileType.getExtension()}"
                )
        } else {
            p = this.parent.getTCaseDir().resolve(
                "${encodedUrl}.${fileType.getExtension()}"
                )
        }
        if (this.getMaterialWrapper(p) != null) {
            return this.getMaterialWrapper(p)
        } else {
            MaterialWrapper mw = new MaterialWrapper(p, fileType).setParent(this)
            this.materialWrappers.add(mw)
            return mw
        }
    }
     */



    MaterialWrapper getMaterialWrapper(Suffix suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        Path p
        if (suffix != Suffix.NULL) {
            p = this.parent.getTCaseDir().resolve(
                "${encodedUrl}${MaterialWrapper.MAGIC_DELIMITER}${suffix.toString()}.${fileType.getExtension()}"
                )
        } else {
            p = this.parent.getTCaseDir().resolve(
                "${encodedUrl}.${fileType.getExtension()}"
                )
        }
        logger.debug("#getMaterialWrapper(Suffix,FileType) p=${p.toString()}")
        return this.getMaterialWrapper(p)
    }

    MaterialWrapper getMaterialWrapper(Path materialFilePath) {
        for (MaterialWrapper mw : this.materialWrappers) {
            if (mw.getMaterialFilePath() == materialFilePath) {
                return mw
            }
        }
        return null
    }

    List<MaterialWrapper> getMaterialWrappers() {
        return this.materialWrappers
    }

    void addMaterialWrapper(MaterialWrapper materialWrapper) {
        boolean found = false
        for (MaterialWrapper mw : this.materialWrappers) {
            if (mw == materialWrapper) {
                found = true
            }
        }
        if (!found) {
            this.materialWrappers.add(materialWrapper)
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
        sb.append('"materialWrappers":[')
        def count = 0
        for (MaterialWrapper mw : materialWrappers) {
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