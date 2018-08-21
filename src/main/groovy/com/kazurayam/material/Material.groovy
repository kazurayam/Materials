package com.kazurayam.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
    private Path subpath_
    private Path path_
    private LocalDateTime lastModified_

    Material(Path subpath, URL url, Suffix suffix, FileType fileType) {
        url_ = url
        suffix_ = (suffix == null) ? Suffix.NULL : suffix
        fileType_ = fileType
        subpath_ = subpath
        path_ = subpath.resolve(MaterialFileName.format(url_, suffix_, fileType_)).normalize() // this value will be overriden by setParent()
    }

    /**
     *
     * @param path
     */
    Material(Path fullPath) {
        path_ = fullPath.normalize()
        MaterialFileName mfn = new MaterialFileName(path_.getFileName().toString())
        fileType_ = mfn.getFileType()  // FileType.UNSUPPORTED or other
        suffix_   = mfn.getSuffix()    // Suffix.NULL or other
        url_      = mfn.getURL()       // null or other
    }

    Material setParent(TCaseResult parent) {
        parent_ = parent
        if (subpath_ != null && url_ != null && suffix_ != null && fileType_ != null) {
            path_ = parent_.getTCaseDirectory().resolve(subpath_).resolve(MaterialFileName.format(url_, suffix_, fileType_)).normalize()
        }
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

    Path getPath() {
        return path_
    }

    Path getSubpath() {
        if (parent_.getTCaseDirectory() == path_.getParent()) {
            return Paths.get('.')
        } else {
            return parent_.getTCaseDirectory().relativize(path_.getParent()).normalize()
        }
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
    Path getPathRelativeToTSuiteTimestamp() {
        Path tSuiteTimestampDir = this.getParent().getParent().getTSuiteTimestampDirectory()
        Path path = getPathRelativeTo(tSuiteTimestampDir)
        logger_.debug("#getPathRelativeToTSuiteTimestamp path=${path}")
        return path
    }

    Path getPathRelativeToRepositoryRoot() {
        Path rootDir            = this.getParent().getParent().getParent().getBaseDir()
        return getPathRelativeTo(rootDir)
    }

    private Path getPathRelativeTo(Path base) {
        Path path =  base.relativize(this.getPath())
        logger_.debug("#getPathRelativeTo base=${base} this.getPath()=${this.getPath()} path=${path}")
        return path
    }

    // --------------------------------------

    String getHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getHrefRelativeTo(rootDir)
    }

    private String getHrefRelativeTo(Path base) {
        Path href = base.relativize(this.getPath())
        return href.normalize().toString().replace('\\', '/')
    }

    // ---------------------------------------------

    String getEncodedHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getEncodedHrefRelativeTo(rootDir)
    }

    private String getEncodedHrefRelativeTo(Path base) {
        String baseUri = base.toUri()
        String mateUri = this.getPath().toUri()
        String result = mateUri.substring(baseUri.length())
        logger_.debug("#getEncodedHrefRelativeTo baseUri=${baseUri} mateUri=${mateUri} result={result}")
        return result
    }


    // ---------------- helpers -----------------------------------------------


    // ---------------- overriding java.lang.Object properties --------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Material)) { return false }
        Material other = (Material)obj
        return this.getPath() == other.getPath()
    }

    @Override
    int hashCode() {
        return this.getPath().hashCode()
    }

    @Override
    int compareTo(Material other) {
        return this.getPath().compareTo(other.getPath())
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
        sb.append('"fileType":"' + Helpers.escapeAsJsonText(fileType_.toString()) + '",')
        sb.append('"path":"' + Helpers.escapeAsJsonText(this.getPath().toString()) + '",')
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
     *                     <img src="TS1/TS1/20180624_043621/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
     *                         class="img-fluid" style="border: 1px solid #ddd" alt="material">
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
        sb.append('        <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>' + "\n")
        def ar = this.anchorToReport()
        if (ar != null) {
            sb.append('        ' + ar + "\n")
        }
        sb.append('      </div>' + "\n")
        sb.append('    </div>' + "\n")
        sb.append('  </div>' + "\n")
        sb.append('</div>' + "\n")
        return sb.toString()
    }

    String anchorToReport() {
        String reportHref = this.hrefToReport()
        if (reportHref != null) {
            Path p = this.getParent().getParent().getRepositoryRoot().getBaseDir().resolve(reportHref)
            if (Files.exists(p)) {
                StringBuilder sb = new StringBuilder()
                sb.append('<a href="')
                sb.append(reportHref)
                sb.append('" class="btn btn-default" role="button" target="_blank">Report</a>')
                return sb.toString()
            } else {
                logger_.debug("#anchorToReport ${p} does not exist")
                return null
            }
        } else {
            logger_.debug("#anchorToReport this.hrefToReport() returned null")
            return null
        }
    }

    String hrefToReport() {
        TSuiteResult tsr = this.getParent().getParent()
        if (tsr != null) {
            StringBuilder sb = new StringBuilder()
            sb.append('../Reports/')
            sb.append(tsr.getTSuiteName().getValue().replace('.', '/'))
            sb.append('/')
            sb.append(tsr.getTSuiteTimestamp().format())
            sb.append('/Report.html')
        return sb.toString()
        } else {
            return null
        }
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
            Path subpath = parent_.getTCaseDirectory().relativize(this.getPath())
            sb.append(subpath.toString())
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
                sb.append('<img src="' + this.getEncodedHrefRelativeToRepositoryRoot() +
                    '" class="img-fluid" style="border: 1px solid #ddd" alt="material"></img>')
                break
            case FileType.CSV:
                def content = this.getPath().toFile().getText('UTF-8')
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.TXT:
                sb.append('<div style="height:350px;overflow:auto;">')
                def content = this.getPath().toFile().getText('UTF-8')
                def file = this.getPath().toFile()
                file.readLines('UTF-8').each { line ->
                    sb.append('<p>')
                    sb.append(escapeHtml(line))
                    sb.append('</p>' + "\n")
                }
                sb.append('</div>' + "\n")
                break
            case FileType.JSON:
                def content = this.getPath().toFile().getText('UTF-8')
                content = JsonOutput.prettyPrint(content)
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.XML:
                def content = this.getPath().toFile().getText('UTF-8')
                content = XmlUtil.serialize(content)
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.PDF:
                sb.append('<div class="embed-responsive embed-responsive-16by9" style="padding-bottom:150%">' + "\n")
                sb.append('  <object class="embed-responsive-item" data="')
                sb.append(this.getEncodedHrefRelativeToRepositoryRoot())
                sb.append('" type="application/pdf" width="100%" height="100%"></object>' + "\n")
                sb.append('</div>' + "\n")
                sb.append('<div><a href="' + this.getEncodedHrefRelativeToRepositoryRoot() + '">')
                sb.append(this.getPathRelativeToRepositoryRoot())
                sb.append('</a></div>')
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                sb.append('<a class="btn btn-primary btn-lg" target="_blank" href="')
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
