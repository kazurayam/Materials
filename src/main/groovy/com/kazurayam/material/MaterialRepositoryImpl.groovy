package com.kazurayam.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class MaterialRepositoryImpl implements MaterialRepository {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryImpl.class)

    private Path baseDir_
    private TSuiteName currentTSuiteName_
    private TSuiteTimestamp currentTSuiteTimestamp_
    private RepositoryRoot repoRoot_

    // ---------------------- constructors & initializer ----------------------

    /**
     *
     * @param baseDir required
     * @param tsName required
     * @param tsTimestamp required
     */
    MaterialRepositoryImpl(Path baseDir) {
        //
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        baseDir_ = baseDir
        // create the directory if not present
        Helpers.ensureDirs(baseDir_)

        // load data from the local disk
        RepositoryScanner scanner = new RepositoryScanner(baseDir_)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()

        // set default Material path to the "./${baseDir name}/_/_" directory
        this.putCurrentTestSuite(TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
    }

    /**
     * The current time now is assumed
     *
     * @param testSuiteId
     */
    @Override
    void putCurrentTestSuite(String testSuiteId) {
        this.putCurrentTestSuite(
                testSuiteId,
                Helpers.now())
    }

    @Override
    void putCurrentTestSuite(String testSuiteId, String testSuiteTimestampString) {
        this.putCurrentTSuiteResult(
                new TSuiteName(testSuiteId),
                new TSuiteTimestamp(testSuiteTimestampString))
    }

    void putCurrentTestSuite(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        this.putCurrentTSuiteResult(
                tSuiteName,
                tSuiteTimestamp)
    }

    void putCurrentTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        // memorize the specified TestSuite
        currentTSuiteName_ = tSuiteName
        currentTSuiteTimestamp_ = tSuiteTimestamp

        // add the specified TestSuite
        TSuiteResult tsr = this.getTSuiteResult(currentTSuiteName_, currentTSuiteTimestamp_)
        if (tsr == null) {
            tsr = new TSuiteResult(tSuiteName, tSuiteTimestamp).setParent(repoRoot_)
            this.addTSuiteResult(tsr)
        }
    }

    @Override
    Path getCurrentTestSuiteDirectory() {
        TSuiteResult tsr = this.getTSuiteResult(currentTSuiteName_, currentTSuiteTimestamp_)
        if (tsr != null) {
            return tsr.getTSuiteTimestampDirectory()
        }
        return null
    }

    // -------------------------- attribute getters & setters ------------------------
    @Override
    Path getBaseDir() {
        return baseDir_
    }

    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }

    TSuiteName getCurrentTSuiteName() {
        return currentTSuiteName_
    }

    TSuiteTimestamp getCurrentTSuiteTimestamp() {
        return currentTSuiteTimestamp_
    }

    // --------------------- create/add/get child nodes -----------------------

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        List<TSuiteResult> tSuiteResults = repoRoot_.getTSuiteResults()
        boolean found = false
        for (TSuiteResult tsr : tSuiteResults) {
            if (tsr == tSuiteResult) {
                found = true
            }
        }
        if (!found) {
            repoRoot_.addTSuiteResult(tSuiteResult)
        }
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        List<TSuiteResult> tSuiteResults = repoRoot_.getTSuiteResults()
        for (TSuiteResult tsr : tSuiteResults) {
            if (tsr.getTSuiteName() == tSuiteName && tsr.getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    // -------------------------- do the business -----------------------------

    Path resolveMaterial(String testCaseName, String url, FileType fileType) {
        return this.resolveMaterial(
                new TCaseName(testCaseName),
                new URL(url),
                Suffix.NULL,
                fileType)
    }


    Path resolveMaterial(String testCaseName, String url, int suffix, FileType fileType) {
        return this.resolveMaterial(
                new TCaseName(testCaseName),
                new URL(url),
                new Suffix(suffix),
                fileType)
    }

    Path resolveMaterial(TCaseName tCaseName, URL url, Suffix suffix, FileType fileType) {
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("tSuiteResult is null")
        }
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
        if (tCaseResult == null) {
            tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
            tSuiteResult.addTCaseResult(tCaseResult)
        }
        Material material = tCaseResult.getMaterial(url, suffix, fileType)
        if (material == null) {
            material = new Material(url, suffix, fileType).setParent(tCaseResult)
            // Here we create the parent directory for the material
            Helpers.ensureDirs(material.getPath().getParent())
        }
        return material.getPath()
    }


    /**
     *
     */
    @Override
    Path resolveScreenshotPath(String testCaseName, URL url) {
        return this.resolveScreenshotPath(new TCaseName(testCaseName), url)
    }

    Path resolveScreenshotPath(TCaseName tCaseName, URL url) {
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("tSuiteResult is null")
        }
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
        if (tCaseResult == null) {
            tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
            tSuiteResult.addTCaseResult(tCaseResult)
        }

        Material material = tCaseResult.getMaterial(url, Suffix.NULL, FileType.PNG)
        if (material == null) {
            material = new Material(url, Suffix.NULL, FileType.PNG).setParent(tCaseResult)
        } else {
            Suffix newSuffix = tCaseResult.allocateNewSuffix(url, FileType.PNG)
            material = new Material(url, newSuffix, FileType.PNG).setParent(tCaseResult)
        }

        Helpers.ensureDirs(material.getPath().getParent())
        return material.getPath()
    }

    @Override
    Path resolveMaterialPath(String testCaseId, String fileName) {
        return resolveMaterialPath(testCaseId, Paths.get('.'), fileName)
    }

    @Override
    Path resolveMaterialPath(String testCaseId, Path subpath, String fileName) {
        TCaseName tCaseName = new TCaseName(testCaseId)
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("tSuiteResult is null")
        }
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
        if (tCaseResult == null) {
            tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
            tSuiteResult.addTCaseResult(tCaseResult)
        }
        Helpers.ensureDirs(tCaseResult.getTCaseDirectory())
        //
        Path targetFile = tCaseResult.getTCaseDirectory().resolve(subpath).resolve(fileName)
        Helpers.touch(targetFile)
        return targetFile
    }

    @Override
    int deleteFilesInDownloadsDir(String fileName) {
        DownloadsDirectoryHelper.deleteSuffixedFiles(fileName)
    }

    @Override
    Path importFileFromDownloadsDir(String testCaseId, String fileName) {
        Path downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads")
        Path sourceFile = downloadsDir.resolve(fileName)
        TCaseName tCaseName = new TCaseName(testCaseId)
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("tSuiteResult is null")
        }
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
        if (tCaseResult == null) {
            tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
            tSuiteResult.addTCaseResult(tCaseResult)
        }
        Helpers.ensureDirs(tCaseResult.getTCaseDirectory())
        //
        Path targetFile = tCaseResult.getTCaseDirectory().resolve(fileName)
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING)
        return targetFile
    }



    /**
     * create index.html file in the current <test suite name>/<test suite timestamp>/ directory.
     * returns the Path of the index.html
     */
    @Override
    Path makeIndex() {
        Indexer indexer = new Indexer(baseDir_)
        Path index = indexer.makeIndex()
        return index
    }

    // ----------------------------- helpers ----------------------------------

    TSuiteResult getCurrentTSuiteResult() {
        if (currentTSuiteName_ != null) {
            if (currentTSuiteTimestamp_ != null) {
                TSuiteResult tsr = getTSuiteResult(currentTSuiteName_, currentTSuiteTimestamp_)
                return tsr
            } else {
                throw new IllegalStateException('currentTSuiteTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTSuiteName is not set')
        }
    }

    TCaseResult getTCaseResult(String testCaseId) {
        return this.getTCaseResult(new TCaseName(testCaseId))
    }

    TCaseResult getTCaseResult(TCaseName tCaseName) {
        if (tCaseName != null) {
            TSuiteResult tsr = this.getCurrentTSuiteResult()
            return tsr.getTCaseResult(tCaseName)
        }
        else {
            throw new IllegalStateException("currentTcName is null")
        }
    }

    @Override
    Path getTestCaseDirectory(String testCaseId) {
        return this.getTCaseResult(testCaseId).getTCaseDirectory()
    }

    // ---------------------- overriding Object properties --------------------
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"MaterialRepositoryImpl":{')
        sb.append('"baseDir":"' +
            Helpers.escapeAsJsonText(baseDir_.toString()) + '",')
        sb.append('"currentTsName":"' +
            Helpers.escapeAsJsonText(currentTSuiteName_.toString()) + '",')
        sb.append('"currentTsTimestamp":"' +
            Helpers.escapeAsJsonText(currentTSuiteTimestamp_.toString()) + '",')
        sb.append('"repoRoot":' + repoRoot_.toJson() + '')
        sb.append('}}')
        return sb.toString()
    }
}