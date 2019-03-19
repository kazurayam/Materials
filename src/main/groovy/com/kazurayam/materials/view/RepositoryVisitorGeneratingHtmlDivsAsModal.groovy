package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitResult

import groovy.json.JsonOutput
import groovy.xml.XmlUtil
import groovy.xml.MarkupBuilder

/**
 * This class is a Closure, which works as a part of Closure 
 * in the generate method of 
 * com.kazurayam.materials.view.BaseIndexer#generate().
 * 
 * @author kazurayam
 */
class RepositoryVisitorGeneratingHtmlDivsAsModal 
                    implements RepositoryVisitor {
                       
    static Logger logger_ = LoggerFactory.getLogger(
                            RepositoryVisitorGeneratingHtmlDivsAsModal.class)
    
    static final String classShortName = Helpers.getClassShortName(
                            RepositoryVisitorGeneratingHtmlDivsAsModal.class)
    
    MarkupBuilder builder
    
    RepositoryVisitorGeneratingHtmlDivsAsModal(MarkupBuilder builder) {
        this.builder = builder
    }
    
    def preVisitRepositoryRootAction = {
        builder.mkp.comment "here is inserted the output of ${classShortName}"
    }
    
    def postVisitRepositoryRootAction = {
        builder.mkp.comment "end of the output of ${classShortName}"
    }
    
    def visitMaterialAction = { Material material ->
        Objects.requireNonNull(material, "material must not be null")
        builder.div(['id': material.hashCode(), 'class':'modal fade']) {
            builder.div(['class':'modal-dialog modal-lg', 'role':'document']) {
                builder.div(['class':'modal-content']) {
                    builder.div(['class':'modal-header']) {
                        builder.p(['class':'modal-title', 'id': material.hashCode() + 'title'], material.getIdentifier())
                    }
                    builder.div(['class':'modal-body']) {
                        markupInModalWindowAction(material)
                    }
                    builder.div(['class':'modal-footer']) {
                        builder.button(['type':'button', 'class':'btn btn-primary',
                                        'data-dismiss':'modal'], 'Close')
                        anchorToReport(material)
                    }
                }
            }
        }
    }
    
    def markupInModalWindowAction = { Material mate ->
        switch (mate.getFileType()) {
            case FileType.BMP:
            case FileType.GIF:
            case FileType.JPG:
            case FileType.JPEG:
            case FileType.PNG:
                builder.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(), 'class':'img-fluid',
                    'style':'border: 1px solid #ddd', 'alt':'material'])
                break
            case FileType.CSV:
                builder.pre(['class':'pre-scrollable'], mate.getPath().toFile().getText('UTF-8'))
                break
            case FileType.TXT:
                builder.div(['style':'height:350px;overflow:auto;']) {
                    File file = mate.getPath().toFile()
                    file.readLines('UTF-8').each { line ->
                        builder.p line
                    }
                }
                break
            case FileType.JSON:
                def content = mate.getPath().toFile().getText('UTF-8')
                def pp = JsonOutput.prettyPrint(content)
                builder.pre(['class':'pre-scrollable'], pp)
                break
            case FileType.XML:
                def content = mate.getPath().toFile().getText('UTF-8')
                content = XmlUtil.serialize(content)
                builder.pre(['class':'pre-scrollable'], content)
                break
            case FileType.PDF:
                builder.div(['class':'embed-responsive embed-responsive-16by9', 'style':'padding-bottom:150%']) {
                    builder.object(['class':'embed-responsive-item', 'data':mate.getEncodedHrefRelativeToRepositoryRoot(),
                                    'type':'application/pdf', 'width':'100%', 'height':'100%'],'')
                    builder.div {
                        builder.a(['href': mate.getEncodedHrefRelativeToRepositoryRoot() ],
                            mate.getPathRelativeToRepositoryRoot())
                    }
                }
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                builder.a(['class':'btn btn-primary btn-g', 'target':'_blank',
                            'href': mate.getEncodedHrefRelativeToRepositoryRoot()], 'Download')
                break
            default:
                def msg = "this.getFileType()='${mate.getFileType()}' is unexpected"
                logger_.warn('markupInModalWindow' + msg)
                builder.p msg
        }    
    }
    
    def anchorToReport = { Material mate ->
        String reportHref = mate.getHrefToReport()
        if (reportHref != null) {
            Path p = mate.getParent().getParent().getRepositoryRoot().getBaseDir().resolve(reportHref)
            if (Files.exists(p)) {
                builder.a(['href':reportHref, 'class':'btn btn-default', 'role':'button',
                            'target':'_blank'], 'Report')
            } else {
                logger_.debug("#anchorToReport ${p} does not exist")
                return null
            }
        } else {
            logger_.debug("#anchorToReport this.hrefToReport(mate) return null for ${mate.toString()}")
            return null
        }
    }
    
    /**
     * returns a URL string for the Report.html
     *  
     * href="../Reports/main.TS1/20190321_103759/Report.html"
     *
    def hrefToReport = { Material mate ->
        return mate.hrefToReport()
    }
    */
                  
    /*
     * implementing methods required by RepositoryVisitor
     */
    @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
        preVisitRepositoryRootAction()
        return RepositoryVisitResult.SUCCESS
    }
    @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
        postVisitRepositoryRootAction()
        return RepositoryVisitResult.SUCCESS
    }
    @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {}
    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {}
    
    @Override RepositoryVisitResult visitMaterial(Material material) {
        visitMaterialAction(material)
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }
}
