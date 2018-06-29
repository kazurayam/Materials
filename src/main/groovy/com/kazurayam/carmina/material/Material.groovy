package com.kazurayam.carmina.material

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.xml.XmlUtil

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

    Material setParent(TCaseResult parent) {
        parent_ = parent
        return this
    }

    TCaseResult getParent() {
        return this.getTCaseResult()
    }

    TCaseResult getTCaseResult() {
        return parent_
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

    // ---------------- business ----------------------------------------------
    /**
     * Returns the java.nio.file.Path of the Material.
     * The Path is based on the directory given to the
     * <pre>com.kazurayam.carmina.TestMaterialsRespositoryFactory.createInstance(Path baseDir)</pre>
     *
     * @return the Path of the Material
     */
    Path getMaterialFilePath() {
        if (parent_ != null) {
            String fileName = resolveMaterialFileName(url_, suffix_, fileType_)
            Path materialPath = parent_.getTCaseDirectory().resolve(fileName).normalize()
            return materialPath
        } else {
            logger_.warn("#getMaterialFilePath parent_ is null")
            return null
        }
    }

    /**
     *
     * @return
     */
    Path getPathRelativeToTSuiteTimestamp() {
        return getPathRelativeTo(this.getParent().getParent().getTSuiteTimestampDirectory())
    }

    Path getPathRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return getPathRelativeTo(rootDir)
    }

    Path getPathRelativeTo(Path base) {
        return base.relativize(this.getMaterialFilePath())
    }

    /**
     *
     * @return
     */
    String getHrefRelativeToTSuiteTimestamp() {
        Path timestampDir = this.getParent().getParent().getTSuiteTimestampDirectory()
        return this.getHrefRelativeTo(timestampDir)
    }

    String getHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getHrefRelativeTo(rootDir)
    }

    private String getHrefRelativeTo(Path base) {
        Path tCaseResultRelativeToTSuiteTimestamp = base.relativize(
                this.getParent().getTCaseDirectory())
        Path href = tCaseResultRelativeToTSuiteTimestamp.resolve(
                this.resolveEncodedMaterialFilename(url_, suffix_, fileType_))
        return href.normalize().toString().replace('\\', '/')
    }



    // ---------------- helpers -----------------------------------------------
    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F.§atoz.png</pre>' is given
     * as fileName argument, then returns com.kazurayam.carmina.FileType.PNG
     *
     * @param fileName
     * @return
     */
    static FileType parseFileNameForFileType(String fileName) {
        String[] arr = fileName.split('\\.')
        if (arr.length < 2) {
            return FileType.NULL
        } else {
            String candidate = arr[arr.length - 1]
            FileType ft = FileType.getByExtension(candidate)
        }
    }

    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png</pre>' is given
     * as fileName argument, then returns an instance of com.kazurayam.carmina.Suffix of '<pre>atoz</pre>'
     *
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F.png</pre>' is given
     * as fileName argument, then returns com.kazurayam.carmina.Suffix.NULL
     *
     * @param fileName
     * @return
     */
    static Suffix parseFileNameForSuffix(String fileName) {
        FileType ft = parseFileNameForFileType(fileName)
        if (ft != FileType.NULL) {
            String str = fileName.substring(0, fileName.lastIndexOf('.'))
            String[] arr = str.split(Material.MAGIC_DELIMITER)
            if (arr.length < 2) {
                return Suffix.NULL
            }
            if (arr.length > 3) {
                logger_.warn("#parseFileNameForSuffix ${fileName} contains 2 or " +
                    "more ${Material.MAGIC_DELIMITER} character. " +
                    "Valid but unexpected.")
            }
            return new Suffix(arr[arr.length - 1])
        } else {
            return Suffix.NULL
        }
    }

    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png</pre>' is given
     * as fileName argument, then returns an instance of java.net.URL of
     * '<pre>http://demoauto.katalon.com</pre>'
     *
     * @param fileName
     * @return
     */
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

    /**
     * Determines the file name of a Material. The file name is in the format:
     *
     * <pre>&lt;encoded URL string&gt;.&lt;file extension&gt;</pre>
     *
     * for example:
     *
     * <pre>http:%3A%2F%2Fdemoaut.katalon.com%2F.png</pre>
     *
     * or
     *
     * <pre>&lt;encoded URL string&gt;§&lt;suffix string&gt;.&lt;file extension&gt;</pre>
     *
     * for example:
     *
     * <pre>http:%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png</pre>
     *
     * @param url
     * @param suffix
     * @param fileType
     * @return
     */
    static String resolveMaterialFileName(URL url, Suffix suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        String encodedSuffix = URLEncoder.encode(suffix.toString(), 'UTF-8')
        if (suffix != Suffix.NULL) {
            return "${encodedUrl}${Material.MAGIC_DELIMITER}${encodedSuffix}.${fileType.getExtension()}"
        } else {
            return "${encodedUrl}.${fileType.getExtension()}"
        }
    }

    static String resolveEncodedMaterialFilename(URL url, Suffix suffix, FileType fileType ) {
        String doubleEncodedUrl = URLEncoder.encode(URLEncoder.encode(url.toExternalForm(), 'UTF-8'), 'UTF-8')
        String doubleEncodedSuffix = URLEncoder.encode(URLEncoder.encode(suffix.toString(), 'UTF-8'), 'UTF-8')
        if (suffix != Suffix.NULL) {
            return "${doubleEncodedUrl}${Material.MAGIC_DELIMITER}${doubleEncodedSuffix}.${fileType.getExtension()}"
        } else {
            return "${doubleEncodedUrl}.${fileType.getExtension()}"
        }
    }

    // ---------------- overriding java.lang.Object properties --------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Material)) { return false }
        Material other = (Material)obj
        return this.url_.toString() == other.url_.toString() &&
            this.suffix_ == other.suffix_ &&
            this.fileType_ == other.fileType_
    }

    @Override
    int hashCode() {
        final int prime = 3
        int result = 1
        if (this.parent_ != null) {
            if (this.parent_.parent_ != null) {
                result = prime * result + this.parent_.parent_.hashCode()
            }
            result = prime * result + this.parent_.hashCode()
        }
        result = prime * result + this.url_.toString().hashCode()
        result = prime * result + this.suffix_.hashCode()
        result = prime * result + this.fileType_.hashCode()
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
        sb.append('"text":"' + Helpers.escapeAsJsonText(this.getIdentifier())+ '",')
        sb.append('"href":"#' + this.hashCode() + '"')
        sb.append('}')
        return sb.toString()
    }

    /**
     * <pre>
     *     <div id="XXXXXXXX" class=”modal fade”>
     *         <div class=”modal-dialog modalcenter” role=”document”>
     *             <div class=”modal-content”>
     *                 <div class=”modal-header”>
     *                     <h4 class=”modal-title” id=”XXXXXXXXtitle”>http://demoaut.katalon.com/</h4>
     *                 </div>
     *                 <div class=”modal-body”>
     *                     <img src="TS1/TS1/20180624_043621/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png" alt="">
     *                 </div>
     *                 <div class=”modal-footer”>
     *                     <button type=”button” class=”btn btn-default” data-dismiss=”modal”>Close</button>
     *                 </div>
     *             </div>
     *         </div>
     *     </div>
     * </pre>
     *
     * @return String as a HTML fragment
     */
    String toHtmlAsModalWindow() {
        StringBuilder sb = new StringBuilder()
        sb.append('<div id="' + this.hashCode() + '" class="modal fade">' + "\n")
        sb.append('  <div class="modal-dialog modal-lg" role="document">' + "\n")
        sb.append('    <div class="modal-content">' + "\n")
        sb.append('      <div class="modal-header">' + "\n")
        sb.append('        <p class="modal-title" id="')
        sb.append(this.hashCode() + 'title')
        sb.append('">')
        sb.append(this.getIdentifier())
        sb.append('</p>' + "\n")
        sb.append('      </div>' + "\n")
        sb.append('      <div class="modal-body">' + "\n")
        sb.append('        ' + this.markupInModalWindow() + "\n")
        sb.append('      </div>' + "\n")
        sb.append('      <div class="modal-footer">' + "\n")
        sb.append('        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' + "\n")
        sb.append('      </div>' + "\n")
        sb.append('    </div>' + "\n")
        sb.append('  </div>' + "\n")
        sb.append('</div>' + "\n")
        return sb.toString()
    }

    /**
     * returns the identifier of the Material which is used as
     * - the name in the Bootstrap Treeview
     * - the title of Modal window
     *
     * @return
     */
    String getIdentifier() {
        StringBuilder sb = new StringBuilder()
        String urlStr = url_.toString()
        sb.append(urlStr)
        if (suffix_ != Suffix.NULL) {
            sb.append(' ')
            sb.append(MAGIC_DELIMITER)
            sb.append(suffix_.getValue())
        }
        if (!urlStr.endsWith(fileType_.getExtension())) {
            sb.append(' ')
            sb.append(fileType_.name())
        }
        return sb.toString()
    }

    String markupInModalWindow() {
        StringBuilder sb = new StringBuilder()
        switch (fileType_) {
            case FileType.BMP:
            case FileType.GIF:
            case FileType.JPG:
            case FileType.JPEG:
            case FileType.PNG:
                sb.append('        <img src="' + this.getHrefRelativeToRepositoryRoot() +
                    '" class="img-fluid" alt="material"></img>' + "\n")
                break
            case FileType.CSV:
                def content = this.getMaterialFilePath().toFile().getText('UTF-8')
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.TXT:
                sb.append('<div style="height:350px;overflow:auto;">' + "\n")
                def content = this.getMaterialFilePath().toFile().getText('UTF-8')
                def file = this.getMaterialFilePath().toFile()
                file.readLines('UTF-8').each { line ->
                    sb.append('<p>')
                    sb.append(escapeHtml(line))
                    sb.append('</p>' + "\n")
                }
                sb.append('</div>' + "\n")
                break
            case FileType.JSON:
                def content = this.getMaterialFilePath().toFile().getText('UTF-8')
                content = JsonOutput.prettyPrint(content)
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.XML:
                def content = this.getMaterialFilePath().toFile().getText('UTF-8')
                content = XmlUtil.serialize(content)
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.PDF:
                sb.append('        <div class="embed-responsive" style="padding-bottom:150%">' + "\n")
                sb.append('            <object data="')
                sb.append(this.getHrefRelativeToRepositoryRoot())
                sb.append('" type="application/pdf" width="100%" height="100%"></object>' + "\n")
                sb.append('        </div>' + "\n")
                sb.append('        <div><a href="' + this.getHrefRelativeToRepositoryRoot() + '">')
                sb.append(this.getPathRelativeToRepositoryRoot())
                sb.append('</a></div>')
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                sb.append('        <a class="btn btn-primary btn-lg" target="_blank" href="')
                sb.append(this.getHrefRelativeToRepositoryRoot())
                sb.append('">')
                sb.append('Download')
                sb.append('</a>')
                break
            default:
                def msg = "fileType_  '${fileType_}' is unexpected"
                logger_.warn('#markupInModalWindow ' + msg)
                sb.append("        <p>${msg}</p>")
        }
        return sb.toString()
    }


    /**
     * Escape HTML angle brackets in the given string. For example.,
     * & --- &amp;
     * < --- &lt;
     * > --- &gt;
     * " --- &quot;
     *   --- &nbsp;
     * © --- &copy;
     *
     * @param str
     * @return
     */
    static String escapeHtml(String str) {
        StringBuilder sb = new StringBuilder();
        char[] charArray = str.toCharArray();
        for (char ch : charArray) {
            switch (ch) {
                case '&': sb.append('&amp;'); break;
                case '<': sb.append('&lt;'); break;
                case '>': sb.append('&gt;'); break;
                case '"': sb.append('&quot;'); break;
                case ' ': sb.append('&nbsp;'); break;
                case '©': sb.append('&copy;'); break;
                default : sb.append(ch); break;
            }
        }
        return sb.toString();
    }
}
