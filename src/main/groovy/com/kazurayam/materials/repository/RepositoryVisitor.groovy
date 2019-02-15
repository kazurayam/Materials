package com.kazurayam.materials.repository

import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult

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
