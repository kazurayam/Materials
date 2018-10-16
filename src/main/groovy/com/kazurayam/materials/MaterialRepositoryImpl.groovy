package com.kazurayam.materials

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
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_)
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

    @Override
    void putCurrentTestSuite(TSuiteName tSuiteName) {
        this.putCurrentTestSuite(
                tSuteName,
                new TSuiteTimestamp(Helpers.now())
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
            material = new Material(subpath, url, Suffix.NULL, FileType.PNG).setParent(tCaseResult)
        } else {
            Suffix newSuffix = tCaseResult.allocateNewSuffix(subpath, url, FileType.PNG)
            logger_.debug("#resolveScreenshotPath newSuffix is ${newSuffix.toString()}")
            material = new Material(subpath, url, newSuffix, FileType.PNG).setParent(tCaseResult)
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
     * create a List<MaterialPair>オブジェクトのListを組み立てて返す。
     * 
     * MaterialRepositoryの中にはスクリーショットが下記の形式のPathに収録されている。
     *
     * ./Materials/<TSuiteName>/<TSuiteTimestamp>/<TCaseName>/xxx/xxx/sssss.png
     *
     * 指定されたtestSuiteIdと一致するサブディレクトリをスキャンする。
     * TSuiteTimestampに該当する./Reports/TSuiteName/TSuiteTimestamp の中から
     * expectedProfileに一致するTSuiteResultの集合と
     * actualProfileに一致するTSuiteResultの集合を特定する。
     * かつ各々の集まりのなかで時刻がもっとも新しいTSuiteResultを選別する。
     * これで２つのTSuiteResultが選定される。
     * expectedTSuiteResultと
     * actualTSuiteResultとを見比べてMaterialオブジェクトの組を生成する。
     * Materialのパス文字列
     * TCaseName/xxx/xxx/sssss.ext
     * 一致するもの同士をMatrialPairオブジェクトに格納し、
     * MaterialPairのListを組み立てる。それをreturnする。
     *
     * @param tSuiteName
     * @param expectedProfile
     * @param actualProfile
     * @return
     */
    @Override
    List<MaterialPair> createMaterialPairs(
            TSuiteName tSuiteName,
            ExecutionProfile expectedProfile, ExecutionProfile actualProfile) {

        List<MaterialPair> result = new ArrayList<MaterialPair>()
        List<TSuiteResult> tSuiteResults = repoRoot_.getTSuiteResults(tSuiteName)
        List<TSuiteResult> expectedTSRList = new ArrayList<TSuiteResult>()
        List<TSuiteResult> actualTSRList = new ArrayList<TSuiteResult>()

        StringBuilder sb = new StringBuilder()
        sb.append("${this.getClass().getName()}#getRecentMaterialPairs() diagnostics:\n")
        sb.append("Arguments:\n")
        sb.append("    expectedProfile: ${expectedProfile}\n")
        sb.append("    actualProfile  : ${actualProfile}\n")
        sb.append("    tSuiteName     : ${tSuiteName.getValue()}\n")
        sb.append("\n")
        sb.append("TSuiteResults found:\n")
        sb.append("    TSuiteName\tTimestamp\t\t\tProfile\t\tMatch?\n")
        for (TSuiteResult tsr : tSuiteResults) {
            sb.append("    ${tsr.getTSuiteName().getValue()}\t${tsr.getTSuiteTimestamp().format()}\t\t")
            ExecutionPropertiesWrapper epw = tsr.getExecutionPropertiesWrapper()
            if (epw != null) {
                ExecutionProfile ep = epw.getExecutionProfile() ?: 'unknown'
                sb.append("${ep}\t\t")
                if (ep == expectedProfile) {
                    sb.append("match to Expected")
                } else if (ep == actualProfile) {
                    sb.append("match to Actual")
                } else {
                    sb.append("does not match")
                }
                sb.append("\n")
            } else {
                sb.append("tsr.getExecutionPropertiesWrapper() returned null")
            }
        }
        // The following code will print message like this:
        
        // |com.kazurayam.materials.MaterialRepositoryImpl#getRecentMaterialPairs() diagnostics:
        // |Arguments:
        // |    expectedProfile: product
        // |    actualProfile  : develop
        // |    tSuiteName     : AllCorps
        // |
        // |TSuiteResults found:
        // |    TSuiteName  Timestamp           Profile     Match?
        // |    AllCorps    20181015_160850     product     match to Expected
        // |    AllCorps    20181015_160851     develop     match to Actual
        
        System.out.println(sb.toString())
        //logger_.info(sb.toString())

        // select TSuiteResult with the specified ExecutionProfile
        for (TSuiteResult tsr : tSuiteResults) {
            ExecutionPropertiesWrapper epw = tsr.getExecutionPropertiesWrapper()
            if (epw != null) {
                ExecutionProfile ep = epw.getExecutionProfile() ?: 'unknown'
                if (ep == expectedProfile) {
                    expectedTSRList.add(tsr)
                } else if (ep == actualProfile) {
                    actualTSRList.add(tsr)
                }
            } else {
                logger_.warn("#getRecentMaterialPairs could not get ExecutionPropertiesWrapper out of TestSuite '${tsr.getTSuiteName().getId()}'")
            }
        }
        
        // sort the List<TSuiteResult> by Timestamp in reverse order
        if (expectedTSRList.size() == 0) {
            logger_.debug("#getRecentMaterialPairs expectedTSRList.size() was 0 for ${tSuiteName.getValue()}:${expectedProfile}")
            return result
        } else {
            Collections.sort(expectedTSRList, Comparator.reverseOrder())
        }
        if (actualTSRList.size() == 0) {
            logger_.debug("#getRecentMaterialPairs actualTSRList.size() was 0 for ${tSuiteName.getValue()}:${actualProfile}")
            return result
        } else {
            Collections.sort(actualTSRList, Comparator.reverseOrder())
        }
        
        // pickup the LATEST TSuiteResult
        TSuiteResult expectedTSR = expectedTSRList[0]
        TSuiteResult actualTSR = actualTSRList[0]
        
        // create the instance of List<Material>
        List<Material> expMaterials = expectedTSR.getMaterials()
        List<Material> actMaterials = actualTSR.getMaterials()
        for (Material expMate : expMaterials) {
            Path expPath = expMate.getPathRelativeToTSuiteTimestamp()
            for (Material actMate : actMaterials) {
                Path actPath = actMate.getPathRelativeToTSuiteTimestamp()
                // create a MateialPair object and add it to the result
                if (expPath == actPath) {
                    result.add(new MaterialPair().setExpected(expMate).setActual(actMate))
                }
            }
        }
        return result
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