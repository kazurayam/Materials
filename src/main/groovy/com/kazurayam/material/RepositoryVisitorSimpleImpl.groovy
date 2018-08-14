package com.kazurayam.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RepositoryVisitorSimpleImpl implements RepositoryVisitor {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorSimpleImpl.class)

    private StringBuilder sb_

    RepositoryVisitorSimpleImpl() {
        sb_ = new StringBuilder()
    }

    @Override
    RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
        sb_.append("#preVisitRepositoryRoot ${repoRoot.getBaseDir().toString()}")
        return RepositoryVisitResult.SUCCESS
    }

    @Override
    RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
        sb_.append("#postVisitRepositoryRoot ${repoRoot.getBaseDir().toString()}")
        return RepositoryVisitResult.SUCCESS
    }

    @Override
    RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {
        sb_.append("#preVisitTSuiteResult ${tSuiteResult.treeviewTitle()}")
        return RepositoryVisitResult.SUCCESS
    }

    @Override
    RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {
        sb_.append("#postVisitTSuiteResult ${tSuiteResult.treeviewTitle()}")
        return RepositoryVisitResult.SUCCESS
    }

    @Override
    RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {
        sb_.append("#preVisitTCaseResult ${tCaseResult.getTCaseName().getValue()}")
        return RepositoryVisitResult.SUCCESS
    }

    @Override
    RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {
        sb_.append("#postVisitTCaseResult ${tCaseResult.getTCaseName().getValue()}")
        return RepositoryVisitResult.SUCCESS
    }

    @Override
    RepositoryVisitResult visitMaterial(Material material) {
        sb_.append("#visitMaterial ${material.getIdentifier()}")
        return RepositoryVisitResult.SUCCESS
    }

    @Override
    RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        sb_.append("#visitMaterialFailed ${material.getIdentifier()}")
        return RepositoryVisitResult.FAILURE
    }

    String getOutput() {
        return sb_.toString()
    }
}
