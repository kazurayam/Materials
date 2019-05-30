package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Material
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.view.ExecutionPropertiesWrapper
import com.kazurayam.materials.view.JUnitReportWrapper

class ReportsAccessorImpl implements ReportsAccessor {
    
    static Logger logger_ = LoggerFactory.getLogger(ReportsAccessorImpl.class)
    private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    private Path reportsDir_
    
    static ReportsAccessor newInstance(Path reportsDir) {
        return new ReportsAccessorImpl(reportsDir)
    }
    
    private ReportsAccessorImpl(Path reportsDir) {
        Objects.requireNonNull(reportsDir, "reportsDir must not be null")
        this.reportsDir_ = reportsDir
        
        // do more business
    }
    
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }
    
    @Override
    Path getReportsDir() {
        return this.reportsDir_
    }
    
    @Override
    JUnitReportWrapper getJUnitReportWrapper(TSuiteResult tSuiteResult) {
        Objects.requireNonNull(tSuiteResult, "tSuiteResult must not be null")
        Path reportFilePath = reportsDir_.
                                resolve(tSuiteResult.getId().getTSuiteName().getValue().replace('.', '/')).
                                resolve(tSuiteResult.getId().getTSuiteTimestamp().format()).
                                resolve('JUnit_Report.xml')
        if (Files.exists(reportFilePath)) {
            return new JUnitReportWrapper(reportFilePath)
        } else {
            String msg = this.class.getSimpleName() + "#getJUnitReportWrapper ${reportFilePath} does not exist"
            logger_.warn(msg)
            vtLogger_.failed(msg)
            return null
        }
    }
    
    @Override
    ExecutionPropertiesWrapper getExecutionPropertiesWrapper(TSuiteResult tSuiteResult) {
        Objects.requireNonNull(tSuiteResult, "tSuiteResult must not be null")
        Path expropFilePath = reportsDir_.
                                resolve(tSuiteResult.getId().getTSuiteName().getValue().replace('.', '/')).
                                resolve(tSuiteResult.getId().getTSuiteTimestamp().format()).
                                resolve('execution.properties')
        if (Files.exists(expropFilePath)) {
            return new ExecutionPropertiesWrapper(expropFilePath)
        } else {
            String msg = this.class.getSimpleName() + "#getExecutionPropertiesWrapper ${expropFilePath} does not exist"
            logger_.warn(msg)
            vtLogger_.failed(msg)
            return null
        }
    }

    /**
     * Provided that the Material exists at 
     *    - TSuiteName: 'Test Suites/main/TS1'
     *    - TSuiteTimestamp: '20180805_081908'
     *    - TCaseName: 'Test Cases/main/TC1'
     *    - Material's relative path to the TCaseResult dir: 'screenshot.png'
     * then hrefToReport() should return
     *    '../Reports/main/TS1/20180805_081908/20180805_081908.html'
     * which is the href to the report relative to the Materials directory.
     * Here we assume that the Materials directory and the Reports directory is 
     * located under a single parent directory.
     * 
     * However, if you locate the Materials directory on a network drive (or on a Newowrk File System)
     * and you use the GUI mode of Katalon Studio,
     * there is a case where the Materials directory and the Reports directory are isolated.
     * In that case this getHrefToReport() returns null.
     * 
     * If you run Katalon Studio in Console Mode specifying -reportDir option, you can move
     * the Reports directory to the sibling of the Materials directory. In this case this getHrefToReport()
     * should return a valid path string.
     */
    @Override
    String getHrefToReport(Material material) {
        Objects.requireNonNull(material, "material must not be null")
        TCaseResult tCaseResult = material.getParent()
        TSuiteResult tSuiteResult = tCaseResult.getParent()
        RepositoryRoot repoRoot = tSuiteResult.getParent()
        Path materialsDir = repoRoot.getBaseDir().toAbsolutePath()
        assert reportsDir_ != null
        Path tSuiteNamePath = reportsDir_.resolve(tSuiteResult.getTSuiteName().getAbbreviatedId())
        Path tSuiteTimestampPath = tSuiteNamePath.resolve(tSuiteResult.getTSuiteTimestamp().format())
        
        // as of Katalon Studio v6.1.5, the name of HTML report is in the format of
        //     Reports/CURA/twins_exam/20190528_111335/20190528_111335.html
        //
        Objects.requireNonNull(tSuiteTimestampPath, this.class.getSimpleName() + "#getHrefToReport tSuiteTimestampPath must not be null")
        Path htmlPath = tSuiteTimestampPath.resolve(tSuiteTimestampPath.getFileName().toString() + '.html').toAbsolutePath()
        
        // we want to relativize it; relative to the Materials dir
        if (htmlPath.getRoot() == materialsDir.getRoot()) {
            Path relativeHtmlPath = materialsDir.relativize(htmlPath).normalize()
            return relativeHtmlPath.toString().replace(File.separator, '/')
        } else {
            String msg = this.class.getSimpleName() + "#getHrefToReport different root of path." + 
                " therefore unable to create relative href from the material to the report html." + 
                " htmlPath=\'${htmlPath}\' materialsDir=\'${materialsDir}\'"
            logger_.info(msg)
            if (vtLogger_ != null) {
                vtLogger_.info(msg)
            }
            return null
        }
    }
    
}
