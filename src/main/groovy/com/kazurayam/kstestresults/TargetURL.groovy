package com.kazurayam.kstestresults

import java.nio.file.Path

class TargetURL {

    private TcResult parent
    private URL url
    private List<MaterialWrapper> materialWrappers

    // ---------------------- constructors & initializers ---------------------
    TargetURL(URL url) {
        this.url = url
        this.materialWrappers = new ArrayList<MaterialWrapper>()
    }

    TargetURL setParent(TcResult parent) {
        this.parent = parent
        return this
    }

    // --------------------- properties getter & setter -----------------------
    TcResult getParent() {
        return this.parent
    }

    TcResult getTcResult() {
        return this.getParent()
    }

    URL getUrl() {
        return this.url
    }

    // --------------------- create/add/get child nodes -----------------------

    /**
     * This is a TRICKY part.
     *
     * @param targetPageUrl
     * @return
     */
    MaterialWrapper findOrNewMaterialWrapper(String suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')

        String filteredSuffix = suffix.trim().replace('.', '')
        String ammendedSuffix = (filteredSuffix.length() > 0) ? '.' + filteredSuffix : ''

        Path p = this.parent.getTcDir().resolve(
            "${encodedUrl}${ammendedSuffix}.${fileType.getExtension()}"
            )
        if (this.getMaterialWrapper(p) != null) {
            return this.getMaterialWrapper(p)
        } else {
            MaterialWrapper mw = new MaterialWrapper(p, fileType).setParent(this)
            this.materialWrappers.add(mw)
            return mw
        }
    }

    MaterialWrapper findOrNewMaterialWrapper(Path materialFilePath) {
        MaterialWrapper mw = this.getMaterialWrapper(materialFilePath)
        if (mw == null) {
            mw = new MaterialWrapper(materialFilePath).setParent(this)
            this.materialWrappers.add(mw)
        }
        return mw
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

    // --------------------- helpers ------------------------------------------



    // ------------------------ overriding Object properties ------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof TargetURL)) { return false }
        TargetURL other = (TargetURL)obj
        if (this.parent == other.getTcResult()
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
        result = prime * result + this.getTcResult().hashCode()
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
        sb.append('"url":"' + Helpers.escapeAsJsonText(url.toExternalForm()) + '",')
        sb.append('"materialWrappers":[')
        def count = 0
        for (MaterialWrapper sw : materialWrappers) {
            if (count > 0) {
                sb.append(',')
            }
            sb.append(sw.toJson())
            count += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}