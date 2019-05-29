package com.kazurayam.materials.view

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.imagedifference.ComparisonResultBundle
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitorSimpleImpl

/**
 *
 * @author kazurayam
 *
 */
class RepositoryVisitorGeneratingBootstrapTreeviewData
        extends RepositoryVisitorSimpleImpl implements RepositoryVisitor {
     
     static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingBootstrapTreeviewData.class)
     
     private ReportsAccessor reportsAccessor_
     private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
     
     private int tSuiteResultCount
     private int tCaseResultCount
     private int materialsCount
     private def comparisonResultBundle
     
     RepositoryVisitorGeneratingBootstrapTreeviewData(Writer writer) {
         super(writer)
     }
     
     void setReportsAccessor(ReportsAccessor reportsAccessor) {
         this.reportsAccessor_ = reportsAccessor    
     }
     
     void setVisualTestingLogger(VisualTestingLogger vtLogger) {
         this.vtLogger_ = vtLogger
     }
     
     @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
         tSuiteResultCount = 0
         pw_.print('[')
         pw_.flush()
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
         pw_.print(']')
         pw_.flush()
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {
         if (tSuiteResultCount > 0) {
             pw_.print(',')
         }
         tSuiteResultCount += 1
         //
         StringBuilder sb = new StringBuilder()
         sb.append('{')
         sb.append('"text":"' + Helpers.escapeAsJsonText(tSuiteResult.treeviewTitle()) + '",')
         sb.append('"backColor":"#CCDDFF",')
         sb.append('"selectable":false,')
         sb.append('"state":{')
         sb.append('    "expanded":' + tSuiteResult.isLatestModified() )
         sb.append('},')
         sb.append('"nodes":[')
         pw_.print(sb.toString())
         pw_.flush()
         tCaseResultCount = 0
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {
         StringBuilder sb = new StringBuilder()
         sb.append(']')
         JUnitReportWrapper junitReportWrapper = null
         ExecutionPropertiesWrapper executionPropertiesWrapper = null
         if (reportsAccessor_ != null) {
             junitReportWrapper = reportsAccessor_.getJUnitReportWrapper(tSuiteResult)
             executionPropertiesWrapper = reportsAccessor_.getExecutionPropertiesWrapper(tSuiteResult)
         }
         if (junitReportWrapper != null) {
             sb.append(',')
             sb.append('"tags": ["')
             logger_.info("#toBootstrapTreeviewData this.getTSuiteName() is '${tSuiteResult.getId().getTSuiteName()}'")
             sb.append(junitReportWrapper.getTestSuiteSummary(tSuiteResult.getId().getTSuiteName().getId()))
             sb.append('"')
             sb.append(',')
             sb.append('"')
             sb.append("${executionPropertiesWrapper.getExecutionProfile()}")
             sb.append('"')
             sb.append(',')
             sb.append('"')
             sb.append("${executionPropertiesWrapper.getDriverName()}")
             sb.append('"')
             sb.append(']')
         }
         sb.append('}')
         pw_.print(sb.toString())
         pw_.flush()
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {
         StringBuilder sb = new StringBuilder()
         if (tCaseResultCount > 0) {
             sb.append(',')
         }
         tCaseResultCount += 1
         //
         sb.append('{')
         sb.append('"text":"' + Helpers.escapeAsJsonText(tCaseResult.getTCaseName().getValue())+ '",')
         sb.append('"selectable":false,')
         sb.append('"nodes":[')
         pw_.print(sb.toString())
         pw_.flush()
         materialsCount = 0
         //
         Material crbMaterial = tCaseResult.getMaterial(Paths.get(ComparisonResultBundle.SERIALIZED_FILE_NAME))
         if (crbMaterial != null) {
             Path baseDir = tCaseResult.getParent().getParent().getBaseDir()
             String jsonText = crbMaterial.getPath().toFile().text
             this.comparisonResultBundle = new ComparisonResultBundle(baseDir, jsonText)
         } else {
             logger_.info("${ComparisonResultBundle.SERIALIZED_FILE_NAME} is not found in ${tCaseResult.toString()}")
         }
         //
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {
         StringBuilder sb = new StringBuilder()
         sb.append(']')
         JUnitReportWrapper junitReportWrapper = null
         if (reportsAccessor_ != null) {
             junitReportWrapper = reportsAccessor_.getJUnitReportWrapper(tCaseResult.getParent())
         }
         if (tCaseResult.getParent() != null && junitReportWrapper != null) {
             def status = junitReportWrapper.getTestCaseStatus(tCaseResult.getTCaseName().getId())
             sb.append(',')
             sb.append('"tags": ["')
             sb.append(status)
             sb.append('"]')
             /*
              * #1BC98E; green
              * #E64759; red
              * #9F86FF; purple
              * #E4D836; yellow
              */
             if (status == 'FAILED') {
                 sb.append(',')
                 sb.append('"backColor": "#E4D836"')
             } else if (status == 'ERROR') {
                 sb.append(',')
                 sb.append('"backColor": "#E64759"')
             }
         }
         sb.append('}')
         pw_.print(sb.toString())
         pw_.flush()
         //
         this.comparisonResultBundle = null
         //
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult visitMaterial(Material material) {
         StringBuilder sb = new StringBuilder()
         if (materialsCount > 0) {
             sb.append(',')
         }
         materialsCount += 1
         
         // TODO Carousel based on the info of the comparisonResultBundle
         
         sb.append('{')
         sb.append('"text":"' + Helpers.escapeAsJsonText(material.getIdentifier())+ '",')
         sb.append('"selectable":true,')
         if (material.getPath().getFileName().toString().endsWith('FAILED.png')) {
             sb.append('"backColor": "#9F86FF",')
         }
         sb.append('"href":"#' + material.hashCode() + '"')
         sb.append('}')
         pw_.print(sb.toString())
         pw_.flush()
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
         throw new UnsupportedOperationException("failed visiting " + material.toString())
     }
}

