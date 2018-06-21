package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Material {

    static Logger logger_ = LoggerFactory.getLogger(Material.class)

    protected static final String MAGIC_DELIMITER = '§'

    //private TargetURL parent_
    private TCaseResult parent_
    private URL url_
    private Suffix suffix_
    private FileType fileType_

    Material(URL url, Suffix suffix, FileType fileType) {
        url_ = url
        suffix_ = (suffix == null) ? Suffix.NULL : suffix
        fileType_ = fileType
    }

    /*
    Material setParent(TargetURL parent) {
        parent_ = parent
        return this
    }
    */
    Material setParent(TCaseResult parent) {
        parent_ = parent
        return this
    }

    /*
    TargetURL getParent() {
        return this.getTargetURL()
    }
    */
    TCaseResult getParent() {
        return this.getTCaseResult()
    }

    /*
    TargetURL getTargetURL() {
        return parent_
    }
    */
    TCaseResult getTCaseResult() {
        return parent_
    }

    Path getMaterialFilePath() {
        if (parent_ != null) {
            String fileName = resolveMaterialFileName(url_, suffix_, fileType_)
            //Path materialPath = parent_.getParent().getTCaseDirectory().resolve(fileName).normalize()
            Path materialPath = parent_.getTCaseDirectory().resolve(fileName).normalize()
            return materialPath
        } else {
            logger_.warn("#getMaterialFilePath parent_ is null")
            return null
        }
    }

    URL getURL() {
        return url_
    }

    Suffix getSuffix() {
        return suffix_
    }

    FileType getFileType() {
        return fileType_
    }

    // ---------------- helpers -----------------------------------------------
    static FileType parseFileNameForFileType(String fileName) {
        String[] arr = fileName.split('\\.')
        if (arr.length < 2) {
            return FileType.NULL
        } else {
            String candidate = arr[arr.length - 1]
            try {
                FileType ft = FileType.getByExtension(candidate)
                return ft
            } catch (IllegalArgumentException e) {
                logger_.info("unknown file extension '${candidate}' in the file name '${fileName}'")
                return FileType.NULL
            }
        }
    }

    static Suffix parseFileNameForSuffix(String fileName) {
        FileType ft = parseFileNameForFileType(fileName)
        if (ft != FileType.NULL) {
            String str = fileName.substring(0, fileName.lastIndexOf('.'))
            String[] arr = str.split(Material.MAGIC_DELIMITER)
            if (arr.length < 2) {
                return Suffix.NULL
            }
            if (arr.length > 3) {
                logger_.warn("${fileName} contains 2 or more ${Material.MAGIC_DELIMITER} character. " +
                        "Valid but unexpected.")
            }
            return new Suffix(arr[arr.length - 1])
        } else {
            return Suffix.NULL
        }
    }

    static URL parseFileNameForURL(String fileName) {
        FileType ft = parseFileNameForFileType(fileName)
        if (ft != FileType.NULL) {
            Suffix suffix = parseFileNameForSuffix(fileName)
            String urlstr
            if (suffix != Suffix.NULL) {
                urlstr = fileName.substring(0, fileName.lastIndexOf(Material.MAGIC_DELIMITER))
            } else {
                urlstr = fileName.substring(0, fileName.lastIndexOf('.'))
            }
            String decoded = URLDecoder.decode(urlstr, 'UTF-8')
            try {
                URL url = new URL(decoded)
                return url
            } catch (MalformedURLException e) {
                logger_.warn("#parseFileNameForURL unknown protocol in the var decoded='${decoded}'")
                return null
            }
        } else {
            return null
        }
    }

    static String resolveMaterialFileName(URL url, Suffix suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        if (suffix != Suffix.NULL) {
            return "${encodedUrl}${Material.MAGIC_DELIMITER}${suffix.toString()}.${fileType.getExtension()}"
        } else {
            return "${encodedUrl}.${fileType.getExtension()}"
        }
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Material)) { return false }
        Material other = (Material)obj
        return this.url_ == other.url_ &&
            this.suffix_ == other.suffix_ &&
            this.fileType_ == other.fileType_
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getMaterialFilePath().hashCode()
        return result
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"Material":{')
        sb.append('"url":"' + Helpers.escapeAsJsonText(url_.toString())+ '",')
        sb.append('"suffix":"' + Helpers.escapeAsJsonText(suffix_.toString())+ '",')
        sb.append('"materialFilePath":"' + Helpers.escapeAsJsonText(this.getMaterialFilePath().toString()) + '",')
        sb.append('"fileType":"' + Helpers.escapeAsJsonText(fileType_.toString()) + '"')
        sb.append('}}')
        return sb.toString()
    }

    String toBootstrapTreeviewData() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"text":"' + Helpers.escapeAsJsonText(this.getMaterialFilePath().getFileName().toString())+ '"')
        sb.append('}')
        return sb.toString()
    }
}
