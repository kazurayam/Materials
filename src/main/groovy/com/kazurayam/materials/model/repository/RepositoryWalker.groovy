package com.kazurayam.materials.model.repository

import com.kazurayam.materials.Material
import com.kazurayam.materials.model.TCaseResult
import com.kazurayam.materials.model.TSuiteResult

final class RepositoryWalker {

    static RepositoryRoot walkRepository(RepositoryRoot repoRoot, RepositoryVisitor visitor) {
        Objects.requireNonNull(repoRoot, "repoRoot must not be null")
        Objects.requireNonNull(visitor, "visitor must not be null")

        // traverse the Material Repository tree
        visitor.preVisitRepositoryRoot(repoRoot)

        for (TSuiteResult tSuiteResult : repoRoot.getSortedTSuiteResults()) {
            visitor.preVisitTSuiteResult(tSuiteResult)

            for (TCaseResult tCaseResult : tSuiteResult.getTCaseResultList()) {
                visitor.preVisitTCaseResult(tCaseResult)

                for (Material material : tCaseResult.getMaterialList()) {

                    try {
                        if (material.getPath().toFile().exists()) {
                            visitor.visitMaterial(material)
                        } else {
                            visitor.visitMaterialFailed(material,
                                new IOException("${material.getPath().toString()} is not present"))
                        }
                    } catch (IOException e) {
                        visitor.visitMaterialFailed(material, e)
                    }

                }

                visitor.postVisitTCaseResult(tCaseResult)
            }

            visitor.postVisitTSuiteResult(tSuiteResult)
        }

        visitor.postVisitRepositoryRoot(repoRoot)

        return repoRoot
    }

}
