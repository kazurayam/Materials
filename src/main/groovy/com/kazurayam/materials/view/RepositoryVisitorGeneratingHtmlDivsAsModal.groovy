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
    
    private MarkupBuilder builder_
    private ComparisonResultBundle comparisonResultBundle_
    private PathResolutionLogBundleCache pathResolutionLogBundleCache_
    
    RepositoryVisitorGeneratingHtmlDivsAsModal(MarkupBuilder builder) {
        this.builder_ = builder
        this.comparisonResultBundle_ = null
        this.pathResolutionLogBundleCache_ = new PathResolutionLogBundleCache()
    }
    
    def preVisitRepositoryRootAction = {
        builder_.mkp.comment "here is inserted the output of ${classShortName}"
    }
    
    def postVisitRepositoryRootAction = {
        builder_.mkp.comment "end of the output of ${classShortName}"
    }
    
    def visitMaterialAction = { Material material ->
        Objects.requireNonNull(material, "material must not be null")
        builder_.div(['id': material.hashCode(), 'class':'modal fade']) {
            builder_.div(['class':'modal-dialog modal-lg', 'role':'document']) {
                builder_.div(['class':'modal-content']) {
                    builder_.div(['class':'modal-header']) {
                        builder_.p(['class':'modal-title', 'id': material.hashCode() + 'title'], material.getIdentifier())
                        //
                        this.generateAnchorsToOrigins(builder_, material)
                    }
                    builder_.div(['class':'modal-body']) {
                        markupInModalWindowAction(material)
                    }
                    builder_.div(['class':'modal-footer']) {
                        builder_.button(['type':'button', 'class':'btn btn-primary',
                            'data-dismiss':'modal'], 'Close')
                        anchorToReport(material)
                    }
                }
            }
        }
    }
    
    private void generateAnchorsToOrigins(MarkupBuilder builder, Material material) {
        String originHref = this.getImmediateOriginHref(material)
        if (originHref != null) {
            builder.a([
                'href': originHref,
                'class':'btn btn-link', 'role':'button'],
                'Origin')
        }
        //
        if (this.comparisonResultBundle_ != null) {
            ComparisonResult cr = this.comparisonResultBundle_.getByDiffMaterial(material.getHrefRelativeToRepositoryRoot())
            if (cr != null) {
                builder.a([
                    'href': cr.getExpectedMaterial().getHrefRelativeToRepositoryRoot(),
                    'class':'btn btn-link', 'role':'button'],
                    'Back origin')
                builder.a([
                    'href': cr.getActualMaterial().getHrefRelativeToRepositoryRoot(),
                    'class':'btn btn-link', 'role':'button'],
                    'Forth origin')
            }
        } else {
            logger_.warn("#generateAnchorsToOrigins this.comparisonResultBundle_ is found to be null")
        }
    }

    private String getImmediateOriginHref(Material material) {
        TCaseResult tcr = material.getParent()
        TSuiteResult tsr = tcr.getParent()
        Path path = tsr.getTSuiteTimestampDirectory().resolve(PathResolutionLogBundle.SERIALIZED_FILE_NAME)
        if (Files.exists(path)) {
            PathResolutionLogBundle bundle = pathResolutionLogBundleCache_.get(path)
            if (bundle == null) {
                // failed loading path-resolution-log-bundle.json of this material
                return null
            }
            PathResolutionLog resolution = bundle.findLastByMaterialPath(material.getHrefRelativeToRepositoryRoot())
            if (resolution != null) {
                String result = resolution.getUrl()   // getUrl() may return null
                logger_.debug("#getImmediateOriginHref returning ${result}")
                return result
            } else {
                logger_.warn("#getImmediateOriginHref could not find a PathResolutionLog entry of ${material.getHrefRelativeToRepositoryRoot()} in the bundle at ${path}")
                logger_.debug("#getImmediateOriginHref bundle=${JsonOutput.prettyPrint(bundle.toString())}")
                return null
            }
        } else {
            logger_.warn("#getImmediateOriginHref ${path} does not exist")
            return null
        }
    }
    
    def generateImgTags = { Material mate ->
        if (this.comparisonResultBundle_ != null &&
            this.comparisonResultBundle_.containsImageDiff(mate.getPath())) {
            // This material is a diff image, so render it in Carousel format of Back > Diff > Forth
            ComparisonResult cr = comparisonResultBundle_.get(mate.getPath())
            Path repoRoot = mate.getParent().getParent().getParent().getBaseDir()
            builder_.div(['class':'carousel slide', 'data-ride':'carousel', 'id': "${mate.hashCode()}carousel"]) {
                builder_.div(['class':'carousel-inner']) {
                    builder_.div(['class':'carousel-item']) {
                        builder_.div(['class':'carousel-caption d-none d-md-block']) {
                            builder_.p "Back ${cr.getExpectedMaterial().getDescription() ?: ''}"
                        }
                        builder_.img(['src': "${cr.getExpectedMaterial().getHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Back"])
                    }
                    builder_.div(['class':'carousel-item active']) {
                        builder_.div(['class':'carousel-caption d-none d-md-block']) {
                            String eval = (cr.imagesAreSimilar()) ? "Images are similar." : "Images are different." 
                            String rel = (cr.getDiffRatio() <= cr.getCriteriaPercentage()) ? '<=' : '>'
                            builder_.p "${eval} diffRatio(${cr.getDiffRatio()}) ${rel} criteria(${cr.getCriteriaPercentage()})"
                        }
                        builder_.img(['src': "${cr.getDiffMaterial().getHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Diff"])
                    }
                    builder_.div(['class':'carousel-item']) {
                        builder_.div(['class':'carousel-caption d-none d-md-block']) {
                            builder_.p "Forth ${cr.getActualMaterial().getDescription() ?: ''}"
                        }
                        builder_.img(['src': "${cr.getActualMaterial().getHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Forth"])
                    }
                    builder_.a(['class':'carousel-control-prev',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'prev']) {
                        builder_.span(['class':'carousel-control-prev-icon',
                                        'area-hidden':'true'], '')
                        builder_.span(['class':'sr-only'], 'Back')
                    }
                    builder_.a(['class':'carousel-control-next',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'next']) {
                        builder_.span(['class':'carousel-control-next-icon',
                                        'area-hidden':'true'], '')
                        builder_.span(['class':'sr-only'], 'Forth')
                    }
                }
            }
        } else {
            builder_.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(),
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
                builder_.pre(['class':'pre-scrollable'], mate.getPath().toFile().getText('UTF-8'))
                break
            case FileType.TXT:
                builder_.div(['style':'height:350px;overflow:auto;']) {
                    File file = mate.getPath().toFile()
                    file.readLines('UTF-8').each { line ->
                        builder_.p line
                    }
                }
                break
            case FileType.JSON:
                def content = mate.getPath().toFile().getText('UTF-8')
                def pp = JsonOutput.prettyPrint(content)
                builder_.pre(['class':'pre-scrollable'], pp)
                break
            case FileType.XML:
                def content = mate.getPath().toFile().getText('UTF-8')
                content = XmlUtil.serialize(content)
                builder_.pre(['class':'pre-scrollable'], content)
                break
            case FileType.PDF:
                builder_.div(['class':'embed-responsive embed-responsive-16by9', 'style':'padding-bottom:150%']) {
                    builder_.object(['class':'embed-responsive-item', 'data':mate.getEncodedHrefRelativeToRepositoryRoot(),
                                    'type':'application/pdf', 'width':'100%', 'height':'100%'],'')
                    builder_.div {
                        builder_.a(['href': mate.getEncodedHrefRelativeToRepositoryRoot() ],
                            mate.getPathRelativeToRepositoryRoot())
                    }
                }
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                builder_.a(['class':'btn btn-primary btn-g', 'target':'_blank',
                            'href': mate.getEncodedHrefRelativeToRepositoryRoot()], 'Download')
                break
            default:
                def msg = "this.getFileType()='${mate.getFileType()}' is unexpected"
                logger_.warn('markupInModalWindow' + msg)
                builder_.p msg
        }
    }
    
    def anchorToReport = { Material mate ->
        String reportHref = mate.getHrefToReport()
        if (reportHref != null) {
            Path p = mate.getParent().getParent().getRepositoryRoot().getBaseDir().resolve(reportHref)
            if (Files.exists(p)) {
                builder_.a(['href':reportHref, 'class':'btn btn-default', 'role':'button',
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
     * Check if comarison-result-bundle.json file is there in the TCaseResult directory.
     * If found, instanciate a ComparisonResultBundle object of the TestCase from the file. 
     */
    @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {
        Material mate =
            tCaseResult.getMaterial(Paths.get(ComparisonResultBundle.SERIALIZED_FILE_NAME))
        if (mate != null) {
            Path baseDir = tCaseResult.getParent().getParent().getBaseDir()
            String jsonText = mate.getPath().toFile().text
            this.comparisonResultBundle_ = new ComparisonResultBundle(baseDir, jsonText)
        }
        return RepositoryVisitResult.SUCCESS
    }

    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {
        this.comparisonResultBundle_ = null
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterial(Material material) {
        visitMaterialAction(material)
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }
    
    
    
    /**
     * 
     * @author kazurayam
     *
     */
    static class PathResolutionLogBundleCache {
        
        private Map<Path, PathResolutionLogBundle> cache_
        
        PathResolutionLogBundleCache() {
            cache_ = new HashMap<Path, PathResolutionLogBundle>()
        }
        
        PathResolutionLogBundle get(Path bundleFile) {
            if (cache_.containsKey(bundleFile)) {
                return cache_.get(bundleFile)
            } else {
                PathResolutionLogBundle bundle
                try {
                    bundle = PathResolutionLogBundle.deserialize(bundleFile)
                    cache_.put(bundleFile, bundle)
                } catch (Exception e) {
                    logger_.warn("#PathResolutionLogBundleCache#get failed to deserialize PathResolutionLogBundle instance from ${bundleFile}")
                    return null
                }
                return bundle
            }
        }
    }
}
