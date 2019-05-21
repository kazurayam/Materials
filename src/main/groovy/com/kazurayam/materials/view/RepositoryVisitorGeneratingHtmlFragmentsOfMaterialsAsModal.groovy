package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitorSimpleImpl

import groovy.json.JsonOutput
import groovy.xml.XmlUtil

/**
 *
 * @author kazurayam
 *
 */
class RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal
        extends RepositoryVisitorSimpleImpl implements RepositoryVisitor {
    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal.class)
    RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal(Writer writer) {
        super(writer)
    }
    @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {}
    @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {}
    @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {}
    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {}

    @Override RepositoryVisitResult visitMaterial(Material material) {
        StringBuilder sb = new StringBuilder()
        sb.append('<div id="' + material.hashCode() + '" class="modal fade">' + "\n")
        sb.append('  <div class="modal-dialog modal-lg" role="document">' + "\n")
        sb.append('    <div class="modal-content">' + "\n")
        sb.append('      <div class="modal-header">' + "\n")
        sb.append('        <p class="modal-title" id="')
        sb.append(material.hashCode() + 'title')
        sb.append('">')
        sb.append(material.getIdentifier())
        sb.append('</p>' + "\n")
        sb.append('      </div>' + "\n")
        sb.append('      <div class="modal-body">' + "\n")
        sb.append('        ' + markupInModalWindow(material) + "\n")
        sb.append('      </div>' + "\n")
        sb.append('      <div class="modal-footer">' + "\n")
        sb.append('        <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>' + "\n")
        def ar = anchorToReport(material)
        if (ar != null) {
            sb.append('        ' + ar + "\n")
        }
        sb.append('      </div>' + "\n")
        sb.append('    </div>' + "\n")
        sb.append('  </div>' + "\n")
        sb.append('</div>' + "\n")
        pw_.print(sb.toString())
        pw_.flush()
        return RepositoryVisitResult.SUCCESS
    }

    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }

    /**
     *
     * @return
     */
    String markupInModalWindow(Material mate) {
        StringBuilder sb = new StringBuilder()
        switch (mate.getFileType()) {
            case FileType.BMP:
            case FileType.GIF:
            case FileType.JPG:
            case FileType.JPEG:
            case FileType.PNG:
                sb.append('<img src="' + mate.getEncodedHrefRelativeToRepositoryRoot() +
                    '" class="img-fluid" style="border: 1px solid #ddd" alt="material"></img>')
                break
            case FileType.CSV:
                def content = mate.getPath().toFile().getText('UTF-8')
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.TXT:
                sb.append('<div style="height:350px;overflow:auto;">')
                def content = mate.getPath().toFile().getText('UTF-8')
                def file = mate.getPath().toFile()
                file.readLines('UTF-8').each { line ->
                    sb.append('<p>')
                    sb.append(escapeHtml(line))
                    sb.append('</p>' + "\n")
                }
                sb.append('</div>' + "\n")
                break
            case FileType.JSON:
                def content = mate.getPath().toFile().getText('UTF-8')
                content = JsonOutput.prettyPrint(content)
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.XML:
                def content = mate.getPath().toFile().getText('UTF-8')
                content = XmlUtil.serialize(content)
                sb.append('<pre class="pre-scrollable"><code>')
                sb.append(escapeHtml(content))
                sb.append('</code></pre>')
                break
            case FileType.PDF:
                sb.append('<div class="embed-responsive embed-responsive-16by9" style="padding-bottom:150%">' + "\n")
                sb.append('  <object class="embed-responsive-item" data="')
                sb.append(mate.getEncodedHrefRelativeToRepositoryRoot())
                sb.append('" type="application/pdf" width="100%" height="100%"></object>' + "\n")
                sb.append('</div>' + "\n")
                sb.append('<div><a href="' + mate.getEncodedHrefRelativeToRepositoryRoot() + '">')
                sb.append(mate.getPathRelativeToRepositoryRoot())
                sb.append('</a></div>')
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                sb.append('<a class="btn btn-primary btn-lg" target="_blank" href="')
                sb.append(mate.getEncodedHrefRelativeToRepositoryRoot())
                sb.append('">')
                sb.append('Download')
                sb.append('</a>')
                break
            default:
                def msg = "this.getFileType()='${mate.getFileType()}' is unexpected"
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

    String anchorToReport(Material mate) {
        String reportHref = mate.getHrefToReport()
        if (reportHref != null) {
            Path p = mate.getParent().getParent().getRepositoryRoot().getBaseDir().resolve(reportHref)
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

}
