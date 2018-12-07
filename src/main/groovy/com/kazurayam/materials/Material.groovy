package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.model.MaterialFileName
import com.kazurayam.materials.model.Suffix
import com.kazurayam.materials.model.TCaseResult

final class Material implements Comparable<Material> {

    static Logger logger_ = LoggerFactory.getLogger(Material.class)

    private TCaseResult parent_
    private Path dirpath_
    private MaterialFileName materialFileName_
    private LocalDateTime lastModified_

    Material(Path dirpath, URL url, Suffix suffix, FileType fileType) {
        dirpath_ = dirpath.normalize()
        materialFileName_ = new MaterialFileName(MaterialFileName.format(url, suffix, fileType))
    }

    Material setParent(TCaseResult parent) {
        parent_ = parent
        return this
    }

    Material(TCaseResult parent, Path filePath) {
        parent_ = parent
        dirpath_ = parent.getTCaseDirectory().relativize(filePath.normalize().getParent()).normalize()
        if (dirpath_.toString() == '') {
            dirpath_ = Paths.get('.')
        }
        materialFileName_ = new MaterialFileName(filePath.getFileName().toString())
    }

    TCaseResult getParent() {
        return this.getTCaseResult()
    }

    TCaseResult getTCaseResult() {
        return parent_
    }

    URL getURL() {
        return materialFileName_.getURL()
    }

    Suffix getSuffix() {
        return materialFileName_.getSuffix()
    }

    FileType getFileType() {
        return materialFileName_.getFileType()
    }

    Path getPath() {
        if (parent_ != null) {
            return parent_.getTCaseDirectory().resolve(dirpath_).resolve(materialFileName_.getFileName()).normalize()
        } else {
            throw new IllegalStateException("parent_ is not set")
        }
    }

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

    
    
    Material setLastModified(long lastModified) {
        Instant instant = Instant.ofEpochMilli(lastModified)
        this.setLastModified(instant)
        return this
    }

    Material setLastModified(Instant lastModified) {
        lastModified_ = LocalDateTime.ofInstant(lastModified, ZoneOffset.UTC);
        return this
    }

    LocalDateTime getLastModified() {
        return lastModified_
    }

    // ---------------- business ----------------------------------------------
    Path getPathRelativeToTSuiteTimestamp() {
        Path tSuiteTimestampDir = this.getParent().getParent().getTSuiteTimestampDirectory()
        Path path = getPathRelativeTo(tSuiteTimestampDir)
        logger_.debug("#getPathRelativeToTSuiteTimestamp path=${path}")
        return path
    }

    Path getPathRelativeToRepositoryRoot() {
        Path rootDir            = this.getParent().getParent().getParent().getBaseDir()
        return getPathRelativeTo(rootDir)
    }

    private Path getPathRelativeTo(Path base) {
        Path path =  base.relativize(this.getPath())
        logger_.debug("#getPathRelativeTo base=${base} this.getPath()=${this.getPath()} path=${path}")
        return path
    }

    // --------------------------------------

    String getHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getHrefRelativeTo(rootDir)
    }

    private String getHrefRelativeTo(Path base) {
        Path href = base.relativize(this.getPath())
        return href.normalize().toString().replace('\\', '/')
    }

    // ---------------------------------------------

    String getEncodedHrefRelativeToRepositoryRoot() {
        Path rootDir = this.getParent().getParent().getParent().getBaseDir().normalize()
        return this.getEncodedHrefRelativeTo(rootDir)
    }

    private String getEncodedHrefRelativeTo(Path base) {
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
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"Material":{')
        sb.append('"url":"' + Helpers.escapeAsJsonText(this.getURL().toString())+ '",')
        sb.append('"suffix":"' + Helpers.escapeAsJsonText(this.getSuffix().toString())+ '",')
        sb.append('"fileType":"' + Helpers.escapeAsJsonText(this.getFileType().toString()) + '",')
        sb.append('"path":"' + Helpers.escapeAsJsonText(this.getPath().toString()) + '",')
        sb.append('"lastModified":"' + lastModified_.toString() + '"')
        sb.append('}}')
        return sb.toString()
    }




}
