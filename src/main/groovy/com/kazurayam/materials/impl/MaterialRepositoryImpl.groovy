package com.kazurayam.materials.impl

import com.kazurayam.materials.*
import com.kazurayam.materials.metadata.InvokedMethodName
import com.kazurayam.materials.metadata.MaterialMetadata
import com.kazurayam.materials.metadata.MaterialMetadataBundle
import com.kazurayam.materials.metadata.MaterialMetadataImpl
import com.kazurayam.materials.model.Suffix

import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.TreeTrunkScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

final class MaterialRepositoryImpl implements MaterialRepository {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryImpl.class)

    /**
     * path of the Materials directory
     */
    private Path baseDir_

    // set default Material path to the "./${baseDir name}/_/_/_" directory
    private TSuiteName currentTSuiteName_ = TSuiteName.SUITELESS
    private TExecutionProfile currentTExecutionProfile_ = TExecutionProfile.UNUSED
    private TSuiteTimestamp currentTSuiteTimestamp_ = TSuiteTimestamp.TIMELESS
    private boolean alreadyMarked_ = false

    private RepositoryRoot repoRoot_
    
    private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    // ---------------------- constructors & initializer ----------------------

    private MaterialRepositoryImpl(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        //
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        baseDir_ = baseDir
        vtLogger_ = new VisualTestingLoggerDefaultImpl()

        // create the Materials directory if not present
        Helpers.ensureDirs(baseDir_)

        // load data from the local disk
        this.scan()
        
    }

    static MaterialRepositoryImpl newInstance(Path baseDir) {
        return new MaterialRepositoryImpl(baseDir)    
    }



    // ================================================================
    //
    //     implementing MaterialRepository interface
    //
    // ----------------------------------------------------------------
    @Override
    void scan() {
        TreeTrunkScanner scanner = new TreeTrunkScanner(baseDir_)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }

    @Override
    void markAsCurrent(String testSuiteId,
                       String executionProfile,
                       String testSuiteTimestamp) {
        this.markTSuiteResultAsCurrent(
                new TSuiteName(testSuiteId),
                new TExecutionProfile(executionProfile),
                new TSuiteTimestamp(testSuiteTimestamp))
    }

    @Override
    void markAsCurrent(TSuiteName tSuiteName,
                       TExecutionProfile tExecutionProfile,
                       TSuiteTimestamp tSuiteTimestamp) {
        this.markTSuiteResultAsCurrent(
                tSuiteName,
                tExecutionProfile,
                tSuiteTimestamp)
    }
    
    @Override
    void markAsCurrent(TSuiteResultId tSuiteResultId) {
        this.markTSuiteResultAsCurrent(
                tSuiteResultId.getTSuiteName(),
                tSuiteResultId.getTExecutionProfile(),
                tSuiteResultId.getTSuiteTimestamp()
        )
    }

    /**
     * 
     * @param tSuiteName
     * @param tSuiteTimestamp
     */
    private void markTSuiteResultAsCurrent(TSuiteName tSuiteName,
                                           TExecutionProfile tExecutionProfile,
                                           TSuiteTimestamp tSuiteTimestamp) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
        
        // memorize this specified TestSuite as the current one
        currentTSuiteName_ = tSuiteName
        currentTExecutionProfile_ = tExecutionProfile
        currentTSuiteTimestamp_ = tSuiteTimestamp

        // ensure a directory for the TSuiteResult
        ensureTSuiteResultPresent(tSuiteName, tExecutionProfile, tSuiteTimestamp)
        
        // make the status easily looked up
        alreadyMarked_ = true
    }
    
    
    @Override
    boolean isAlreadyMarked() {
        return alreadyMarked_
    }


    @Override
    TSuiteResult ensureTSuiteResultPresent(String testSuiteName,
                                           String executionProfile,
                                           String testSuiteTimestamp) {
        return this.ensureTSuiteResultPresent(
                new TSuiteName(testSuiteName),
                new TExecutionProfile(executionProfile),
                TSuiteTimestamp.newInstance(testSuiteTimestamp))
    }
    
    @Override
    TSuiteResult ensureTSuiteResultPresent(TSuiteResultId tSuiteResultId) {
        return this.ensureTSuiteResultPresent(
                tSuiteResultId.getTSuiteName(),
                tSuiteResultId.getTExecutionProfile(),
                tSuiteResultId.getTSuiteTimestamp())
    }
    
    @Override
    TSuiteResult ensureTSuiteResultPresent(TSuiteName tSuiteName,
                                           TExecutionProfile tExecutionProfile,
                                           TSuiteTimestamp tSuiteTimestamp) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
        
        // add the specified TestSuite
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(tSuiteName, tExecutionProfile, tSuiteTimestamp)
        TSuiteResult tsr = this.getTSuiteResult(tsri)
        
        // if a TSuiteResult of tSuiteName/tExecutionProfile/tSuiteTimestamp is NOT found in the directory,
        // then create new object and create a directory for it 
        if (tsr == null) {
            tsr = TSuiteResult.newInstance(tSuiteName, tExecutionProfile, tSuiteTimestamp).setParent(repoRoot_)
            this.addTSuiteResult(tsr)
            tsr.createDirectories()
        }
        //logger_.debug("#putCurrentTSuiteResult pathResolutionLogBundleAt_ is ${pathResolutionLogBundleAt_.toString()}")
        //logger_.debug("#putCurrentTSuiteResult Files.exists(pathResolutionLogBundleAt_) returned ${Files.exists(pathResolutionLogBundleAt_)}")
        
        return tsr
    }


    @Override
    Path getBaseDir() {
        return baseDir_
    }

    @Override
    Path getCurrentTestSuiteDirectory() {
        TSuiteResultId tsri =
                TSuiteResultId.newInstance(
                        currentTSuiteName_,
                        currentTExecutionProfile_,
                        currentTSuiteTimestamp_)
        TSuiteResult tsr = this.getTSuiteResult(tsri)
        if (tsr != null) {
            return tsr.getTSuiteTimestampDirectory()
        }
        return null
    }

    @Override
    String getCurrentTestSuiteId() {
        return currentTSuiteName_.getId()
    }

    @Override
    String getCurrentExecutionProfile() {
        return currentTExecutionProfile_.getName()
    }

    @Override
    String getCurrentTestSuiteTimestamp() {
        return currentTSuiteTimestamp_.format()
    }

    @Override
    Path getTestCaseDirectory(String testCaseId) {
        Objects.requireNonNull(testCaseId)
        TCaseResult tCaseResult = this.getTCaseResult(testCaseId)
        if (tCaseResult != null) {
            return tCaseResult.getTCaseDirectory()
        } else
            return null
    }


    @Override
    MaterialMetadataBundle findMaterialMetadataBundleOfCurrentTSuite() {
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName(this.getCurrentTestSuiteId()),
                new TExecutionProfile(this.getCurrentExecutionProfile()),
			    new TSuiteTimestamp(this.getCurrentTestSuiteTimestamp()))
        
		TSuiteResult currentTsr = this.getTSuiteResult(tsri)
		Path mmbPath = this.locateMaterialMetadataBundle(currentTsr)
		if (Files.exists(mmbPath)) {
			return MaterialMetadataBundle.deserialize(mmbPath)
		} else {
			return null
		}
    }
	
	@Override
	boolean hasMaterialMetadataBundleOfCurrentTSuite() {
		MaterialMetadataBundle mmb = this.findMaterialMetadataBundleOfCurrentTSuite()
		return (mmb != null)
	}

    @Override
    boolean printVisitedURLsAsMarkdown(Writer writer) {
        if (this.hasMaterialMetadataBundleOfCurrentTSuite()) {
            MaterialMetadataBundle mmb = this.findMaterialMetadataBundleOfCurrentTSuite()
            mmb.serializeAsMarkdown(writer)
            return true
        } else {
            logger_.info("no MaterialMetadataBundle of the current TSuite")
            return false
        }
    }

    @Override
    boolean printVisitedURLsAsTSV(Writer writer) {
        if (this.hasMaterialMetadataBundleOfCurrentTSuite()) {
            MaterialMetadataBundle mmb = this.findMaterialMetadataBundleOfCurrentTSuite()
            mmb.serializeAsTSV(writer)
            return true
        } else {
            logger_.info("no MaterialMetadataBundle of the current TSuite")
            return false
        }
    }

    private MaterialMetadataBundle recordMaterialMetadata(TSuiteResult tSuiteResult,
                                                          MaterialMetadata materialMetadata) {
        Path bundleFile = this.locateMaterialMetadataBundle(tSuiteResult)
        MaterialMetadataBundle mmBundle = new MaterialMetadataBundle()
        if (Files.exists(bundleFile)) {
            mmBundle = MaterialMetadataBundle.deserialize(bundleFile)
        }
        mmBundle.add(materialMetadata)
        //
        OutputStream os = new FileOutputStream(bundleFile.toFile())
        Writer writer = new OutputStreamWriter(os, "UTF-8")
        mmBundle.serialize(writer)
        writer.close()
        
        return mmBundle
    }
    
    /**
     *
     */
    @Override
    Path resolveScreenshotPath(String testCaseId, URL url,
                               MaterialDescription description = MaterialDescription.EMPTY) {
        return this.resolveScreenshotPath(testCaseId, '', url, description)
    }

    @Override
    Path resolveScreenshotPath(TCaseName tCaseName, URL url,
                               MaterialDescription description = MaterialDescription.EMPTY) {
        return this.resolveScreenshotPath(tCaseName, '', url, description)
    }

    @Override
    Path resolveScreenshotPath(String testCaseId, String subPath, URL url,
                               MaterialDescription description = MaterialDescription.EMPTY) {
        TCaseName tCaseName = new TCaseName(testCaseId)
        return this.resolveScreenshotPath(tCaseName, subPath, url, description)
    }

    @Override
    Path resolveScreenshotPath(TCaseName tCaseName, String subPath, URL url,
                               MaterialDescription description = MaterialDescription.EMPTY) {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        Objects.requireNonNull(subPath, "subPath must not be null")
        Objects.requireNonNull(url, "url must not be null")
        Objects.requireNonNull(description, "description must not be null")
        if ( !this.isAlreadyMarked() ) {
			// in case when MaterialRepository is called by a Test Case outside a Test Suite so that Materials/_/_ dir is required
			this.markAsCurrent(currentTSuiteName_, currentTSuiteTimestamp_)
			this.ensureTSuiteResultPresent(currentTSuiteName_, currentTSuiteTimestamp_)
		}
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("tSuiteResult is null")
        }

        TCaseResult tCaseResult = tSuiteResult.ensureTCaseResultPresent(tCaseName)

        // check if a Material is already there
        Material material = tCaseResult.getMaterial(subPath, url, Suffix.NULL, FileType.PNG)
        logger_.debug("#resolveScreenshotPath material is ${material.toString()}")
        if (material == null) {
            // not there, create new one
            material = new MaterialImpl(tCaseResult, subPath, url, Suffix.NULL, FileType.PNG)
        } else {
            // "file.png" is already there, allocate new file name like "file(2).png" 
            Suffix newSuffix = tCaseResult.allocateNewSuffix(subPath, url, FileType.PNG)
            logger_.debug("#resolveScreenshotPath newSuffix is ${newSuffix.toString()}")
            material = new MaterialImpl(tCaseResult, subPath, url, newSuffix, FileType.PNG)
        }
        Helpers.ensureDirs(material.getPath().getParent())
        
        // log resolution of a Material path
        MaterialMetadata metadata =
            new MaterialMetadataImpl(
                    InvokedMethodName.RESOLVE_SCREENSHOT_PATH,
                    tCaseName,
                    material.getHrefRelativeToRepositoryRoot(),
                    description)
        metadata.setSubPath(subPath)
        metadata.setUrl(url)

        //
        MaterialMetadataBundle bundle = this.recordMaterialMetadata(tSuiteResult, metadata)
        
        return material.getPath().normalize()
    }


    /**
     * 
     */
    @Override
    Path resolveScreenshotPathByURLPathComponents(String testCaseId,
                                                  URL url,
                                                  int startingDepth = 0,
                                                  String defaultName = 'default',
                                                  MaterialDescription description = MaterialDescription.EMPTY) {
        return this.resolveScreenshotPathByURLPathComponents(testCaseId, '', url, startingDepth, defaultName, description)
    }


    @Override
    Path resolveScreenshotPathByURLPathComponents(TCaseName tCaseName,
                                                  URL url,
                                                  int startingDepth = 0,
                                                  String defaultName = 'default',
                                                  MaterialDescription description = MaterialDescription.EMPTY) {
        return this.resolveScreenshotPathByURLPathComponents(tCaseName, '', url, startingDepth, defaultName, description)
    }


    @Override
    Path resolveScreenshotPathByURLPathComponents(String testCaseId,
                                                  String subPath,
                                                  URL url,
                                                  int startingDepth = 0,
                                                  String defaultName = 'default',
                                                  MaterialDescription description = MaterialDescription.EMPTY) {
        TCaseName tCaseName = new TCaseName(testCaseId)
        return this.resolveScreenshotPathByURLPathComponents(tCaseName, subPath, url, startingDepth, defaultName, description)
    }


    @Override
    Path resolveScreenshotPathByURLPathComponents(TCaseName tCaseName,
                                                  String subPath,
                                                  URL url,
                                                  int startingDepth = 0,
                                                  String defaultName = 'default',
                                                  MaterialDescription description = MaterialDescription.EMPTY) {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        Objects.requireNonNull(subPath, "subPath must not be null")
        Objects.requireNonNull(url, "url must not be null")
        Objects.requireNonNull(description, "description must not be null")
        if (startingDepth < 0) {
            throw new IllegalArgumentException("startingDepth=${startingDepth} must not be negative")
        }
        if (url.getPath() == null || url.getPath() == '') {
            return resolveScreenshotPath(tCaseName, subPath, url)
        }

        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("getCurrentTSuiteResult() returned null")
        }

        TCaseResult tCaseResult = tSuiteResult.ensureTCaseResultPresent(tCaseName)

        Helpers.ensureDirs(tCaseResult.getTCaseDirectory())

        String fileName = resolveFileNameByURLPathComponents(url, startingDepth, defaultName)
        Material material = new MaterialImpl(tCaseResult,
                tCaseResult.getTCaseDirectory().resolve(subPath).resolve(fileName))

        // see https://github.com/kazurayam/Materials/issues/28
        // We will check the absolute path length of the material file.
        // Ff the length exceed 255, Windows path length limit, then shorten the file name
        // by filtering '%26' (& URL encoded) and '%3D' (= URL encoded) to '' and
        // recreate the material
        if (material.getPath().toAbsolutePath().toString().length() > 255) {
            fileName =
                    fileName.replace('%26', '')
                            .replace('%3D', '')
            material = new MaterialImpl(tCaseResult,
                    tCaseResult.getTCaseDirectory().resolve(subPath).resolve(fileName))
        }
        //
        Files.createDirectories(material.getPath().getParent())
        //Helpers.touch(material.getPath())
        
        // log resolution of a Material path
        MaterialMetadata metadata =
            new MaterialMetadataImpl(
                    InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS,
                    tCaseName,
                    material.getHrefRelativeToRepositoryRoot(),
                    description)
        metadata.setSubPath(subPath)
        metadata.setUrl(url)

        //
        MaterialMetadataBundle bundle = this.recordMaterialMetadata(tSuiteResult, metadata)
        
        return material.getPath().normalize()
    }
    
    /**
     * 
     * @param url
     * @param startingDepth
     * @param defaultName
     * @return
     */
    protected String resolveFileNameByURLPathComponents(URL url,
                                                        int startingDepth = 0,
                                                        String defaultName) {
        StringBuilder sb = new StringBuilder()
        Path p = Paths.get(url.getPath())
        //logger_.debug("#resolveScreenshotPathByURLPathComponents p=${p.toString()}")
        //logger_.debug("#resolveScreenshotPathByURLPathComponents p.getNameCount()=${p.getNameCount()}")
        //for (int i=0; i < p.getNameCount(); i++) {
            //logger_.debug("#resolveScreenshotPathByURLPathComponents p.getName(${i})=${p.getName(i)}")
        //}
        if (startingDepth < p.getNameCount()) {
            for (int i = 0; i < p.getNameCount(); i++) {
                if (startingDepth <= i) {
                    if (sb.length() > 0) {
                        sb.append('%2F')
                    }
                    sb.append(URLEncoder.encode(p.getName(i).toString(), 'utf-8'))
                }
            }
        } else {
            sb.append(defaultName)
        }
        if (url.getQuery() != null) {
            sb.append(URLEncoder.encode('?', 'utf-8'))
            sb.append(URLEncoder.encode(url.getQuery(), 'utf-8'))
        }
        if (url.getRef() != null) {
            sb.append(URLEncoder.encode('#', 'utf-8'))
            sb.append(URLEncoder.encode(url.getRef(), 'utf-8'))
        }
        sb.append('.png')
        String fileName = sb.toString()
        return fileName
    }


    /**
     * given 'corp=abcd&foo=bar' as queryString, return ['corp':'abcd','foo':'bar']
     */
    protected static final Map<String, String> parseQuery(String query) {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return query_pairs;
    }


    /**
     *
     */
    @Override
    Path resolveMaterialPath(String testCaseId,
                             String fileName,
                             MaterialDescription description = MaterialDescription.EMPTY) {
        return resolveMaterialPath(testCaseId, '', fileName, description)
    }

    @Override
    Path resolveMaterialPath(TCaseName tCaseName,
                             String fileName,
                             MaterialDescription description = MaterialDescription.EMPTY) {
        return resolveMaterialPath(tCaseName, '', fileName, description)
    }

    @Override
    Path resolveMaterialPath(String testCaseId,
                             String subPath,
                             String fileName,
                             MaterialDescription description = MaterialDescription.EMPTY) {
        TCaseName tCaseName = new TCaseName(testCaseId)
        return this.resolveMaterialPath(tCaseName, subPath, fileName, description)
    }
    
    @Override
    Path resolveMaterialPath(TCaseName tCaseName,
                             String subPath,
                             String fileName,
                             MaterialDescription description = MaterialDescription.EMPTY) {
        Objects.requireNonNull(tCaseName, "tCaseName must not be null")
        Objects.requireNonNull(subPath, "subPath must not be null")
        Objects.requireNonNull(fileName, "fileName must not be null")
        Objects.requireNonNull(description, "description must not be null")
        if ( !this.isAlreadyMarked() ) {
            // in case when MaterialRepository is called by a Test Case outside a Test Suite so that Materials/_/_ dir is required
            this.markAsCurrent(currentTSuiteName_,
                    currentTExecutionProfile_,
                    currentTSuiteTimestamp_)

            this.ensureTSuiteResultPresent(currentTSuiteName_,
                    currentTExecutionProfile_,
                    currentTSuiteTimestamp_)
        }
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            throw new IllegalStateException("tSuiteResult is null")
        }

        TCaseResult tCaseResult = tSuiteResult.ensureTCaseResultPresent(tCaseName)
        Helpers.ensureDirs(tCaseResult.getTCaseDirectory())
        
        //logger_.debug("#resolveMaterialPath tCaseResult=${tCaseResult}")
        Material material = new MaterialImpl(tCaseResult, tCaseResult.getTCaseDirectory().resolve(subPath).resolve(fileName))
        
        //
        //logger_.debug("#resolveMaterialPath")
        //logger_.debug("#resolveMaterialPath material=${material}")
        //logger_.debug("#resolveMaterialPath material.getParent()=${material.getParent()}")
        //logger_.debug("#resolveMaterialPath material.getPath()=${material.getPath()}")
        //logger_.debug("#resolveMaterialPath material.getPath().getParent()=${material.getPath().getParent()}")
        
        Files.createDirectories(material.getPath().getParent())
        //Helpers.touch(material.getPath())
        
        // log resolution of a Material path
        MaterialMetadata metadata =
            new MaterialMetadataImpl(
                    InvokedMethodName.RESOLVE_MATERIAL_PATH,
                    tCaseName,
                    material.getHrefRelativeToRepositoryRoot(),
                    description)
        metadata.setSubPath(subPath)
        metadata.setFileName(fileName)

        MaterialMetadataBundle bundle = this.recordMaterialMetadata(tSuiteResult, metadata)
        //
        return material.getPath().normalize()
    }


    /**
     * Given the path of <pre>Materials</pre> directory = the base directory of the MaterialsRepository class instance, 
     * create <pre>&lt;Materials directory&gt;/index.html</pre> file based on the current content of the Materials directory
     * while naively assuming the <pre>&lt;Materials directory&gt;/../Reports</pre> is also available.
     * 
     * @returns the Path of the index.html
     */
    @Override
    Path makeIndex() {
        Indexer indexer = IndexerFactory.newIndexer()
        indexer.setBaseDir(baseDir_)
        Path reportsDir = baseDir_.resolve('..').resolve('Reports')  // a naive assumption, may break 
        indexer.setReportsDir(reportsDir)
        Path index = baseDir_.resolve('index.html')
        indexer.setOutput(index)
        if (vtLogger_ != null) {
            indexer.setVisualTestingLogger(vtLogger_)
        }
        vtLogger_.info(this.class.getSimpleName() + "#makeIndex baseDir is ${baseDir_}")
        vtLogger_.info(this.class.getSimpleName() + "#makeIndex reportsDir is ${reportsDir}")
        vtLogger_.info(this.class.getSimpleName() + "#makeIndex index is ${index}")
        indexer.execute()
        return index
    }


    /**
     * Scans the Materials directory to look up pairs of Material objects to compare
     * for the Twins mode.
     */
    @Override
    MaterialPairs createMaterialPairsForTwinsMode(TSuiteName tSuiteName) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")

        // before sorting, we create a copy of the list which is unmodifiable
        List<TSuiteResult> tSuiteResults = new ArrayList<>(
                repoRoot_.getTSuiteResultList(tSuiteName))

        // we expect 2 or more TSuiteResult objects with the given tSuiteName
        if (tSuiteResults.size() == 0) {
            logger_.warn("#createMaterialPairs(TSuiteName \"${tSuiteName.getValue()}\").size()=${tSuiteResults.size()} == 0")
            throw new IllegalStateException("No sub directory found under ${tSuiteName.getValue()} in ${repoRoot_.getBaseDir().toString()}.")
        } else if (tSuiteResults.size() == 1) {
            logger_.warn("#createMaterialPairs(TSuiteName \"${tSuiteName.getValue()}\").size()=${tSuiteResults.size()} == 1")
            throw new IllegalStateException("Only 1 sub directory found under ${tSuiteName.getValue()} in ${repoRoot_.getBaseDir().toString()}."
                    + " Chronos mode requires 2 sub direstories under ${tSuiteName.getValue()}."
                    + " Don\'t get surprised. Just execute the Chronos test suite again."
                    + " Possibly Chronos mode will work fine next time.")
        } else {
            vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() tSuiteResults.size() is ${tSuiteResults.size()}")
        }

        // sort the List<TSuiteResult> by descending order of the tSuiteTimestamp
        Collections.sort(tSuiteResults, Comparator.reverseOrder())

        // pickup the 1st LATEST TSuiteResult as "Actual one", the 2nd LATEST as "Expeted one"
        TSuiteResult actualTSR   = tSuiteResults[0]
        TSuiteResult expectedTSR = tSuiteResults[1]
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() actualTSR is ${actualTSR.getId().toString()}")
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() actualTSR.getMaterialList().size() is ${actualTSR.getMaterialList().size()}")
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() expectedTSR IS ${expectedTSR.getId().toString()}")
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() expectedTSR.getMaterialList().size() is ${expectedTSR.getMaterialList().size()}")

        // the result to be returned
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTSR, actualTSR)

        // fill in entries into the MaterialPairs object
        for (Material expectedMaterial : expectedTSR.getMaterialList()) {
            mps.putExpectedMaterial(expectedMaterial)
        }
        for (Material actualMaterial : actualTSR.getMaterialList()) {
            mps.putActualMaterial(actualMaterial)
        }
        return mps
    }

    /**
     * Scans the Materials directory to look up pairs of Material objects to compare
     * for the Chronos mode.
     */
    @Override
    MaterialPairs createMaterialPairsForChronosMode(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile) {
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")

        // before sorting, we create a copy of the list which is unmodifiable
        List<TSuiteResult> tSuiteResults = new ArrayList<>(
                repoRoot_.getTSuiteResultList(tSuiteName, tExecutionProfile))
        
        // we expect 2 or more TSuiteResult objects with the tSuiteName+tExecutionProfile
        if (tSuiteResults.size() == 0) {
            logger_.warn("#createMaterialPairs(TSuiteName \"${tSuiteName.getValue()}\", TExecutionProfile \"${tExecutionProfile.getName()}\").size()=${tSuiteResults.size()} == 0")
            throw new IllegalStateException("No sub directory found under ${tSuiteName.getValue()},${tExecutionProfile.getName()} in ${repoRoot_.getBaseDir().toString()}.")
        } else if (tSuiteResults.size()== 1) {
            logger_.warn("#createMaterialPairs(TSuiteName \"${tSuiteName.getValue()}\", TExecutionProfile \"${tExecutionProfile.getName()}\").size()=${tSuiteResults.size()} == 1")
            throw new IllegalStateException("Only 1 sub directory found under ${tSuiteName.getValue()},${tExecutionProfile.getName()} in ${repoRoot_.getBaseDir().toString()}."
                + " Chronos mode requires 2 sub direstories under ${tSuiteName.getValue()}."
                + " Don\'t get surprized. Just execute the chronos test suite again."
                + " Possibly Chronos mode will work fine next time.")
        } else {
            vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() tSuiteResults.size() is ${tSuiteResults.size()}")
        }
        
        // sort the List<TSuiteResult> by descending order of the tSuiteTimestamp
        Collections.sort(tSuiteResults, Comparator.reverseOrder())

        // pickup the 1st LATEST TSuiteResult as "Actual one", the 2nd LATEST as "Expeted one" 
        TSuiteResult actualTSR   = tSuiteResults[0]
        TSuiteResult expectedTSR = tSuiteResults[1]
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() actualTSR is ${actualTSR.getId().toString()}")
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() actualTSR.getMaterialList().size() is ${actualTSR.getMaterialList().size()}")
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() expectedTSR IS ${expectedTSR.getId().toString()}")
        vtLogger_.info("MaterialRepositoryImpl#createMaterialPairs() expectedTSR.getMaterialList().size() is ${expectedTSR.getMaterialList().size()}")
        
        // the result to be returned
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTSR, actualTSR)
        
        // fill in entries into the MaterialPairs object
        for (Material expectedMaterial : expectedTSR.getMaterialList()) {
            mps.putExpectedMaterial(expectedMaterial)
        }
        for (Material actualMaterial : actualTSR.getMaterialList()) {
            mps.putActualMaterial(actualMaterial)
        }
        return mps
    }

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
    int clear(TSuiteResultId tSuiteResultId, boolean scan = true) throws IOException {
        Objects.requireNonNull(tSuiteResultId,"tSuiteResultId must not be null")
        Path tstDir = this.getBaseDir()
                .resolve(tSuiteResultId.getTSuiteName().getValue())
                .resolve(tSuiteResultId.getTExecutionProfile().getNameInPathSafeChars())
                .resolve(tSuiteResultId.getTSuiteTimestamp().format())
        int count = Helpers.deleteDirectory(tstDir)
        if (scan) {
            this.scan()
        }
        return count
    }

    @Override
    int clearRest(TSuiteResultId exceptThis,
                  boolean scan = true) throws IOException {
        Objects.requireNonNull(exceptThis,
                "exceptThisTSuiteResultId must not be null")
        int count = 0
        List<TSuiteResultId> list = this.getTSuiteResultIdList()
        for (tsri in list) {
            if (tsri != exceptThis) {
                count += this.clear(tsri, false)
            }
        }
        if (scan) {
            this.scan()
        }
        return count
    }

    @Override
    int clear(List<TSuiteResultId> tSuiteResultIdList) throws IOException {
        int count = 0
        for (TSuiteResultId tsri : tSuiteResultIdList) {
            count += this.clear(tsri, false)
        }
        this.scan()
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

    /**
     * @return Path of material-metadata-bundl.json file under the directory of the TSuiteResult
     */
    @Override
    Path locateMaterialMetadataBundle(TSuiteResult tSuiteResult) {
        return tSuiteResult.getTSuiteTimestampDirectory().resolve(
                MaterialMetadataBundle.SERIALIZED_FILE_NAME)
    }

	
	
	@Override
    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }


    @Override
    TSuiteResult getCurrentTSuiteResult() {
		TSuiteResultId tSuiteResultId =
                TSuiteResultId.newInstance(currentTSuiteName_,
                        currentTExecutionProfile_, currentTSuiteTimestamp_)
        TSuiteResult tSuiteResult = this.getTSuiteResult(tSuiteResultId)
        return tSuiteResult
    }
    
    @Override
    long getSize() {
        long size = 0
        for (TSuiteResult tsr : this.getTSuiteResultList()) {
            size += tsr.getSize()
        }
        return size
    }
    
    @Override
    Set<Path> getSetOfMaterialPathRelativeToTSuiteTimestamp(TSuiteName tSuiteName,
                                                            TExecutionProfile tExecutionProfile) {
        Set<Path> set = new TreeSet<Path>()
        List<TSuiteResultId> idList = this.getTSuiteResultIdList(tSuiteName, tExecutionProfile)
        for (id in idList) {
            TSuiteResult tSuiteResult = this.getTSuiteResult(id)
            List<Material> materialList = tSuiteResult.getMaterialList()
            for (Material material: materialList) {
                set.add(material.getPathRelativeToTSuiteTimestamp().normalize())
            }
        } 
        return set
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

    /**
     * @return a TCaseResult object with tCaseName inside
     * the tSuiteName + tSuiteTimestamp directory.
     * Returns null if not found.@return
     */
    @Override
    TCaseResult getTCaseResult(TSuiteName tSuiteName,
                               TExecutionProfile tExecutionProfile,
                               TSuiteTimestamp tSuiteTimestamp,
                               TCaseName tCaseName) {
        return repoRoot_.getTCaseResult(tSuiteName, tExecutionProfile, tSuiteTimestamp, tCaseName)
    }




    @Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }

    @Override
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"MaterialRepository":{')
        sb.append('"baseDir":"' +
                Helpers.escapeAsJsonText(baseDir_.toString()) + '",')
        sb.append('"currentTsName":' + currentTSuiteName_.toJsonText() + ',')
        sb.append('"currentTsTimestamp":' + currentTSuiteTimestamp_.toJsonText() + ',')
        sb.append('"repoRoot":' + repoRoot_.toJsonText() + '')
        sb.append('}}')
        return sb.toString()
    }



    // ================================================================
    //
    //     implementing TSuiteResultTree
    //
    // ----------------------------------------------------------------

    /**
     * implementing TSuiteResultTree
     */
    @Override
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        repoRoot_.addTSuiteResult(tSuiteResult)
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    boolean hasTSuiteResult(TSuiteResult given) {
        throw new UnsupportedOperationException()
    }


    @Override
    List<TSuiteName> getTSuiteNameList() {
        return repoRoot_.getTSuiteNameList()
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile, TSuiteTimestamp tSuiteTimestamp) {
        return repoRoot_.getTSuiteResult(tSuiteName, tExecutionProfile, tSuiteTimestamp)
    }

    /**
     * implementing TSuiteResultTree
     */
    @Override
    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId) {
        repoRoot_.getTSuiteResult(tSuiteResultId)
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResultId> getTSuiteResultIdList(TSuiteName tSuiteName,
                                               TExecutionProfile tExecutionProfile) {
        return repoRoot_.getTSuiteResultIdList(tSuiteName, tExecutionProfile)
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResultId> getTSuiteResultIdList() {
        return repoRoot_.getTSuiteResultIdList()
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResult> getTSuiteResultList() {
        return repoRoot_.getTSuiteResultList()
    }


    /**
     * implementing TSuiteResultTree
     * not used in fact
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName) {
        return repoRoot_.getTSuiteResultList(tSuiteName)
    }


    /**
     * implementing TSuiteResultTree
     * not used in fact
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile) {
        return repoRoot_.getTSuiteResultList(tSuiteName, tExecutionProfile)
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(List<TSuiteResultId> tSuiteResultIdList) {
        return repoRoot_.getTSuiteResultList(tSuiteResultIdList)
    }



    // ---------------------- overriding Object properties --------------------
    @Override
    String toString() {
        return this.toJsonText()
    }

}