package com.kazurayam.materials

import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.view.JUnitReportWrapper
import com.kazurayam.materials.view.ExecutionPropertiesWrapper

interface ReportsAccessor { 
    
    /**
     * Replacement of com.kazurayam.materials.TSuiteResultImpl#getJUnitReportWrapper()
     * which is used by
     * - com.kazurayam.materials.view.RepositoryVisitorGeneratingBootstrapTreeviewData#postVisitTSuiteResult()
     * 
     * @param tSuteResult
     * @return 
     */
    JUnitReportWrapper getJUnitReportWrapper(TSuiteResult tSuiteResult)
    
    /**
     * Replacement of com.kazurayam.materials.TSuiteResultImpl#getExecutionProfileWrapper()
     * which is used by
     * - com.kazurayam.materials.imageDifference.ImageDifferenceFilenameResolverDefaultImpl#resolveImageDifferenceFilename()
     * - com.kazurayam.materials.view.RepositoryVisitorGeneratingBootstrapTreeviewData#postVisitTSuiteResult()
     * 
     * @param tSuiteResult
     * @return
     */
    ExecutionPropertiesWrapper getExecutionPropertiesWrapper(TSuiteResult tSuiteResult)

    /**
     * Replacement of com.kazurayam.materials.impl.MaterialImpl#getHrefReport()
     * which is used by
     * - com.kazurayam.materials.view.RepositoryVisitorGeneratingHtmlDivsAsModal#anchorToReport()
     * - com.kazurayam.materials.view.RepositoryVisitorGeneratingHtmlFragmentsOfMaterialsAsModal()
     *
     * @param material
     * @return
     */
    String getHrefToReport(Material material)

}
