package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.model.MaterialFileName
import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.repository.RepositoryRoot

import groovy.json.JsonOutput

/**
 * A Material has a Path <TSuiteName>/<TSuiteTimestamp>/<TCaseName>/<subpath>/<MaterialFileName>
 */
class MaterialImpl implements Material, Comparable<Material> {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialImpl.class)
    private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    private TCaseResult parentTCR_
    private String subpath_
    private MaterialFileName materialFileName_
    
    private LocalDateTime lastModified_
    private long length_
    
    // following properties are optional
    private String description_ = null
    
    // -------- constructors --------------------------------------------------
    
    MaterialImpl(TCaseResult parent, String subpath, URL url, Suffix suffix, FileType fileType) {
        Objects.requireNonNull(parent, "parent TCaseResult must not be null")
        Objects.requireNonNull(subpath, "subpath must not be null")
        parentTCR_ = parent
        subpath_ = subpath
        materialFileName_ = new MaterialFileName(MaterialFileName.format(url, suffix, fileType))
    }
    
    /**
     * 
     * @param parent TCaserResult at './Materials/main.TS1/20180530_130419/main.TC1'
     * @param filePath './Materials/main.TS1/20180530_130419/main.TC1/foo/bar/fixture.xls'
     * 
     * supath_ should be equal to Paths.get('foo/bar')
     */
    MaterialImpl(TCaseResult parent, Path filePath) {
        Objects.requireNonNull(parent, "parent must not be null")
        Objects.requireNonNull(filePath, "filePath must not be null")
		init(parent, filePath)
    }
	
	private void init(TCaseResult parent, Path filePath) {
        parentTCR_ = parent
        subpath_ = parent.getTCaseDirectory().normalize().relativize(filePath.getParent().normalize()).toString()
        materialFileName_ = new MaterialFileName(filePath.getFileName().toString())
    }
    
    
    // ------- implematation of MaterialCore interface ------------------------
    
    @Override
    Path getBaseDir() {
        TCaseResult tcr = this.getParent()
        TSuiteResult tsr = tcr.getParent()
        RepositoryRoot repoRoot = tsr.getParent()
        return repoRoot.getBaseDir()
        // for short, return this.getParent().getParent().getParent().getBaseDir()
    }
    
    @Override
    Path getPath() {
        logger_.debug("#getPath parentTCR_.getTCaseDirectory()=${parentTCR_.getTCaseDirectory()}")
        logger_.debug("#getPath subpath_=${subpath_}")
        logger_.debug("#getPath parentTCR_.getTCaseDirectory().resolve(subpath_)=${parentTCR_.getTCaseDirectory().resolve(subpath_)}")
        logger_.debug("#getPath materialFileName_.getFileName()=${materialFileName_.getFileName()}")
        if (parentTCR_ != null) {
            Path p = parentTCR_.getTCaseDirectory().resolve(subpath_).resolve(materialFileName_.getFileName()).normalize()
            logger_.debug("#getPath p=${p}")
            return p
        } else {
            throw new IllegalStateException("parentTCR_ is not set")
        }
    }
    
    @Override
    boolean fileExists() {
        return Files.exists(this.getPath())
    }

    @Override
    Path getPathRelativeToRepositoryRoot() {
        //return getPathRelativeTo(this.getBaseDir())
        return this.getBaseDir().relativize(this.getPath())
    }
    //
    //private Path getPathRelativeTo(Path base) {
    //    Objects.requireNonNull(base)
    //    Path path =  base.relativize(this.getPath())
    //    logger_.debug("#getPathRelativeTo base=${base} this.getPath()=${this.getPath()} path=${path}")
    //    return path
    //}
    
    @Override
    String getHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        Path href = rootDir.relativize(this.getPath())
        return href.normalize().toString().replace('\\', '/')
    }
    
    @Override
    String getDescription() {
        return this.description_
    }
    
    @Override
    void setDescription(String description) {
        this.description_ = description
    }
    
    @Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }

    // ------------- implematation of Material interface ----------------------
    @Override
    Material setParent(TCaseResult parent) {
        Objects.requireNonNull(parent)
        parentTCR_ = parent
        return this
    }
    
    @Override
    String getFileName() {
        Objects.requireNonNull(materialFileName_, "materialFileName_ must not be null")
        materialFileName_.getFileName()
    }
    
    @Override
    TCaseResult getParent() {
        return this.getTCaseResult()
    }
    
    /**
     * @return a TCaseName object, same as this.getTCaseResult().getTcaseName(); may return null
     */
    @Override
    TCaseName getTCaseName() {
        if (this.getTCaseResult() != null) {
            return this.getTCaseResult().getTCaseName()
        } else {
            return null
        }
    }
    
    /**
     * @return a TCaseResult object, may return null
     */
    @Override
    TCaseResult getTCaseResult() {
        return parentTCR_
    }

    /**
     * @return a URL object set by the constructor, may return null
     */
    @Override    
    URL getURL() {
        return materialFileName_.getURL()
    }
    
    
    @Override
    Suffix getSuffix() {
        return materialFileName_.getSuffix()
    }
    
    @Override
    FileType getFileType() {
        return materialFileName_.getFileType()
    }

    
    @Override    
    String getSubpath() {
        return subpath_
    }
    
    /**
     * @return may return null
     *
    @Override
    Path getSubpath() {
        Path p = this.getPathRelativeToTSuiteTimestamp()   // main.TC4\foo\bar\smilechart.xls
        try {
            return p.subpath(1, p.getNameCount() - 1)      // -> foo\bar
        } catch (IllegalArgumentException ex) {
            return null                                    // main.TC1\somefile.txt --> no subpath here, return null
        }
    }
     */

        
    /**
     * if this.getPath() returns Paths.get("C:\tmp\VisualTesting\Materials\TS0\20181003_105321\TC1\foo\bar.png")
     * then the method returns Path('TC1\foo').
     * In other words, returns the Path of the parent directory of this Material, relative to the TSuiteResult.
     *
     * @return
     */
    @Override
    Path getParentDirectoryPathRelativeToTSuiteResult() {
        Path tSuiteTimestampDir = this.getParent().getParent().getTSuiteTimestampDirectory()
        return tSuiteTimestampDir.relativize(parentTCR_.getTCaseDirectory().resolve(subpath_))
    }
        
    @Override    
    Material setLastModified(long lastModified) {
        Objects.requireNonNull(lastModified)
        Instant instant = Instant.ofEpochMilli(lastModified)
        this.setLastModified(instant)
        return this
    }
    
    @Override
    Material setLastModified(Instant lastModified) {
        Objects.requireNonNull(lastModified)
        lastModified_ = LocalDateTime.ofInstant(lastModified, ZoneOffset.UTC);
        return this
    }
    
    @Override
    LocalDateTime getLastModified() {
        return lastModified_
    }
    
    @Override
    long getLength() {
        return length_
    }
    
    @Override
    Material setLength(long length) {
        length_ = length
        return this
    }
    // ---------------- business ----------------------------------------------
    
    @Override
    Path getPathRelativeToTSuiteTimestamp() {
        Path tSuiteTimestampDir = this.getParent().getParent().getTSuiteTimestampDirectory()
        Path relativeToTS = tSuiteTimestampDir.relativize(this.getPath())
        logger_.debug("#getPathRelativeToTSuiteTimestamp relativeToTS=${relativeToTS}")
        return relativeToTS
    }
    
    
    
    
    
    /**
     * Provided that the Material exists at 
     *    - TSuiteName: 'Test Suites/main/TS1'
     *    - TSuiteTimestamp: '20180805_081908'
     *    - TCaseName: 'Test Cases/main/TC1'
     *    - Material's relative path to the TCaseResult dir: 'screenshot.png'
     * then hrefToReport() should return
     *    '../Reports/main/TS1/20180805_081908/20180805_081908.html'
     * here we assume that the Materials directory and the Reports directory is 
     * located under a single parent directory.
     * 
     * Howerver, if you locate the Materials directory on a network drive (or on a Newowrk File System)
     * and you use the GUI mode of Katalon Studio,
     * there is a case where the Materials directory and the Reports directory are isolated.
     * In that case this getHrefToReport() returns null.
     * 
     * If you run Katalon Studio in Console Mode specifying -reportDir option, you can move
     * the Reports directory to the sibling of the Materials directory. In this case this getHrefToReport()
     * should return a valid path string.
     */
    //FIXME
    @Override
    String getHrefToReport() {
        TCaseResult tCaseResult = this.getParent()
        TSuiteResult tSuiteResult = tCaseResult.getParent()
        RepositoryRoot repoRoot = tSuiteResult.getParent()
        Path reportsDir = repoRoot.getReportsDir()
        if (reportsDir != null) {
            //vtLogger_.info(this.class.getSimpleName() + "#getHrefToReport reportsDir=${reportsDir.toString()}")
            //vtLogger_.info(this.class.getSimpleName() + "#getHrefToReport TSuiteName=${tSuiteResult.getTSuiteName().toString()}")
            Path tsnPath = reportsDir.resolve(tSuiteResult.getTSuiteName().getAbbreviatedId())
            Path tstPath = tsnPath.resolve(tSuiteResult.getTSuiteTimestamp().format())
            Path htmlPath = tstPath.resolve(tstPath.getFileName().toString() + '.html').toAbsolutePath()
            // we need to relativise it; relative to the Materials dir
            Path baseDir = repoRoot.getBaseDir().toAbsolutePath()
            if (htmlPath.getRoot() == baseDir.getRoot()) {
                Path relativeHtmlPath = baseDir.relativize(htmlPath).normalize()
                return relativeHtmlPath.toString()
            } else {
                vtLogger_.failed(this.class.getSimpleName() + "#getHrefToReport different root. htmlPath=${htmlPath} baseDir=${baseDir}")
                return null
            }
        } else {
            vtLogger_.failed(this.class.getSimpleName() + "#getHrefToReport reportsDir is null")
            return null
        }
    }

    

    // ---------------------------------------------

    @Override
    String getEncodedHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getEncodedHrefRelativeTo(rootDir)
    }
    private String getEncodedHrefRelativeTo(Path base) {
        Objects.requireNonNull(base)
        String baseUri = base.toUri()
        String mateUri = this.getPath().toUri()
		//System.out.println("baseUri=${baseUri}")
		//System.out.println("mateUri=${mateUri}")
        String result = mateUri.substring(baseUri.length())
        logger_.debug("#getEncodedHrefRelativeTo baseUri=${baseUri} mateUri=${mateUri} result={result}")
        return result
    }

    /**
     * returns the identifier of the Material which is used as
     * - the name in the Bootstrap Treeview
     * - the title of Modal window
     * 
     * The identifer is in the following format:
     * 
     * <PRE>subpath under the TCaseResult directory/filename</PRE>
     * 
     * For example, an identifier could be
     * 
     * <PRE>fundDetail#corp=ecza&code=01311962.1024x768.png</PRE>
     * <PRE>main.fnhp.visitAllFunds_ecza_pc/fundDetail#corp=ecza&code=47311037.1024x768.20190403_100516_product-20190403_100517_develop.(0.12).png</PRE>
     *
     * @return
     */
    @Override
    String getIdentifier() {
        StringBuilder sb = new StringBuilder()
        if (this.getURL() != null) {
            if (this.getSubpath().toString() != '' && this.getSubpath().toString() != '.') {
                sb.append(this.getSubpath().toString())
                sb.append('/')
                sb.append(' ')
            }
            String urlStr = this.getURL().toString()
            sb.append(urlStr)
            if (this.getSuffix() != Suffix.NULL) {
                sb.append(' ')
                sb.append(this.getSuffix().toString())
            }
            if (!urlStr.endsWith(this.getFileType().getExtension())) {
                sb.append(' ')
                sb.append(this.getFileType().name())
            }
        } else {
            Path subpath = parentTCR_.getTCaseDirectory().relativize(this.getPath())
            sb.append(subpath.toString().replace(File.separator, '/'))
        }
        return sb.toString()
    }

    // ---------------- helpers -----------------------------------------------

    // ---------------- overriding java.lang.Object properties --------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof Material)) { return false }
        Material other = (Material)obj
        return this.getPath().equals(other.getPath())
    }

    @Override
    int hashCode() {
        return this.getPath().hashCode()
    }

    @Override
    int compareTo(Material other) {
        return this.getPath().compareTo(other.getPath())
    }

    @Override
    String toString() {
        return this.toJsonText()
    }

    @Override
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"Material":{')
        sb.append('"url":"'          + Helpers.escapeAsJsonText(this.getURL().toString())+ '",')
        sb.append('"suffix":"'       + Helpers.escapeAsJsonText(this.getSuffix().toString())+ '",')
        sb.append('"fileType":'      + this.getFileType().toString() + ',')
        sb.append('"path":"'         + Helpers.escapeAsJsonText(this.getPath().toString()) + '",')
        sb.append('"hrefRelativeToRepositoryRoot":"' + Helpers.escapeAsJsonText(this.getHrefRelativeToRepositoryRoot()) + '",')
        sb.append('"lastModified":"' + this.getLastModified().toString() + '"')
        if (this.getDescription() != null) {
            sb.append(',')
            sb.append('"description":"' + Helpers.escapeAsJsonText(this.getDescription()) + '"')
        }
        sb.append('}}')
        return sb.toString()
    }
    
}
