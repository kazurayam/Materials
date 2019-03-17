package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
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
     private int tSuiteResultCount
     private int tCaseResultCount
     private int materialsCount
     RepositoryVisitorGeneratingBootstrapTreeviewData(Writer writer) {
         super(writer)
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
         if (tSuiteResult.getJUnitReportWrapper() != null) {
             sb.append(',')
             sb.append('"tags": ["')
             logger_.debug("#toBootstrapTreeviewData this.getTSuiteName() is '${tSuiteResult.getId().getTSuiteName()}'")
             sb.append(tSuiteResult.getJUnitReportWrapper().getTestSuiteSummary(tSuiteResult.getId().getTSuiteName().getId()))
             sb.append('"')
             sb.append(',')
             sb.append('"')
             sb.append("${tSuiteResult.getExecutionPropertiesWrapper().getExecutionProfile()}")
             sb.append('"')
             sb.append(',')
             sb.append('"')
             sb.append("${tSuiteResult.getExecutionPropertiesWrapper().getDriverName()}")
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
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {
         StringBuilder sb = new StringBuilder()
         sb.append(']')
         if (tCaseResult.getParent() != null && tCaseResult.getParent().getJUnitReportWrapper() != null) {
             def status = tCaseResult.getParent().getJUnitReportWrapper().getTestCaseStatus(tCaseResult.getTCaseName().getId())
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
         return RepositoryVisitResult.SUCCESS
     }
     @Override RepositoryVisitResult visitMaterial(Material material) {
         StringBuilder sb = new StringBuilder()
         if (materialsCount > 0) {
             sb.append(',')
         }
         materialsCount += 1
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

