package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.imagedifference.ComparisonResult
import com.kazurayam.materials.imagedifference.ComparisonResultBundle
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.resolution.PathResolutionLog
import com.kazurayam.materials.resolution.PathResolutionLogBundle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

/**
 * This class is called by 
 * com.kazurayam.materials.view.BaseIndexer#generate().
 * 
 * @author kazurayam
 */
class RepositoryVisitorGeneratingHtmlDivsAsModal 
                    implements RepositoryVisitor {
                       
    protected MarkupBuilder mkbuilder
    protected ReportsAccessor reportsAccessor
    
    protected static Logger logger_ = LoggerFactory.getLogger(
                            RepositoryVisitorGeneratingHtmlDivsAsModal.class)
    
    protected VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    protected String classShortName = Helpers.getClassShortName(
                            RepositoryVisitorGeneratingHtmlDivsAsModal.class)
    
    private ComparisonResultBundle comparisonResultBundle_
    private PathResolutionLogBundleCache pathResolutionLogBundleCache_
    
    /*
     * class name which determins the width of Bootstrap Modal window
     */
    protected String bootstrapModalSize = 'modal-lg'  // 'modal-sm', '', 'modal-lg', 'modal-xl'
    
    RepositoryVisitorGeneratingHtmlDivsAsModal(MarkupBuilder mkbuilder) {
        Objects.requireNonNull(mkbuilder, "mkbuilder must not be null")
        this.mkbuilder = mkbuilder
        this.comparisonResultBundle_ = null
        this.pathResolutionLogBundleCache_ = new PathResolutionLogBundleCache()
    }
    
    void setReportsAccessor(ReportsAccessor reportsAccessor) {
        this.reportsAccessor = reportsAccessor    
    }
    
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
        this.pathResolutionLogBundleCache_.setVisualTestingLogger(vtLogger_)
    }
    
    /*
     * implementing methods required by RepositoryVisitor
     */
    @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
        mkbuilder.mkp.comment "here is inserted the output of ${classShortName}"
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
        mkbuilder.mkp.comment "end of the output of ${classShortName}"
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
            logger_.debug("#preVisitTCaseResult comparisonResultBundle_ is set to be ${comparisonResultBundle_}")
        }
        return RepositoryVisitResult.SUCCESS
    }

    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {
        this.comparisonResultBundle_ = null
        logger_.info("#postVisitTCaseResult comparisonResultBundle_ is set to be null")
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterial(Material material) {
        // Here we do the business!
        visitMaterialAction(material)
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }
    

    private void visitMaterialAction(Material material) {
        Objects.requireNonNull(material, "material must not be null")
        mkbuilder.div(['id': material.hashCode(), 'class':'modal fade']) {
            mkbuilder.div(['class':"modal-dialog ${bootstrapModalSize}", 'role':'document']) {
                mkbuilder.div(['class':'modal-content']) {
                    mkbuilder.div(['class':'modal-header']) {
                        mkbuilder.p(['class':'modal-title', 'id': material.hashCode() + 'title'], material.getIdentifier())
                    }
                    mkbuilder.div(['class':'modal-body']) {
                        this.markupInModalWindowAction(material)
                    }
                    mkbuilder.div(['class':'modal-footer']) {
                        // link to "Origin"
                        this.generateAnchorsToOrigins(mkbuilder, material)
                        // Close button
                        mkbuilder.button(['type':'button', 'class':'btn btn-primary',
                            'data-dismiss':'modal'], 'Close')
                        anchorToReport(material)
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @param mate
     */
    private void markupInModalWindowAction(Material mate) {
        switch (mate.getFileType()) {
            case FileType.BMP:
            case FileType.GIF:
            case FileType.JPG:
            case FileType.JPEG:
            case FileType.PNG:
                generateImgTags(mate)
                break
            case FileType.CSV:
                mkbuilder.pre(['class':'pre-scrollable'], mate.getPath().toFile().getText('UTF-8'))
                break
            case FileType.TXT:
                mkbuilder.div(['style':'height:350px;overflow:auto;']) {
                    File file = mate.getPath().toFile()
                    file.readLines('UTF-8').each { line ->
                        mkbuilder.p line
                    }
                }
                break
            case FileType.JSON:
                def content = mate.getPath().toFile().getText('UTF-8')
                def pp = JsonOutput.prettyPrint(content)
                mkbuilder.pre(['class':'pre-scrollable'], pp)
                break
            case FileType.XML:
                def content = mate.getPath().toFile().getText('UTF-8')
                content = XmlUtil.serialize(content)
                mkbuilder.pre(['class':'pre-scrollable'], content)
                break
            case FileType.PDF:
                mkbuilder.div(['class':'embed-responsive embed-responsive-16by9', 'style':'padding-bottom:150%']) {
                    mkbuilder.object(['class':'embed-responsive-item', 'data':mate.getEncodedHrefRelativeToRepositoryRoot(),
                                    'type':'application/pdf', 'width':'100%', 'height':'100%'],'')
                    mkbuilder.div {
                        mkbuilder.a(['href': mate.getEncodedHrefRelativeToRepositoryRoot() ],
                            mate.getPathRelativeToRepositoryRoot())
                    }
                }
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                mkbuilder.a(['class':'btn btn-primary btn-g', 'target':'_blank',
                            'href': mate.getEncodedHrefRelativeToRepositoryRoot()], 'Download')
                break
            default:
                def msg = "this.getFileType()='${mate.getFileType()}' is unexpected"
                logger_.info('markupInModalWindow' + msg)
                mkbuilder.p msg
        }
    }

    /**
     * This method is supposed to be overridden for various presentations of image differences
     */
    protected void generateImgTags(Material mate) {
        if (this.comparisonResultBundle_ != null &&
            this.comparisonResultBundle_.containsImageDiff(mate.getPath())) {
            // This material is a diff image, so render it in Carousel format of Back > Diff > Forth
            ComparisonResult cr = comparisonResultBundle_.get(mate.getPath())
            Path repoRoot = mate.getParent().getParent().getParent().getBaseDir()
            mkbuilder.div(['class':'carousel slide', 'data-ride':'carousel', 'id': "${mate.hashCode()}carousel"]) {
                mkbuilder.div(['class':'carousel-inner']) {
                    mkbuilder.div(['class':'carousel-item']) {
                        mkbuilder.div(['class':'carousel-caption d-none d-md-block']) {
                            mkbuilder.p "Back ${cr.getExpectedMaterial().getDescription() ?: ''}"
                        }
                        mkbuilder.img(['src': "${cr.getExpectedMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Back"])
                    }
                    mkbuilder.div(['class':'carousel-item active']) {
                        mkbuilder.div(['class':'carousel-caption d-none d-md-block']) {
                            String eval = (cr.imagesAreSimilar()) ? "Images are similar." : "Images are different."
                            String rel = (cr.getDiffRatio() <= cr.getCriteriaPercentage()) ? '<=' : '>'
                            mkbuilder.p "${eval} diffRatio(${cr.getDiffRatio()}) ${rel} criteria(${cr.getCriteriaPercentage()})"
                        }
                        mkbuilder.img(['src': "${cr.getDiffMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Diff"])
                    }
                    mkbuilder.div(['class':'carousel-item']) {
                        mkbuilder.div(['class':'carousel-caption d-none d-md-block']) {
                            mkbuilder.p "Forth ${cr.getActualMaterial().getDescription() ?: ''}"
                        }
                        mkbuilder.img(['src': "${cr.getActualMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Forth"])
                    }
                    mkbuilder.a(['class':'carousel-control-prev',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'prev']) {
                        mkbuilder.span(['class':'carousel-control-prev-icon',
                                        'area-hidden':'true'], '')
                        mkbuilder.span(['class':'sr-only'], 'Back')
                    }
                    mkbuilder.a(['class':'carousel-control-next',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'next']) {
                        mkbuilder.span(['class':'carousel-control-next-icon',
                                        'area-hidden':'true'], '')
                        mkbuilder.span(['class':'sr-only'], 'Forth')
                    }
                }
            }
        } else {
            mkbuilder.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(),
                'class':'img-fluid', 'style':'border: 1px solid #ddd', 'alt':'material'])
        }
    }


    /**
     * 
     * @param builder
     * @param material
     */
    private void generateAnchorsToOrigins(MarkupBuilder builder, Material material) {
        String originHref = this.getOriginHref(material)
        if (originHref != null) {
            builder.a([
                'href': originHref,
                'class':'btn btn-link', 'role':'button', 'target': '_blank'],
                'Origin')
        }
        //
        if (this.comparisonResultBundle_ != null) {
            ComparisonResult cr = this.comparisonResultBundle_.getByDiffMaterial(material.getHrefRelativeToRepositoryRoot())
            if (cr != null) {
                String expectedMaterialHref = this.getExpectedMaterialOriginHref(material.getBaseDir(), cr)
                if (expectedMaterialHref != null) {
                    builder.a([
                        'href': expectedMaterialHref,
                        'class':'btn btn-link', 'role':'button', 'target': '_blank'],
                        'Back')
                }
                String actualMaterialHref = this.getActualMaterialOriginHref(material.getBaseDir(), cr)
                if (actualMaterialHref != null) {
                    builder.a([
                        'href': actualMaterialHref,
                        'class':'btn btn-link', 'role':'button', 'target': '_blank'],
                        'Forth')
                }
            }
        } else {
            String msg = this.class.getSimpleName() + "#generateAnchorsToOrigins this.comparisonResultBundle_ is found to be null"
            logger_.warn(msg)
            //vtLogger_.info(msg)
        }
    }

    /**
     * 
     * @param material
     * @return
     */
    private String getOriginHref(Material material) {
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
                logger_.info("#getOriginHref returning ${result}")
                return result
            } else {
                String msg = this.class.getSimpleName() + "#getOriginHref could not find a PathResolutionLog entry of " + 
                            "${material.getHrefRelativeToRepositoryRoot()} in the bundle at ${path}," +
                            " bundle=${JsonOutput.prettyPrint(bundle.toString())}"
                logger_.info(msg)
                vtLogger_.info(msg)
                return null
            }
        } else {
            String msg = this.class.getSimpleName() + "#getOriginHref ${path} does not exist"
            logger_.info(msg)
            vtLogger_.info(msg)
            return null
        }
    }
    
    
    
    /**
     * 
     */
    /* A example of ComparisonResult instance is as follows:
{
 "ComparisonResult": {
     "expectedMaterial": {
         "Material": {
             ...
             "hrefRelativeToRepositoryRoot": "47news.chronos_capture/20190404_111956/47news.visitSite/top.png",
             ...
         }
     },
     "actualMaterial": {
         "Material": {
             ...
             "hrefRelativeToRepositoryRoot": "47news.chronos_capture/20190404_112053/47news.visitSite/top.png",
             ...
         }
     },
     "diffMaterial": {
         "Material": {
             "hrefRelativeToRepositoryRoot": "47news.chronos_exam/20190404_112054/47news.ImageDiff/47news.visitSite/top.20190404_111956_default-20190404_112053_default.(16.99).png"
         }
     },
     ...
 }
}
      */
    String getExpectedMaterialOriginHref(Path baseDir, ComparisonResult cr) {
        def jsonObject = new JsonSlurper().parseText(cr.toJsonText())
        return getXMaterialOriginHref(baseDir, jsonObject.ComparisonResult.expectedMaterial.Material.hrefRelativeToRepositoryRoot)
    }
    
    String getActualMaterialOriginHref(Path baseDir, ComparisonResult cr) {
        def jsonObject = new JsonSlurper().parseText(cr.toJsonText())
        return getXMaterialOriginHref(baseDir, jsonObject.ComparisonResult.actualMaterial.Material.hrefRelativeToRepositoryRoot)
    }
    
    String getXMaterialOriginHref(Path baseDir, String hrefRelativeToRepositoryRoot) {
        String[] components = hrefRelativeToRepositoryRoot.split('/')             // [ '47news.chronos_capture', '20190404_111956', '47news.visitSite', 'top.png' ]
        if (components.length > 2) {
            Path pathResolutionLogBundlePath = baseDir.resolve(components[0]).resolve(components[1]).resolve(PathResolutionLogBundle.SERIALIZED_FILE_NAME)
            if (Files.exists(pathResolutionLogBundlePath)) {
                PathResolutionLogBundle prlb = PathResolutionLogBundle.deserialize(pathResolutionLogBundlePath)
                PathResolutionLog prl = prlb.findLastByMaterialPath(hrefRelativeToRepositoryRoot)
                /*
                * An instance of PathResolutionLog is, for example:
                * <PRE>
                * {
                *   "PathResolutionLogBundle": [
                *     {
                *       "PathResolutionLog": {
                *         "MaterialPath": "47news.chronos_capture/20190404_112053/47news.visitSite/top.png",
                *         "TCaseName": "Test Cases/47news/visitSite",
                *         "InvokedMethodName": "resolveScreenshotPathByUrlPathComponents",
                *         "SubPath": "",
                *         "URL": "https://www.47news.jp/"
                *       }
                *     }
                *   ]
                * }
                * </PRE>
                * but, remember, InvokeMethodName can also be resolveMaterialPath, and in that case
                * there would not be URL property.
                */
                if (prl != null && prl.getUrl() != null) {
                    return prl.getUrl().toExternalForm()
                } else {
                    return null
                }
            } else {
                String msg = "#getXMaterialOriginHref pathResolutionLogBundlePath(${pathResolutionLogBundlePath}) does ot exist"
                logger_.warn(msg)
                vtLogger_.failed(msg)
                return null
            }
        }
    }
    
    private void anchorToReport(Material mate) {
        Path baseDir = mate.getParent().getParent().getRepositoryRoot().getBaseDir()
        String reportHref = null
        Objects.requireNonNull(reportsAccessor , this.class.getSimpleName() + "#anchorToReport reportsAccessor_ must not be null")
        reportHref = reportsAccessor.getHrefToReport(mate)
        //vtLogger_.info(this.class.getSimpleName() + "#anchorToReport baseDir=${baseDir.toString()}")
        //vtLogger_.info(this.class.getSimpleName() + "#anchorToReport reportHref=${reportHref}")
        if (reportHref != null) {
            Path p = baseDir.resolve(reportHref)
            if (Files.exists(p)) {
                mkbuilder.a(['href':reportHref, 'class':'btn btn-default', 'role':'button',
                            'target':'_blank'], 'Report')
            } else {
                String msg = this.class.getSimpleName() + "#anchorToReport file not found: ${p}"
                logger_.warn(msg)
                vtLogger_.failed(msg)
            }
        } else {
            String msg = this.class.getSimpleName() + "#anchorToReport reportsAccessor_.getHrefToReport(mate) returned null. mate is ${mate.getPath()}"
            logger_.info(msg)
            vtLogger_.info(msg)
        }
    }
    

     
    
    /**
     * This is a cache of PathResolutionLogBundle object keyed with the path of bundleFile.
     * 
     * @author kazurayam
     *
     */
    private static class PathResolutionLogBundleCache {
        
        static Logger logger_ = LoggerFactory.getLogger(
            PathResolutionLogBundleCache.class)

        static final String classShortName = Helpers.getClassShortName(
            PathResolutionLogBundleCache.class)
        
        private Map<Path, PathResolutionLogBundle> cache_
        private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
        
        PathResolutionLogBundleCache() {
            cache_ = new HashMap<Path, PathResolutionLogBundle>()
        }
        
        void setVisualTestingLogger(VisualTestingLogger vtLogger) {
            this.vtLogger_ = vtLogger
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
                    String msg = this.class.getSimpleName() + "#get failed to deserialize PathResolutionLogBundle instance from ${bundleFile}"
                    logger_.warn(msg)
                    //vtLogger_.failed(msg)
                    return null
                }
                return bundle
            }
        }
    }
}
