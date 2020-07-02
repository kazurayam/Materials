package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.VTLoggerEnabled
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.metadata.MaterialMetadata
import com.kazurayam.materials.metadata.MaterialMetadataBundle
import com.kazurayam.materials.imagedifference.ComparisonResult
import com.kazurayam.materials.imagedifference.ComparisonResultBundle
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

/**
 * 
 * @author kazurayam
 *
 */
abstract class RepositoryVisitorGeneratingHtmlDivsAsModalBase 
        implements RepositoryVisitor, VTLoggerEnabled {
    
    protected RepositoryRoot repoRoot_
    protected MarkupBuilder mkbuilder_
    protected ReportsAccessor reportsAccessor_
    protected ComparisonResultBundle comparisonResultBundle_
    
    protected static Logger logger_ = LoggerFactory.getLogger(
        RepositoryVisitorGeneratingHtmlDivsAsModalBase.class)
    
    protected VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    protected String classShortName_ = Helpers.getClassShortName(
        RepositoryVisitorGeneratingHtmlDivsAsModalBase.class)
    
    
    /**
     * HTML class attribute to determine the width of
     * Bootstrap Modal window.
     * - 'modal-sm':    small
     * - '': standard width
     * - 'modal-lg': large
     * - 'modal-xl': extra large
     */
    abstract String getBootstrapModalSize()
    
    /**
     * constructor
     */
    RepositoryVisitorGeneratingHtmlDivsAsModalBase(RepositoryRoot repoRoot, MarkupBuilder mkbuilder) {
        Objects.requireNonNull(repoRoot, "repoRoot must not be null")
        Objects.requireNonNull(mkbuilder, "mkbuilder must not be null")
        Objects.requireNonNull(bootstrapModalSize, "bootstrapModalSize must not be null")
        this.repoRoot_ = repoRoot
        this.mkbuilder_ = mkbuilder
        this.comparisonResultBundle_ = null
    }
    
    @Override
    void setReportsAccessor(ReportsAccessor reportsAccessor) {
        this.reportsAccessor_ = reportsAccessor
    }
    // implementing RepositoryVisitorExtended interface -----------------------
    
    @Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }
    
    
    // implementing RepositoryVisitor interface -------------------------------
    @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
        mkbuilder_.mkp.comment "here is inserted the output of ${classShortName_}"
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
        mkbuilder_.mkp.comment "end of the output of ${classShortName_}"
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    
    @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    
    /**
     * Check if comparison-result-bundle.json file is there in the TCaseResult directory.
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

    private def visitMaterialAction = { Material material ->
        Objects.requireNonNull(material, "material must not be null")
        mkbuilder_.div(['id': material.hashCode(), 'class':'modal fade']) {
            mkbuilder_.div(['class':"modal-dialog ${this.getBootstrapModalSize()} modal-dialog-scrollable", 'role':'document']) {
                mkbuilder_.div(['class':'modal-content']) {
                    mkbuilder_.div(['class':'modal-header']) {
                        mkbuilder_.p(['class':'modal-title', 'id': material.hashCode() + 'title'], material.getIdentifier())
                    }
                    mkbuilder_.div(['class':'modal-body']) {
                        this.markupInModalWindowAction(material)
                    }
                    mkbuilder_.div(['class':'modal-footer']) {
                        // link to "Origin"
                        this.generateAnchorsToOrigins(mkbuilder_, material)
                        // Close button
                        mkbuilder_.button(['type':'button', 'class':'btn btn-primary',
                            'data-dismiss':'modal'], 'Close')
                        this.anchorToReport(material)
                    }
                }
            }
        }
    }
    
    
    /**
     *
     * @param mate
     */
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
                mkbuilder_.pre(['class':'pre-scrollable'], mate.getPath().toFile().getText('UTF-8'))
                break
            case FileType.TXT:
            case FileType.HTML:
                mkbuilder_.div(['style':'height:350px;overflow:auto;']) {
                    File file = mate.getPath().toFile()
                    file.readLines('UTF-8').each { line ->
                        mkbuilder_.p line
                    }
                }
                break
            case FileType.JSON:
                def content = mate.getPath().toFile().getText('UTF-8')
                def pp = JsonOutput.prettyPrint(content)
                mkbuilder_.pre(['class':'pre-scrollable'], pp)
                break
            case FileType.XML:
                def content = mate.getPath().toFile().getText('UTF-8')
                content = XmlUtil.serialize(content)
                mkbuilder_.pre(['class':'pre-scrollable'], content)
                break
            case FileType.PDF:
                mkbuilder_.div(['class':'embed-responsive embed-responsive-16by9', 'style':'padding-bottom:150%']) {
                    mkbuilder_.object(['class':'embed-responsive-item', 'data':mate.getEncodedHrefRelativeToRepositoryRoot(),
                                    'type':'application/pdf', 'width':'100%', 'height':'100%'],'')
                    mkbuilder_.div {
                        mkbuilder_.a(['href': mate.getEncodedHrefRelativeToRepositoryRoot() ],
                            mate.getPathRelativeToRepositoryRoot())
                    }
                }
                break
            case FileType.XLS:
            case FileType.XLSM:
            case FileType.XLSX:
                mkbuilder_.a(['class':'btn btn-primary btn-g', 'target':'_blank',
                            'href': mate.getEncodedHrefRelativeToRepositoryRoot()], 'Download')
                break
            default:
                def msg = "this.getFileType()='${mate.getFileType()}' is unexpected"
                logger_.info('markupInModalWindow' + msg)
                mkbuilder_.p msg
        }
    }

    /**
     * 
     * @param mate
     */
    abstract void generateImgTags(Material mate)
    
    
    /**
     *
     * @param builder
     * @param material
     */
    protected void generateAnchorsToOrigins(MarkupBuilder builder, Material material) {
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
            vtLogger_.info(msg)
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
        Path path = tsr.getTSuiteTimestampDirectory().resolve(MaterialMetadataBundle.SERIALIZED_FILE_NAME)
        if (Files.exists(path)) {
			MaterialMetadataBundle bundle = MaterialMetadataBundle.deserialize(path)
			if (bundle == null) {
                // failed loading material-metadata-bundle.json of this material
                return null
            }
            MaterialMetadata metadata = bundle.findLastByMaterialPath(material.getHrefRelativeToRepositoryRoot())
            if (metadata != null) {
                String result = metadata.getUrl()   // getUrl() may return null
                logger_.info("#getOriginHref returning ${result}")
                return result
            } else {
                String msg = this.class.getSimpleName() +
                            "#getOriginHref could not find a MaterialMetadata entry of " +
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
    
    
    protected String findTestSuiteTimestamp(RepositoryRoot repoRoot, MaterialCore materialCore) {
        Material material = repoRoot.getMaterial(materialCore)
        if (material == null) {
            logger_.warn("repoRoot.getMaterial(materialCore) returned null where materialCore=${materialCore.toString()}")
            return null
        }
        TCaseResult tcr = material.getParent()
        TSuiteResult tsr = tcr.getParent()
        return tsr.getTSuiteTimestamp().format()
    }
    
    /**
     * 
     * @param material
     * @return
     */
    protected String findExecutionProfileName(RepositoryRoot repoRoot, MaterialCore materialCore) {
        Objects.requireNonNull(repoRoot, "repoRoot must not be null")
        Objects.requireNonNull(materialCore, "materialCore must not be null")
        Material material = repoRoot.getMaterial(materialCore)
        if (material == null) {
            logger_.warn("repoRoot.getMaterial(materialCore) returned null for materialCore=${materialCore.toString()}")
            return null
        }
		TCaseResult tcr = material.getParent()
        TSuiteResult tsr = tcr.getParent()
        return tsr.getId().getTExecutionProfile().getName()
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
    private String getExpectedMaterialOriginHref(Path baseDir, ComparisonResult cr) {
        def jsonObject = new JsonSlurper().parseText(cr.toJsonText())
        return getXMaterialOriginHref(baseDir, jsonObject.ComparisonResult.expectedMaterial.Material.hrefRelativeToRepositoryRoot)
    }
    
    private String getActualMaterialOriginHref(Path baseDir, ComparisonResult cr) {
        def jsonObject = new JsonSlurper().parseText(cr.toJsonText())
        return getXMaterialOriginHref(baseDir, jsonObject.ComparisonResult.actualMaterial.Material.hrefRelativeToRepositoryRoot)
    }
    
    private String getXMaterialOriginHref(Path baseDir, String hrefRelativeToRepositoryRoot) {
        String[] components = hrefRelativeToRepositoryRoot.split('/')             // [ '47news.chronos_capture', '20190404_111956', '47news.visitSite', 'top.png' ]
        if (components.length > 2) {
            Path metadataBundlePath = baseDir.resolve(components[0]).resolve(components[1]).resolve(MaterialMetadataBundle.SERIALIZED_FILE_NAME)
            if (Files.exists(metadataBundlePath)) {
                MaterialMetadataBundle metadataBundle = MaterialMetadataBundle.deserialize(metadataBundlePath)
                MaterialMetadata metadata = metadataBundle.findLastByMaterialPath(hrefRelativeToRepositoryRoot)
                /*
                * An instance of MaterialMetadata is, for example:
                * <PRE>
                * {
                *   "MaterialMetadataBundle": [
                *     {
                *       "MaterialMetadata": {
                *         "MaterialPath": "47news.chronos_capture/20190404_112053/47news.visitSite/top.png",
                *         "TCaseName": "Test Cases/47news/visitSite",
                *         "InvokedMethodName": "resolveScreenshotPathByUrlPathComponents",
                *         "SubPath": "",
                *         "URL": "https://www.47news.jp/",
                *         "ExecutionProfileName": "default"
                *       }
                *     }
                *   ]
                * }
                * </PRE>
                * but, remember, InvokeMethodName can also be resolveMaterialPath, and in that case
                * there would not be URL property.
                */
                if (metadata != null && metadata.getUrl() != null) {
                    return metadata.getUrl().toExternalForm()
                } else {
                    return null
                }
            } else {
                String msg = "#getXMaterialOriginHref ${metadataBundlePath} does ot exist"
                logger_.warn(msg)
                vtLogger_.failed(msg)
                return null
            }
        }
    }

    

    protected void anchorToReport(Material mate) {
        Path baseDir = mate.getParent().getParent().getRepositoryRoot().getBaseDir()
        String reportHref = null
        Objects.requireNonNull(reportsAccessor_ , this.class.getSimpleName() + "#anchorToReport reportsAccessor_ must not be null")
        reportHref = reportsAccessor_.getHrefToReport(mate)
        //vtLogger_.info(this.class.getSimpleName() + "#anchorToReport baseDir=${baseDir.toString()}")
        //vtLogger_.info(this.class.getSimpleName() + "#anchorToReport reportHref=${reportHref}")
        if (reportHref != null) {
            Path p = baseDir.resolve(reportHref)
            if (Files.exists(p)) {
                mkbuilder_.a(['href':reportHref, 'class':'btn btn-default', 'role':'button',
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

}
