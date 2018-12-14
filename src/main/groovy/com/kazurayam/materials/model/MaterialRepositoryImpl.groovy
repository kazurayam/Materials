package com.kazurayam.materials.model

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.DownloadsDirectoryHelper
import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.IndexerFactory
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.model.repository.RepositoryFileScanner
import com.kazurayam.materials.model.repository.RepositoryRoot

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
    private MaterialRepositoryImpl(Path baseDir) {
        //
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        baseDir_ = baseDir
        // create the directory if not present
        Helpers.ensureDirs(baseDir_)

        // load data from the local disk
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()

        // set default Material path to the "./${baseDir name}/_/_" directory
        this.putCurrentTestSuite(TSuiteName.SUITELESS, TSuiteTimestampImpl.TIMELESS)
    }

    static MaterialRepositoryImpl newInstance(Path baseDir) {
        return new MaterialRepositoryImpl(baseDir)    
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
                TSuiteTimestampImpl.newInstance(testSuiteTimestampString))
    }

    @Override
    void putCurrentTestSuite(TSuiteName tSuiteName) {
        this.putCurrentTestSuite(
                tSuiteName,
                TSuiteTimestampImpl.newInstance(Helpers.now())
        )
    }

    @Override
    void putCurrentTestSuite(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        this.putCurrentTSuiteResult(
                tSuiteName,
                tSuiteTimestamp)
    }

    private void putCurrentTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
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

    @Override
    String getCurrentTestSuiteId() {
        return currentTSuiteName_.getId()
    }

    @Override
    String getCurrentTestSuiteTimestamp() {
        return currentTSuiteTimestamp_.format()
    }

    @Override
    Path getTestCaseDirectory(String testCaseId) {
        return this.getTCaseResult(testCaseId).getTCaseDirectory()
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



    /**
     *
     */
    @Override
    Path resolveScreenshotPath(String testCaseId, URL url) {
        return this.resolveScreenshotPath(testCaseId, Paths.get('.'), url)
    }
    @Override
    Path resolveScreenshotPath(TCaseName tCaseName, URL url) {
        return this.resolveScreenshotPath(tCaseName, Paths.get('.'), url)
    }


    /**
     *
     * @param testCaseName
     * @param subpath sub-path under the TCaseResult directory
     * @param url
     * @return
     */
    @Override
    Path resolveScreenshotPath(String testCaseId, Path subpath, URL url) {
        TCaseName tCaseName = new TCaseName(testCaseId)
        return this.resolveScreenshotPath(tCaseName, subpath, url)
    }
    @Override
    Path resolveScreenshotPath(TCaseName tCaseName, Path subpath, URL url) {
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("tSuiteResult is null")
        }
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
        if (tCaseResult == null) {
            tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
            tSuiteResult.addTCaseResult(tCaseResult)
        }
        //
        Material material = tCaseResult.getMaterial(subpath, url, Suffix.NULL, FileType.PNG)
        logger_.debug("#resolveScreenshotPath material is ${material.toString()}")
        if (material == null) {
            material = MaterialImpl.newInstance(subpath, url, Suffix.NULL, FileType.PNG).setParent(tCaseResult)
        } else {
            Suffix newSuffix = tCaseResult.allocateNewSuffix(subpath, url, FileType.PNG)
            logger_.debug("#resolveScreenshotPath newSuffix is ${newSuffix.toString()}")
            material = MaterialImpl.newInstance(subpath, url, newSuffix, FileType.PNG).setParent(tCaseResult)
        }

        Helpers.ensureDirs(material.getPath().getParent())
        return material.getPath()
    }



    /**
     *
     */
    @Override
    Path resolveMaterialPath(String testCaseId, String fileName) {
        return resolveMaterialPath(testCaseId, Paths.get('.'), fileName)
    }

    @Override
    Path resolveMaterialPath(TCaseName tCaseName, String fileName) {
        return resolveMaterialPath(tCaseName, Paths.get('.'), fileName)
    }

    /**
     * returns a Path which represents a file created by the TestCase.
     * The file will be located under the subpath under the TCaseResult directory.
     * The parent directories will be created if not present.
     * 
     * @param testCaseId
     * @param subpath sub-path under TCaseResult directory
     * @param fileName
     */
    @Override
    Path resolveMaterialPath(String testCaseId, Path subpath, String fileName) {
        TCaseName tCaseName = new TCaseName(testCaseId)
        return this.resolveMaterialPath(tCaseName, subpath, fileName)
    }
    @Override
    Path resolveMaterialPath(TCaseName tCaseName, Path subpath, String fileName) {
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
        Path targetFile = tCaseResult.getTCaseDirectory().resolve(subpath).resolve(fileName).normalize()
        Files.createDirectories(targetFile.getParent())
        Helpers.touch(targetFile)
        return targetFile
    }





    /**
     *
     */
    @Override
    int deleteFilesInDownloadsDir(String fileName) {
        DownloadsDirectoryHelper.deleteSuffixedFiles(fileName)
    }

    @Override
    Path importFileFromDownloadsDir(String testCaseId, String fileName) {
        TCaseName tCaseName = new TCaseName(testCaseId)
        return this.importFileFromDownloadsDir(tCaseName, fileName)
    }
    @Override
    Path importFileFromDownloadsDir(TCaseName tCaseName, String fileName) {
        Path downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads")
        Path sourceFile = downloadsDir.resolve(fileName)
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
        Indexer indexer = IndexerFactory.newIndexer()
        indexer.setBaseDir(baseDir_)
        Path index = baseDir_.resolve('index.html')
        indexer.setOutput(index)
        indexer.execute()
        return index
    }

    
    
    /**
     * Scans the Materials directory to look up pairs of Material objects to compare.
     *
     * This method perform the following search under the &lt;projectDir&gt;/Materials directory
     * in order to identify which Material object to be included.
     *
     * 1. selects all &lt;projectDir&gt;/Materials/&lt;Test Suite Name&gt;/&lt;yyyyMMdd_hhmmss&gt; directories 
     *    with the name equals to the Test Suite Name specified as argument tSuiteName
     * 2. among them, select the directory with the 1st latest timestamp. This one is regarded as "Actual one".
     * 3. among them, select the directory with the 2nd latest timestamp. This one is regarded as "Expected one".
     * 4. please note that we do not check the profile name which was applied to each Test Suite run. also we do
     *    not check the browser type used to each Test Suite run. 
     * 5. Scan the 2 directories selected and create a List of Material objects. 2 files which have the same path
     *    under the &lt;yyyyMMdd_hhmmss&gt; directory will be packaged as a pair to form a MaterialPair object.
     * 6. A List&lt;MaterialPair&gt; is created, fulfilled and returned as the result
     *
     * @return List<MaterialPair>
     */
    @Override
    List<MaterialPair> createMaterialPairs(TSuiteName tSuiteName) {    

        List<MaterialPair> result = new ArrayList<MaterialPair>()
        
        // before sorting, create copy of the list which is unmodifiable
        List<TSuiteResult> tSuiteResults = new ArrayList<>(repoRoot_.getTSuiteResults(tSuiteName))
        
        // we expect 2 or more TSuiteResult objects with the tSuiteName
        if (tSuiteResults.size() < 2) {
            logger_.debug("#createMaterialPairs(TSuiteName \"${tSuiteName.getValue()}\").size()=${tSuiteResults.size()} < 2")
            return result
        }
        
        // sort the List<TSuiteResult> by descending order of the tSuiteTimestamp
        Collections.sort(tSuiteResults, Comparator.reverseOrder())

        // pickup the 1st LATEST TSuiteResult as "Actual one", the 2nd LATEST as "Expeted one" 
        TSuiteResult actualTSR   = tSuiteResults[0]
        TSuiteResult expectedTSR = tSuiteResults[1]

        // create the instance of List<MaterialPairs>
        List<Material> expMaterials = expectedTSR.getMaterials()
        List<Material> actMaterials = actualTSR.getMaterials()
        for (Material expMate : expMaterials) {
            Path expPath = expMate.getPathRelativeToTSuiteTimestamp()
            for (Material actMate : actMaterials) {
                Path actPath = actMate.getPathRelativeToTSuiteTimestamp()
                // create a MateialPair object and add it to the result
                if (expPath == actPath) {
                    result.add(MaterialPairImpl.newInstance().setExpected(expMate).setActual(actMate))
                }
            }
        }
        return Collections.unmodifiableList(result)
    }

    
    

    /**
     *
     * @throws IOException
     */
    @Override
    void deleteBaseDirContents() throws IOException {
        Path baseDir = this.getBaseDir()
        Helpers.deleteDirectoryContents(baseDir)
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