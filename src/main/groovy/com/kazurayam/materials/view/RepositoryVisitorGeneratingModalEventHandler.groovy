package com.kazurayam.materials.view

import com.kazurayam.materials.Material
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.VTLoggerEnabled
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryVisitorSimpleImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RepositoryVisitorGeneratingModalEventHandler
        extends RepositoryVisitorSimpleImpl implements RepositoryVisitor, VTLoggerEnabled {
            
    static Logger logger_ = LoggerFactory.getLogger(RepositoryVisitorGeneratingModalEventHandler.class)
    
    private ReportsAccessor reportsAccessor_
    private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    RepositoryVisitorGeneratingModalEventHandler(Writer writer) {
        super(writer)
    }
    
    void setReportsAccessor(ReportsAccessor reportsAccessor) {
        this.reportsAccessor_ = reportsAccessor
    }
    
	// ---- VTLoggerEnabled -------------------------------------------
	
	@Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }
    
	// implementing RepositoryVisitor -------------------------------------------
	
    @Override RepositoryVisitResult preVisitRepositoryRoot(RepositoryRoot repoRoot) {
        pw_.print("\n")
        pw_.println('        $(function() {')
        pw_.flush()
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult postVisitRepositoryRoot(RepositoryRoot repoRoot) {
        pw_.println('        });')
        pw_.flush()
        return RepositoryVisitResult.SUCCESS
    }
    
    @Override RepositoryVisitResult preVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    
    @Override RepositoryVisitResult postVisitTSuiteResult(TSuiteResult tSuiteResult) {}
    
    @Override RepositoryVisitResult preVisitTCaseResult(TCaseResult tCaseResult) {}

    @Override RepositoryVisitResult postVisitTCaseResult(TCaseResult tCaseResult) {}
    
    /**
     * Check if the Material file is a .PNG file, and it has an associated *.highlight file.
     * If yes, generate a JS code as follows:
     * <PRE>
     *             $("#-610036477").on('shown.bs.modal', function() {
     *                 $(this).find("div.modal-body").scrollTop(4287);
     *             });
     * </PRE>
     */
    @Override RepositoryVisitResult visitMaterial(Material material) {
        StringBuilder sb = new StringBuilder()
        sb.append(' ' * 12)
        sb.append('$(\"#' + material.hashCode() + '\").on(\"shown.bs.modal\", function(e) {' + "\n")
        sb.append(' ' * 16)
        sb.append('$(this).find(\"div.modal-body\").scrollTop(')
        sb.append(4087)
        sb.append(');' + "\n")
        sb.append(' ' * 12)
        sb.append('});' + "\n")
        pw_.print(sb.toString())
        pw_.flush()
        return RepositoryVisitResult.SUCCESS
    }

    @Override RepositoryVisitResult visitMaterialFailed(Material material, IOException ex) {
        throw new UnsupportedOperationException("failed visiting " + material.toString())
    }
}
