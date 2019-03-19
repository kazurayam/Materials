package com.kazurayam.materials.impl

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
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.model.MaterialFileName
import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.repository.RepositoryRoot

import groovy.json.JsonSlurper

class MaterialImpl implements Material {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialImpl.class)
    
    private TCaseResult parent_
    private Path dirpath_
    private MaterialFileName materialFileName_
    private LocalDateTime lastModified_
    private long length_
    
    private MaterialImpl(Path dirpath, URL url, Suffix suffix, FileType fileType) {
        Objects.requireNonNull(dirpath)
        dirpath_ = dirpath.normalize()
        materialFileName_ = new MaterialFileName(MaterialFileName.format(url, suffix, fileType))
    }
    
    private MaterialImpl(TCaseResult parent, Path filePath) {
        Objects.requireNonNull(parent)
        Objects.requireNonNull(filePath)
        parent_ = parent
        dirpath_ = parent.getTCaseDirectory().relativize(filePath.normalize().getParent()).normalize()
        if (dirpath_.toString() == '') {
            dirpath_ = Paths.get('.')
        }
        materialFileName_ = new MaterialFileName(filePath.getFileName().toString())
    }
    
    static Material newInstance(Path dirpath, URL url, Suffix suffix, FileType fileType) {
        return new MaterialImpl(dirpath, url, suffix, fileType)
    }
    
    static Material newInstance(TCaseResult parent, Path filePath) {
        return new MaterialImpl(parent, filePath)
    }
    
    @Override
    Material setParent(TCaseResult parent) {
        Objects.requireNonNull(parent)
        parent_ = parent
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
    
    @Override
    TCaseName getTCaseName() {
        return this.getTCaseResult().getTCaseName()
    }
    
    @Override
    TCaseResult getTCaseResult() {
        return parent_
    }

    @Override    
    URL getURL() {
        return materialFileName_.getURL()
    }
    
    /**
     * 
     */
    @Override
    Path getSubpath() {
        Path p = this.getPathRelativeToTSuiteTimestamp()   // main.TC4\foo\bar\smilechart.xls
        try {
            return p.subpath(1, p.getNameCount() - 1)      // -> foo\bar
        } catch (IllegalArgumentException ex) {
            return null                                    // main.TC1\somefile.txt --> no subpath here, return null
        }
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
    Path getPath() {
        if (parent_ != null) {
            return parent_.getTCaseDirectory().resolve(dirpath_).resolve(materialFileName_.getFileName()).normalize()
        } else {
            throw new IllegalStateException("parent_ is not set")
        }
    }

    @Override    
    Path getDirpath() {
        return dirpath_
    }
        
        
    /**
     * if this.getPath() returns Paths.get("C:\tmp\VisualTesting\Materials\TS0\20181003_105321\TC1\foo\bar.png")
     * then the method returns Path('TC1\foo').
     * In other words, reutrns the Path of the parent directory of this Material, relative to the TSuiteResult.
     *
     * @return
     */
    Path getDirpathRelativeToTSuiteResult() {
        Path tSuiteTimestampDir = this.getParent().getParent().getTSuiteTimestampDirectory()
        return tSuiteTimestampDir.relativize(parent_.getTCaseDirectory().resolve(dirpath_))
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
        Path path = getPathRelativeTo(tSuiteTimestampDir)
        logger_.debug("#getPathRelativeToTSuiteTimestamp path=${path}")
        return path
    }
    
    @Override
    Path getPathRelativeToRepositoryRoot() {
        Path rootDir            = this.getParent().getParent().getParent().getBaseDir()
        return getPathRelativeTo(rootDir)
    }
    
    //
    Path getPathRelativeTo(Path base) {
        Objects.requireNonNull(base)
        Path path =  base.relativize(this.getPath())
        logger_.debug("#getPathRelativeTo base=${base} this.getPath()=${this.getPath()} path=${path}")
        return path
    }
    
    @Override
    String getHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getHrefRelativeTo(rootDir)
    }
    
    /**
     * Provided that the Material exists at 
     *    - TSuiteName: 'Test Suites/main/TS1'
     *    - TSuiteTimestamp: '20180805_081908'
     *    - TCaseName: 'Test Cases/main/TC1'
     *    - Material's relative path to the TCaseResult dir: 'screenshot.png'
     * then hrefToReport() should return
     *    '../Reports/main/TS1/20180805_081908/Report.html'
     * here we assume the Reports directory is located sibling to the Materials directory.
     */
    @Override
    String getHrefToReport() {
        TCaseResult tCaseResult = this.getParent()
        TSuiteResult tSuiteResult = tCaseResult.getParent()
        RepositoryRoot repoRoot = tSuiteResult.getParent()
        Path reportsDir = repoRoot.getReportsDir()
        if (reportsDir != null) {
            println "reportsDir=${reportsDir.toString()}"
            println "TSuiteName=${tSuiteResult.getTSuiteName().toString()}"
            println "TSuiteName.getAbbreviatedId()=${tSuiteResult.getTSuiteName().getAbbreviatedId()}"
            Path tsnPath = reportsDir.resolve(tSuiteResult.getTSuiteName().getAbbreviatedId())
            Path tstPath = tsnPath.resolve(tSuiteResult.getTSuiteTimestamp().format())
            Path htmlPath = tstPath.resolve('Report.html').toAbsolutePath()
            // we need to relativise it; relative to the Materials dir
            Path baseDir = repoRoot.getBaseDir().toAbsolutePath()
            Path relativeHtmlPath = baseDir.relativize(htmlPath).normalize()
            return relativeHtmlPath.toString()
        } else {
            return null
        }
    }

    // unused?
    String getHrefRelativeTo(Path base) {
        Objects.requireNonNull(base)
        Path href = base.relativize(this.getPath())
        return href.normalize().toString().replace('\\', '/')
    }

    // ---------------------------------------------

    @Override
    String getEncodedHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getEncodedHrefRelativeTo(rootDir)
    }

    // unused?
    String getEncodedHrefRelativeTo(Path base) {
        Objects.requireNonNull(base)
        String baseUri = base.toUri()
        String mateUri = this.getPath().toUri()
        String result = mateUri.substring(baseUri.length())
        logger_.debug("#getEncodedHrefRelativeTo baseUri=${baseUri} mateUri=${mateUri} result={result}")
        return result
    }

    /**
     * returns the identifier of the Material which is used as
     * - the name in the Bootstrap Treeview
     * - the title of Modal window
     *
     * @return
     */
    @Override
    String getIdentifier() {
        StringBuilder sb = new StringBuilder()
        if (this.getURL() != null) {
            if (this.getDirpath().toString() != '' && this.getDirpath().toString() != '.') {
                sb.append(this.getDirpath().toString())
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
            Path subpath = parent_.getTCaseDirectory().relativize(this.getPath())
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
        sb.append('"lastModified":"' + this.getLastModified().toString() + '"')
        sb.append('}}')
        return sb.toString()
    }
}
