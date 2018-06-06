package com.kazurayam.kstestresults

import java.nio.file.Path

class TargetURL {

    private TcResult parentTcResult
    private URL url
    private List<MaterialWrapper> materialWrappers

    // ---------------------- constructors & initializers ---------------------
    protected TargetURL(TcResult parent, URL url) {
        this.parentTcResult = parent
        this.url = url
        this.materialWrappers = new ArrayList<MaterialWrapper>()
    }

    // --------------------- properties getter & setter -----------------------
    TcResult getParentTcResult() {
        return this.parentTcResult
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

        String filteredSuffix = suffix.trim().replaceAll('.', '')
        String ammendedSuffix = (filteredSuffix.length() > 0) ? '.' + filteredSuffix : ''

        Path p = this.parentTcResult.getTcDir().resolve(
            "${encodedUrl}${ammendedSuffix}.${fileType.getExtension()}"
            )
        if (this.getMaterialWrapper(p) != null) {
            return this.getMaterialWrapper(p)
        } else {
            MaterialWrapper sw = new MaterialWrapper(this, p, fileType)
            this.materialWrappers.add(sw)
            return sw
        }
    }

    MaterialWrapper findOrNewMaterialWrapper(Path materialFilePath) {
        MaterialWrapper sw = this.getMaterialWrapper(materialFilePath)
        if (sw == null) {
            sw = new MaterialWrapper(this, materialFilePath)
            this.materialWrappers.add(sw)
        }
        return sw
    }

    void addMaterialWrapper(MaterialWrapper materialWrapper) {
        boolean found = false
        for (MaterialWrapper sw : this.materialWrappers) {
            if (sw == materialWrapper) {
                found = true
            }
        }
        if (!found) {
            this.materialWrappers.add(materialWrapper)
        }
    }

    MaterialWrapper getMaterialWrapper(Path materialFilePath) {
        for (MaterialWrapper sw : this.materialWrappers) {
            if (sw.getMaterialFilePath() == materialFilePath) {
                return sw
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
        if (this.parentTcResult == other.getParentTcResult()
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
        result = prime * result + this.getParentTcResult().hashCode()
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