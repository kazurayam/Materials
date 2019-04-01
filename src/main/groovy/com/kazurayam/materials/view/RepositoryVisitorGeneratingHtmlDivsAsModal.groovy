package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.imagedifference.ComparisonResult
import com.kazurayam.materials.imagedifference.ComparisonResultBundle
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.resolution.PathResolutionLog
import com.kazurayam.materials.resolution.PathResolutionLogBundle

import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

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
    
    private MarkupBuilder builder
    private ComparisonResultBundle comparisonResultBundle
    
    RepositoryVisitorGeneratingHtmlDivsAsModal(MarkupBuilder builder) {
        this.builder = builder
        this.comparisonResultBundle = null
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
                        //
                        String originHref = this.getOriginHref(material)
						logger_.debug("#visitMaterialAction originHref of ${material.getPath()} is ${originHref}")
                        if (originHref != null) {
                            builder.a([
                                'href': originHref,
                                'class':'btn btn-link', 'role':'button'],
                                'Origin')
                        }
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
    
    private String getOriginHref(Material material) {
        TCaseResult tcr = material.getParent()
        TSuiteResult tsr = tcr.getParent()
        Path path = tsr.getTSuiteTimestampDirectory().resolve(PathResolutionLogBundle.SERIALIZED_FILE_NAME)
        if (Files.exists(path)) {
            try {
                PathResolutionLogBundle bundle = PathResolutionLogBundle.deserialize(path)
                PathResolutionLog resolution = bundle.findLastByMaterialPath(material.getPath().normalize)
                if (resolution != null) {
                    return resolution.getUrl()   // may return null
                }
            } catch (Exception e) {
                logger_.warn("failed to deserialize PathResolutionLogBundle instance from ${path}")
                return null
            }
        } else {
            return null
        }
    }
    
    def generateImgTags = { Material mate ->
        if (this.comparisonResultBundle != null &&
            this.comparisonResultBundle.containsImageDiff(mate.getPath())) {
            // This material is a diff image, so render it in Crousel format of Back > Diff > Forth
            ComparisonResult cr = comparisonResultBundle.get(mate.getPath())
            Path repoRoot = mate.getParent().getParent().getParent().getBaseDir()
            builder.div(['class':'carousel slide', 'data-ride':'carousel', 'id': "${mate.hashCode()}carousel"]) {
                builder.div(['class':'carousel-inner']) {
                    builder.div(['class':'carousel-item']) {
                        builder.div(['class':'carousel-caption d-none d-md-block']) {
                            builder.p "Back ${cr.getExpectedMaterial().getDescription() ?: ''}"
                        }
                        builder.img(['src': "${cr.getExpectedMaterial().getHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Back"])
                    }
                    builder.div(['class':'carousel-item active']) {
                        builder.div(['class':'carousel-caption d-none d-md-block']) {
                            String eval = (cr.imagesAreSimilar()) ? "Images are similar." : "Images are different." 
                            String rel = (cr.getDiffRatio() <= cr.getCriteriaPercentage()) ? '<=' : '>'
                            builder.p "${eval} diffRatio(${cr.getDiffRatio()}) ${rel} criteria(${cr.getCriteriaPercentage()})"
                        }
                        builder.img(['src': "${cr.getDiffMaterial().getHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Diff"])
                    }
                    builder.div(['class':'carousel-item']) {
                        builder.div(['class':'carousel-caption d-none d-md-block']) {
                            builder.p "Forth ${cr.getActualMaterial().getDescription() ?: ''}"
                        }
                        builder.img(['src': "${cr.getActualMaterial().getHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Forth"])
                    }
                    builder.a(['class':'carousel-control-prev',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'prev']) {
                        builder.span(['class':'carousel-control-prev-icon',
                                        'area-hidden':'true'], '')
                        builder.span(['class':'sr-only'], 'Back')
                    }
                    builder.a(['class':'carousel-control-next',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'next']) {
                        builder.span(['class':'carousel-control-next-icon',
                                        'area-hidden':'true'], '')
                        builder.span(['class':'sr-only'], 'Forth')
                    }
                }
            }
        } else {
            builder.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(),
                'class':'img-fluid', 'style':'border: 1px solid #ddd', 'alt':'material'])
        }
    }
        
    def markupInModalWindowAction = { Material mate ->
        switch (mate.getFileType()) {
            case FileType.BMP:
            case FileType.GIF:
            case FileType.JPG:
            case FileType.JPEG:
            case FileType.PNG:
                generateImgTags(mate)
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
    
    /**
     * Check if ComarisonResults.json file is there in the TCaseResult directory.
     * If found, instanciate a ComparisonResult object of thest Test Case from the file. 
     */
    @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {
        Material mate =
            tCaseResult.getMaterial(Paths.get(ComparisonResultBundle.SERIALIZED_FILE_NAME))
        if (mate != null) {
            Path baseDir = tCaseResult.getParent().getParent().getBaseDir()
            String jsonText = mate.getPath().toFile().text
            this.comparisonResultBundle = new ComparisonResultBundle(baseDir, jsonText)
        }
        return RepositoryVisitResult.SUCCESS
    }
    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {
        this.comparisonResultBundle = null
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterial(Material material) {
        visitMaterialAction(material)
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }
}
