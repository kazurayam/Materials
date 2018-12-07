package com.kazurayam.materials

import com.kazurayam.materials.model.TCaseResult
import com.kazurayam.materials.model.TSuiteResult

/**
 *
 * RepositoryRoot > TSuiteResult > TCaseResult > Material
 *
 * @author kazurayam
 *
 */
interface RepositoryVisitor {

    RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot)

    RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot)

    RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult)

    RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult)

    RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult)

    RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult)

    RepositoryVisitResult visitMaterial(Material material)

    RepositoryVisitResult visitMaterialFailed(Material material, IOException ex)

}
