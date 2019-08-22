package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Material
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
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
    JUnitReportWrapper getJUnitReportWrapper(TSuiteResultId tSuiteResultId) {
        Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")
        Path reportFilePathKS622 = locateReportFile(reportsDir_, tSuiteResultId, 'JUnit_Report.xml')
		if (reportFilePathKS622 != null && Files.exists(reportFilePathKS622)) {
            return new JUnitReportWrapper(reportFilePathKS622)
        } else {
			Path found = null
			Files.list(reportsDir_).forEach({ Path entry ->
				if (Files.isDirectory(entry)) {
					Path reportFilePathKS630 = locateReportFile(entry, tSuiteResultId, 'JUnit_Report.xml')
					if (reportFilePathKS630 != null && Files.exists(reportFilePathKS630)) {
						found = reportFilePathKS630
					}
				}
			})
			if (found != null) {
				return new JUnitReportWrapper(found)
			} else {
				String msg = this.class.getSimpleName() + 
					"#getJUnitReportWrapper JUnit_Report.xml file of " + 
					"TSuiteResultId ${tSuiteResultId} is not found under ${reportsDir_}"
				logger_.warn(msg)
				vtLogger_.failed(msg)
				return null
			}
        }
    }
    
    @Override
    ExecutionPropertiesWrapper getExecutionPropertiesWrapper(TSuiteResultId tSuiteResultId) {
        Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")
        Path expropFilePath = locateReportFile(reportsDir_, tSuiteResultId, 'execution.properties')
        if (expropFilePath != null && Files.exists(expropFilePath)) {
            return new ExecutionPropertiesWrapper(expropFilePath)
        } else {
			Path found = null
			Files.list(reportsDir_).forEach({ Path entry ->
				if (Files.isDirectory(entry)) {
					Path reportFilePathKS630 = locateReportFile(entry, tSuiteResultId, 'execution.properties')
					if (reportFilePathKS630 != null && Files.exists(reportFilePathKS630)) {
						found = reportFilePathKS630
					}
				}
			})
			if (found != null) {
				return new ExecutionPropertiesWrapper(found)
			} else {
				String msg = this.class.getSimpleName() + 
				"#getExecutionPropertiesWrapper execution.properties file of " +
				"TSuiteResultId ${tSuiteResultId} is not found under ${reportsDir_}"
				logger_.warn(msg)
				vtLogger_.failed(msg)
				return null
			}
        }
    }


    /**
     * Provided that a Material exists of the following attributes: 
     *    - TSuiteName: 'Test Suites/main/TS1'
     *    - TSuiteTimestamp: '20180805_081908'
     *    - TCaseName: 'Test Cases/main/TC1'
     *    - Material's relative path to the TCaseResult dir: 'screenshot.png'
     * then hrefToReport() look up a HTML report file which is relevant to the Material.
     * 
     * The path of the HTML report is formated as a relative path to the Materials directory.
     * 
     *    - '../Reports/main/TS1/20180805_081908/20180805_081908.html' when Katalon Stduio version 6.2.2 and prior
     *    or
     *    - '../Reports/20190821_143318/CURA/twins_examp/20190821_143321/20190821_143321.html'
     *     
     * Here we assume that the Materials directory and the Reports directory is 
     * located under a single parent directory.
     * 
     * As of Katalon Studio v6.1.5, the path of HTML report is in the format of
     *     Reports/CURA/twins_exam/20190528_111335/20190528_111335.html
     * 
     * As of Katalon Studio v6.3.0, the path of HTML report is in the format of
     *     Reports/20190821_143318/CURA/twins_exam/20190821_143321/20190821_143321.html
     *
     * If you locate the Materials directory on a network drive (or on a Newowrk File System)
     * and you use the GUI mode of Katalon Studio,
     * there is a case where the Materials directory and the Reports directory are isolated.
     * In that case this getHrefToReport() returns null.
     * 
     * If you run Katalon Studio in Console Mode specifying -reportDir option, you can move
     * the Reports directory to the sibling of the Materials directory. In this case this getHrefToReport()
     * should return a valid path string.
     * 
     * @param material 
     */
    @Override
    String getHrefToReport(Material material) {
        Objects.requireNonNull(material, "material must not be null")
        TCaseResult tCaseResult = material.getParent()
        TSuiteResult tSuiteResult = tCaseResult.getParent()
        RepositoryRoot repoRoot = tSuiteResult.getParent()
        Path materialsDir = repoRoot.getBaseDir().toAbsolutePath()
        
		// valid only for KS6.2.2, KS6.3.0 has different path format
		Path tSuiteNameElementOfHTMLReportPath = reportsDir_.resolve(tSuiteResult.getTSuiteName().getAbbreviatedId())
        
		Path tSuiteTimestampElementOfHTMLReportPath = tSuiteNameElementOfHTMLReportPath.resolve(tSuiteResult.getTSuiteTimestamp().format())
		
		System.out.println("#getHrefToReport tSuiteTimestampPath=${tSuiteTimestampElementOfHTMLReportPath}")
		
        Path htmlPath = tSuiteTimestampElementOfHTMLReportPath.resolve(tSuiteTimestampElementOfHTMLReportPath.getFileName().toString() + '.html').toAbsolutePath()
        System.out.println("#getHrefToReport htmlPath=${htmlPath}")

        // we want to relativize it; relative to the Materials dir
		System.out.println("#getHrefToReport htmlPath.getRoot()=${htmlPath.getRoot()}")
		System.out.println("#getHrefToReport materialsDir.getRoot()=${materialsDir.getRoot()}")
        if (htmlPath.getRoot() == materialsDir.getRoot()) {
            Path relativeHtmlPath = materialsDir.relativize(htmlPath).normalize()
			System.out.println("#getHrefToReport relativeHtmlPath=${relativeHtmlPath}")
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
    
	
	private static Path locateReportFile(Path baseDir, TSuiteResultId tSuiteResultId, String fileName) {
		Objects.requireNonNull(baseDir, "baseDir must not be null")
		Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")
		Objects.requireNonNull(fileName, "fileName must not be null")
		Path file = baseDir.resolve(tSuiteResultId.getTSuiteName().getValue().replace('.', '/')).
							resolve(tSuiteResultId.getTSuiteTimestamp().format()).
							resolve(fileName)
		if (Files.exists(file)) {
			return file
		} else {
			return null
		}
	}

}
