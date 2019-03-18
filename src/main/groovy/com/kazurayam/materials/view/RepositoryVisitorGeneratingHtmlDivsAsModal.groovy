package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitResult

/**
 * This class works as a part of Closure in the generate method of 
 * com.kazurayam.materials.view.BaseIndexer.
 * 
 * @author kazurayam
 */
class RepositoryVisitorGeneratingHtmlDivsAsModal 
                    extends Closure<Boolean> 
                    implements RepositoryVisitor {
                       
    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingHtmlDivsAsModal.class)
    
    RepositoryVisitorGeneratingHtmlDivsAsModal() {
        super(null)
    }
    
    /*
     * implementing methods requied by Closure
     */
    Boolean doCall(final Object value) {
        delegate.p "FOOfooFOO"
    }
                        
    /*
     * implementing methods required by RepositoryVisitor
     */
    @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {}
    @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {}
    @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {}
    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {}
    @Override RepositoryVisitResult visitMaterial(Material material) {}
    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }
}
