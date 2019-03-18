package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitResult

import groovy.xml.MarkupBuilder

/**
 * This class is a Closure, which works as a part of Closure 
 * in the generate method of 
 * com.kazurayam.materials.view.BaseIndexer#generate().
 * 
 * @author kazurayam
 */
class RepositoryVisitorGeneratingHtmlDivsAsModal 
                    implements RepositoryVisitor {
                       
    static Logger logger_ = LoggerFactory.getLogger(
                            RepositoryVisitorGeneratingHtmlDivsAsModal.class)
    
    static final String classShortName = Helpers.getClassShortName(
                            RepositoryVisitorGeneratingHtmlDivsAsModal.class)
    
    MarkupBuilder builder
    
    RepositoryVisitorGeneratingHtmlDivsAsModal(MarkupBuilder builder) {
        this.builder = builder
    }
    
    def preVisitRepositoryRootAction = {
        builder.mkp.comment "here is inserted the output of ${classShortName}"
        builder.p "FOOfooBAR"
    }
    
    def postVisitRepositoryRootAction = {
        builder.mkp.comment "end of the output of ${classShortName}"
    }
    
    def visitMaterialAction = { material ->
        Objects.requireNonNull(material, "material must not be null")
        builder.div(['id': material.hashCode(), 'class':'modal fade']) {
            builder.div(['class':'modal-dialog modal-lg', 'role':'document']) {
                builder.div(['class':'modal-content']) {
                    builder.div(['class':'modal-header']) {
                        builder.p(['class':'modal-title', 'id': material.hashCode() + 'title'])
                    }
                    builder.div(['class':'modal-body]')
                }
            }
        }
    }
                        
    /*
     * implementing methods required by RepositoryVisitor
     */
    @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
        preVisitRepositoryRootAction()
        return RepositoryVisitResult.SUCCESS
    }
    @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
        postVisitRepositoryRootAction()
        return RepositoryVisitResult.SUCCESS
    }
    @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {}
    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {}
    
    @Override RepositoryVisitResult visitMaterial(Material material) {
        visitMaterialAction(material)
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }
}
