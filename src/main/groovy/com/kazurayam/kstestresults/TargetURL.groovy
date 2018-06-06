package com.kazurayam.kstestresults

import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

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
     * This is the core trick.
     *
     * @param targetPageUrl
     * @return
     */
    MaterialWrapper findOrNewMaterialWrapper(String suffix, FileExtension ext) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        Path p = this.parentTcResult.getTcDir().resolve(

            "${encodedUrl}${suffix}.${ext.getExtension()}"

            )
        if (this.getMaterialWrapper(p) != null) {
            return this.getMaterialWrapper(p)
        } else {
            MaterialWrapper sw = new MaterialWrapper(this, p)
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

    /**
     * accept a string in a format (<any string>[/\])(<enocoded URL string>)(.[0-9]+)?(.png)
     * and returns a List<String> of ['<decoded URL>', '[1-9][0-9]*'] or ['<decoded URL>']
     * @param materialFileName
     * @return empty List<String> if unmatched
     */
    static final int flag = Pattern.CASE_INSENSITIVE
    static final String EXTENSION_PART_REGEX = '(\\.([0-9]+))?\\.png$'
    static final Pattern EXTENSION_PART_PATTERN = Pattern.compile(EXTENSION_PART_REGEX, flag)
    static List<String> parseMaterialFileName(String materialFileName) {
        List<String> values = new ArrayList<String>()
        String preprocessed = materialFileName.replaceAll('\\\\', '/')  // Windows XFile path separator -> UNIX
        List<String> elements = preprocessed.split('[/]')
        if (elements.size() > 0) {
            String fileName = elements.getAt(elements.size() - 1)
            Matcher m = EXTENSION_PART_PATTERN.matcher(fileName)
            boolean b = m.find()
            if (b) {
                String encodedUrl = fileName.replaceFirst(EXTENSION_PART_REGEX, '')
                String decodedUrl = URLDecoder.decode(encodedUrl, 'UTF-8')
                values.add(decodedUrl)
                if (m.group(2) != null) {
                    values.add(m.group(2))
                }
            }
        }
        return values
    }

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