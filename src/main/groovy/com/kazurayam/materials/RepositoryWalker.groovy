package com.kazurayam.materials

class RepositoryWalker {

    static RepositoryRoot walkRepository(RepositoryRoot repoRoot, RepositoryVisitor visitor) {
        if (repoRoot == null) {
            throw new IllegalArgumentException("repoRoot is required")
        }
        if (visitor == null) {
            throw new IllegalArgumentException("visitor is required")
        }
        // traverse the Material Repository tree
        visitor.preVisitRepositoryRoot(repoRoot)

        for (TSuiteResult tSuiteResult : repoRoot.getSortedTSuiteResults()) {
            visitor.preVisitTSuiteResult(tSuiteResult)

            for (TCaseResult tCaseResult : tSuiteResult.getTCaseResults()) {
                visitor.preVisitTCaseResult(tCaseResult)

                for (Material material : tCaseResult.getMaterials()) {

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
