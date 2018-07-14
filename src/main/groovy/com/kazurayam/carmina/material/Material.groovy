package com.kazurayam.carmina.material

import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.xml.XmlUtil

class Material implements Comparable<Material> {

    static Logger logger_ = LoggerFactory.getLogger(Material.class)

    private TCaseResult parent_
    private URL url_
    private Suffix suffix_
    private FileType fileType_
    private String fileName_
    private LocalDateTime lastModified_

    Material(URL url, Suffix suffix, FileType fileType) {
        url_ = url
        suffix_ = (suffix == null) ? Suffix.NULL : suffix
        fileType_ = fileType
        fileName_ = MaterialFileNameFormatter.format(url, suffix, fileType)
    }

    Material(String fileName) {
        fileName_ = fileName
        fileType_ = MaterialFileNameFormatter.parseFileNameForFileType(fileName)  // FileType.UNSUPPORTED or other
        suffix_   = MaterialFileNameFormatter.parseFileNameForSuffix(fileName)    // Suffix.NULL or other
        url_      = MaterialFileNameFormatter.parseFileNameForURL(fileName)       // null or other
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

    String getFileNameBody() {
        return MaterialFileNameFormatter.parseFileNameForBody(this.getFileName())
    }

    String getFileName() {
        return fileName_
    }

    //
    Material setLastModified(long lastModified) {
        Instant instant = Instant.ofEpochMilli(lastModified)
        this.setLastModified(instant)
        return this
    }

    Material setLastModified(Instant lastModified) {
        lastModified_ = LocalDateTime.ofInstant(lastModified, ZoneOffset.UTC);
        return this
    }

    LocalDateTime getLastModified() {
        return lastModified_
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
            String fileName = fileName_ ?: MaterialFileNameFormatter.format(url_, suffix_, fileType_)
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
     *
    String getHrefRelativeToTSuiteTimestamp() {
        Path timestampDir = this.getParent().getParent().getTSuiteTimestampDirectory()
        return this.getHrefRelativeTo(timestampDir)
    }
     */

    //
    String getHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getHrefRelativeTo(rootDir)
    }
    
    private String getHrefRelativeTo(Path base) {
        String fileName
        if (url_ != null) {
            fileName = MaterialFileNameFormatter.format(url_, suffix_, fileType_)
        } else {
            fileName = fileName_
        }
        Path tCaseResultRelativeToTSuiteTimestamp = base.relativize(
                this.getParent().getTCaseDirectory())
        Path href = tCaseResultRelativeToTSuiteTimestamp.resolve(fileName)
        return href.normalize().toString().replace('\\', '/')
    }

    //
    String getEncodedHrefRelativeTo(Path base) {
        String encodedFileName
        if (url_ != null) {
            encodedFileName = MaterialFileNameFormatter.formatEncoded(url_, suffix_, fileType_)
        } else {
            encodedFileName = fileName_
        }
        Path tCaseResultRelativeToTSuiteTimestamp = base.relativize(
            this.getParent().getTCaseDirectory())
        Path href = tCaseResultRelativeToTSuiteTimestamp.resolve(encodedFileName)
        return href.normalize().toString().replace('\\', '/')
    }
    
    String getEncodedHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getEncodedHrefRelativeTo(rootDir)
    }


    // ---------------- helpers -----------------------------------------------


    // ---------------- overriding java.lang.Object properties --------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Material)) { return false }
        Material other = (Material)obj
        if (this.getMaterialFilePath() != null && other.getMaterialFilePath() != null) {
            return this.getMaterialFilePath() == other.getMaterialFilePath()
        } else {
            return this.getFileName() == other.getFileName()
        }
    }

    @Override
    int hashCode() {
        if (this.getMaterialFilePath() != null) {
            return this.getMaterialFilePath().hashCode()
        } else {
            return this.getFileName().hashCode()
        }
    }

    @Override
    int compareTo(Material other) {
        if (this.getMaterialFilePath() != null && other.getMaterialFilePath() != null) {
            return this.getMaterialFilePath().compareTo(other.getMaterialFilePath())
        } else {
            return this.getFileName().compareTo(other.getFileName())
        }
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
        sb.append('"fileType":"' + Helpers.escapeAsJsonText(fileType_.toString()) + '",')
        sb.append('"lastModified":"' + lastModified_.toString() + '"')
        sb.append('}}')
        return sb.toString()
    }

    String toBootstrapTreeviewData() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"text":"' + Helpers.escapeAsJsonText(this.getIdentifier())+ '",')
        sb.append('"selectable":true,')
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
        if (url_ != null) {
            String urlStr = url_.toString()
            sb.append(urlStr)
            if (suffix_ != Suffix.NULL) {
                sb.append(' ')
                sb.append(suffix_.toString())
            }
            if (!urlStr.endsWith(fileType_.getExtension())) {
                sb.append(' ')
                sb.append(fileType_.name())
            }
        } else {
            sb.append(this.getFileName())
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
                sb.append('        <img src="' + this.getEncodedHrefRelativeToRepositoryRoot() +
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
                sb.append('        <div class="embed-responsive embed-responsive-16by9" style="padding-bottom:150%">' + "\n")
                sb.append('            <object class="embed-responsive-item" data="')
                sb.append(this.getEncodedHrefRelativeToRepositoryRoot())
                sb.append('" type="application/pdf" width="100%" height="100%"></object>' + "\n")
                sb.append('        </div>' + "\n")
                sb.append('        <div><a href="' + this.getEncodedHrefRelativeToRepositoryRoot() + '">')
                sb.append(this.getPathRelativeToRepositoryRoot())
                sb.append('</a></div>')
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                sb.append('        <a class="btn btn-primary btn-lg" target="_blank" href="')
                sb.append(this.getEncodedHrefRelativeToRepositoryRoot())
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
