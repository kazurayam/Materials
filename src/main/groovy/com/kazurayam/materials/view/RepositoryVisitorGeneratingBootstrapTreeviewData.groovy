package com.kazurayam.materials.view

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
import com.kazurayam.materials.VTLoggerEnabled
import com.kazurayam.materials.imagedifference.ComparisonResultBundle
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitorSimpleImpl
import java.util.stream.Collectors;

/**
 *
 * @author kazurayam
 *
 */
class RepositoryVisitorGeneratingBootstrapTreeviewData
        extends RepositoryVisitorSimpleImpl implements RepositoryVisitor, VTLoggerEnabled {
     
     static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingBootstrapTreeviewData.class)
     
     private ReportsAccessor reportsAccessor_
     private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
     
     private int tSuiteResultCount
     private int tCaseResultCount
     private int materialsCount
     private def comparisonResultBundle
     
     private static List<String> fileNamesToIgnore = [
         'comparison-result-bundle.json',
         'image-delta-stats.json',
         '.highlight.json'
         ]
     
     RepositoryVisitorGeneratingBootstrapTreeviewData(Writer writer) {
         super(writer)
     }
     
     void setReportsAccessor(ReportsAccessor reportsAccessor) {
         this.reportsAccessor_ = reportsAccessor
     }
     
	 // -------- VTLoggerEnabled --------------------------------------
     
	 @Override
     void setVisualTestingLogger(VisualTestingLogger vtLogger) {
         this.vtLogger_ = vtLogger
     }
     
	 // implements RepositoryVisitor -----------------------------------
	 
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
         sb.append('    "expanded":' + (tSuiteResult.getTSuiteName().getValue().endsWith('_exam')))
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
             junitReportWrapper         = reportsAccessor_.getJUnitReportWrapper(tSuiteResult.getId())
             executionPropertiesWrapper = reportsAccessor_.getExecutionPropertiesWrapper(tSuiteResult.getId())
         }
         if (junitReportWrapper != null) {

             // number of Tests
             sb.append(',')
             sb.append('"tags": ["')
             logger_.info("#toBootstrapTreeviewData this.getTSuiteName() is '${tSuiteResult.getId().getTSuiteName()}'")
             sb.append(junitReportWrapper.getTestSuiteSummary(tSuiteResult.getId().getTSuiteName().getId()))
             sb.append('"')

             // execution time in seconds while rounding down to integer. E.g,   47.341 -> 47 seconds
             sb.append(',')
             sb.append('"')
             String secondsScale3 = junitReportWrapper.getTestSuiteTime(tSuiteResult.getId().getTSuiteName().getId())
             int seconds = Double.parseDouble(secondsScale3 ?: "0").intValue()
             sb.append("TIME:${seconds}")
             sb.append('"')

             sb.append(',')
             sb.append('"')
             // screenshots:
             def numOfPNGFiles = tSuiteResult.getMaterialList().stream().filter { material ->
                 material.getFileType() == FileType.PNG }.collect(Collectors.toList()).size()
             sb.append("PNG:${numOfPNGFiles}")
             sb.append('"')

             // ExecutionProfile name
             sb.append(',')
             sb.append('"')
             sb.append("${executionPropertiesWrapper.getTExecutionProfile()}")
             sb.append('"')

             // Browser name
             sb.append(',')
             sb.append('"')
             sb.append("${executionPropertiesWrapper.getDriverName()}")
             sb.append('"')

             sb.append(']')
         } else {
             vtLogger_.info(this.class.getSimpleName() + "#postVisitTSuiteResult failed to instanciate JUnitReportWrapper object") 
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
             junitReportWrapper = reportsAccessor_.getJUnitReportWrapper(tCaseResult.getParent().getId())
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
         if (toList(material)) {
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
         }
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
         throw new UnsupportedOperationException("failed visiting " + material.toString())
     }
     
     /**
      * The material file should be listed in the tree view if
      * toList() returns true.
      * 
      * If the material file is one of 
      * - comparison-result-bundle.json
      * - image-delta-stats.json
      * - *.highlight.json
      * then return false. Otherwise return true.
      * 
      * @param material
      * @return
      */
     private boolean toList(Material material) {
         boolean result = true
         for (String toIgnore in fileNamesToIgnore) {
             if (material.getFileName().endsWith(toIgnore)) {
                 result = false
             }
         }
         return result
     }
}

