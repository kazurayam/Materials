package com.kazurayam.materials.model

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.IndexerFactory
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.TSuiteResultIdImpl
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
        Objects.requireNonNull(baseDir)
        //
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        baseDir_ = baseDir
        // create the directory if not present
        Helpers.ensureDirs(baseDir_)

        // load data from the local disk
        this.scan()

        // set default Material path to the "./${baseDir name}/_/_" directory
        this.putCurrentTestSuite(TSuiteName.SUITELESS, TSuiteTimestampImpl.TIMELESS)
    }

    static MaterialRepositoryImpl newInstance(Path baseDir) {
        return new MaterialRepositoryImpl(baseDir)    
    }
    
    @Override
    void scan() {
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
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
                TSuiteTimestamp.newInstance(testSuiteTimestampString))
    }

    @Override
    void putCurrentTestSuite(TSuiteName tSuiteName) {
        this.putCurrentTestSuite(
                tSuiteName,
                TSuiteTimestamp.newInstance(Helpers.now())
        )
    }

    @Override
    void putCurrentTestSuite(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        this.putCurrentTSuiteResult(
                tSuiteName,
                tSuiteTimestamp)
    }
    
    @Override
    void putCurrentTestSuite(TSuiteResultId tSuiteResultId) {
        this.putCurrentTSuiteResult(
            tSuiteResultId.getTSuiteName(),
            tSuiteResultId.getTSuiteTimestamp())
    }

    /**
     * 
     * @param tSuiteName
     * @param tSuiteTimestamp
     */
    private void putCurrentTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        Objects.requireNonNull(tSuiteName)
        Objects.requireNonNull(tSuiteTimestamp)
        
        // memorize the specified TestSuite
        currentTSuiteName_ = tSuiteName
        currentTSuiteTimestamp_ = tSuiteTimestamp

        // add the specified TestSuite
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(currentTSuiteName_, currentTSuiteTimestamp_)
        TSuiteResult tsr = this.getTSuiteResult(tsri)
        
        // if a TSuiteRusule of tSuiteName/tSuiteTimestamp is NOT found,
        // then create new one
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
        Objects.requireNonNull(testCaseId)
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
        Objects.requireNonNull(tSuiteResult)
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

    /*
    @Override
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(tSuiteName, tSuiteTimestamp)
        return this.getTSuiteResult(tsri)
    }
    */
    
    @Override
    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId) {
        Objects.requireNonNull(tSuiteResultId)
        TSuiteName tSuiteName = tSuiteResultId.getTSuiteName()
        TSuiteTimestamp tSuiteTimestamp = tSuiteResultId.getTSuiteTimestamp()
        List<TSuiteResult> tSuiteResults = repoRoot_.getTSuiteResults()
        for (TSuiteResult tsr : tSuiteResults) {
            if (tsr.getTSuiteResultId().getTSuiteName().equals(tSuiteName) && 
                tsr.getTSuiteResultId().getTSuiteTimestamp().equals(tSuiteTimestamp)) {
                return tsr
            }
        }
        return null
    }
    
    @Override
    List<TSuiteResultId> getTSuiteResultIdList(TSuiteName tSuiteName) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        List<TSuiteResultId> list = new ArrayList<TSuiteResultId>()
        for (TSuiteResult subject : repoRoot_.getTSuiteResults()) {
            if (subject.getTSuiteResultId().getTSuiteName().equals(tSuiteName)) {
                list.add(subject.getTSuiteResultId())
            }
        }
        return list
    }
    
    @Override
    List<TSuiteResultId> getTSuiteResultIdList() {
        List<TSuiteResultId> list = new ArrayList<TSuiteResultId>()
        for (TSuiteResult subject : repoRoot_.getTSuiteResults()) {
            list.add(subject.getTSuiteResultId())
        }
        return list
    }
    
    @Override
    List<TSuiteResult> getTSuiteResultList(List<TSuiteResultId> tSuiteResultIdList) {
        Objects.requireNonNull(tSuiteResultIdList, "tSuiteResultIdList must not be null")
        List<TSuiteResult> list = new ArrayList<TSuiteResult>()
        for (TSuiteResultId subject : tSuiteResultIdList) {
            for (TSuiteResult tsr : repoRoot_.getTSuiteResults()) {
                if (tsr.getTSuiteResultId().getTSuiteName().equals(subject.getTSuiteName()) &&
                    tsr.getTSuiteResultId().getTSuiteTimestamp().equals(subject.getTSuiteTimestamp())) {
                    list.add(tsr)
                }
            }
        }
        return list
    }
    
    @Override
    List<TSuiteResult> getTSuiteResultList() {
        return repoRoot_.getTSuiteResults()
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
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
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
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
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

        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        
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
        List<Material> expMaterials = expectedTSR.getMaterialList()
        List<Material> actMaterials = actualTSR.getMaterialList()
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

    // -------------------- House cleaning -----------------------------------

    /**
     *
     * @throws IOException
     */
    @Override
    void deleteBaseDirContents() throws IOException {
        Path baseDir = this.getBaseDir()
        Helpers.deleteDirectoryContents(baseDir)
        this.scan()
    }

    /**
     * delete Material files and sub directories located in the tSuiteName+tSuiteTimestamp dir.
     * The tSuiteName directory is removed.
     * All tSuiteTimestamp directories under the tSuiteName are removed of cource.
     *
     * @param tSuiteName
     * @return number of Material files removed, excluding deleted sub directories
     * @throws IOException
     */
    @Override
    int clear(TSuiteResultId tSuiteResultId) throws IOException {
        Objects.requireNonNull(tSuiteResultId,"tSuiteResultId must not be null")
        TSuiteName tSuiteName = tSuiteResultId.getTSuiteName()
        TSuiteTimestamp tSuiteTimestamp = tSuiteResultId.getTSuiteTimestamp()
        Path tstDir = this.getBaseDir().resolve(tSuiteName.getValue()).resolve(tSuiteTimestamp.format())
        int count = Helpers.deleteDirectory(tstDir)
        this.scan()
        return count
    }

    @Override
    int clear(List<TSuiteResultId> tSuiteResultIdList) throws IOException {
        int count = 0
        for (TSuiteResultId tsri : tSuiteResultIdList) {
            count += this.clear(tsri)
        }
        return count
    }
    
    /**
     * delete Material files and sub directories located in the tSuiteName+tSuiteTimestamp dir.
     * The tSuiteName directory is removed.
     * All tSuiteTimestamp directories under the tSuiteName are removed of cource.
     *
     * @param tSuiteName
     * @return number of Material files removed, excluding deleted sub directories
     * @throws IOException
     */
    @Override
    int clear(TSuiteName tSuiteName) throws IOException {
        Path tsnDir = this.getBaseDir().resolve(tSuiteName.getValue())
        int count = Helpers.deleteDirectory(tsnDir)
        this.scan()
        return count
    }


    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }

    // ----------------------------- getters ----------------------------------

    TSuiteResult getCurrentTSuiteResult() {
        if (currentTSuiteName_ != null) {
            if (currentTSuiteTimestamp_ != null) {
                TSuiteResultId tsri = TSuiteResultId.newInstance(currentTSuiteName_, currentTSuiteTimestamp_)
                TSuiteResult tsr = this.getTSuiteResult(tsri)
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
            throw new IllegalStateException("tCaseName is null")
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