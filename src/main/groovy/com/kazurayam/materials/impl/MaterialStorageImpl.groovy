package com.kazurayam.materials.impl

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.RestoreResult
import com.kazurayam.materials.RetrievalBy
import com.kazurayam.materials.RetrievalBy.SearchContext
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.repository.RepositoryRoot
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class MaterialStorageImpl implements MaterialStorage {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStorageImpl.class)
    
    private MaterialRepository componentMR_
    
    private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    /**
     * constructor is hidden
     * 
     * @param baseDir
     */
    private MaterialStorageImpl(Path baseDir) {
        Objects.requireNonNull(baseDir)
        //
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        //
        componentMR_ = MaterialRepositoryFactory.createInstance(baseDir)
    }
    
    /**
     * create a new instance of MaterialStorage at the baseDir
     * 
     * @param baseDir
     * @return a MaterialStorage object
     */
    static MaterialStorage newInstance(Path baseDir) {
        return new MaterialStorageImpl(baseDir)
    }
    
    /**
     * copy Material files belonging to the tSuiteName + tSuiteTimestamp 
     * from the Materials dir of the project into the external Storage directory
     * 
     * @return number of Files copied
     */
    @Override
    int backup(MaterialRepository fromMR, TSuiteResultId tSuiteResultId, boolean scan = true) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")
        
        componentMR_.markAsCurrent(tSuiteResultId)
        def toTSuiteResult = componentMR_.ensureTSuiteResultPresent(tSuiteResultId)
        
        if (fromMR.getTSuiteResult(tSuiteResultId) == null) {
            throw new IllegalArgumentException("${tSuiteResultId} is not found in ${fromMR.getBaseDir()}")
        }
        Path fromDir = fromMR.getTSuiteResult(tSuiteResultId).getTSuiteTimestampDirectory()
        Path   toDir = componentMR_.getTSuiteResult(tSuiteResultId).getTSuiteTimestampDirectory()
        boolean skipIfIdentical = true
        
        int count = Helpers.copyDirectory(fromDir, toDir, skipIfIdentical)
        
        // scan the directories/files to update the internal status of componentMR
        if (scan) {
            componentMR_.scan()
        }
        // done
        return count
    }
    
    @Override
    int backup(MaterialRepository fromMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        Objects.requireNonNull(tSuiteResultIdList, "tSuiteResultIdList must not be null")
        List<TSuiteResult> list = fromMR.getTSuiteResultList(tSuiteResultIdList)
        int count = 0
        for (TSuiteResult tSuiteResult : list) {
            count += this.backup(fromMR, tSuiteResult.getId(), false)
        }
        componentMR_.scan()
        return count
    }

    /*
    @Override
    int backup(MaterialRepository fromMR) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        List<TSuiteResult> list = fromMR.getTSuiteResultList()
        logger_.debug("#backup(MaterialRepository) list.size()=${list.size()}")
        int count = 0
        for (TSuiteResult tSuiteResult : list) {
            count += this.backup(fromMR, tSuiteResult.getId(), false)
        }
        componentMR_.scan()
        return count
    }
     */
    
    @Override
    int clear(TSuiteResultId tSuiteResultId) throws IOException {
        int count = componentMR_.clear(tSuiteResultId, true)
        return count
    }
    
    @Override
    int clear(List<TSuiteResultId> tSuiteResultIdList) throws IOException {
        return componentMR_.clear(tSuiteResultIdList)
    }

    @Override
    int clear(TSuiteName tSuiteName) throws IOException {
        return componentMR_.clear(tSuiteName)
    }


    @Override
    void empty() throws IOException {
        componentMR_.deleteBaseDirContents()
    }
    
    @Override
    Path getBaseDir() {
        return componentMR_.getBaseDir()    
    }
    
    RepositoryRoot getRepositoryRoot() {
        MaterialRepositoryImpl mri = (MaterialRepositoryImpl)componentMR_
        return mri.getRepositoryRoot()
    }


    @Override
    long getSize() {
        return componentMR_.getSize()
    }
    
    @Override
    Set<Path> getSetOfMaterialPathRelativeToTSuiteName(TSuiteName tSuiteName,
                                                       TExecutionProfile tExecutionProfile) {
        return componentMR_.getSetOfMaterialPathRelativeToTSuiteTimestamp(tSuiteName, tExecutionProfile)
    }
    
    /**
     * list of TSuiteName, unique
     */
    @Override
    List<TSuiteName> getTSuiteNameList() {
        return componentMR_.getTSuiteNameList()
    }
    




    /**
     * implementing TSuiteResultTree
     *
     */
    @Override
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        throw new UnsupportedOperationException("because we do not add TSuiteResult through the MaterialStorage view")
    }

    /**
     * implementing TSuiteResultTree
     *
     */
    @Override
    boolean hasTSuiteResult(TSuiteResult tSuiteResult) {
        throw new UnsupportedOperationException()
    }


    /**
     * implementing TSuiteResultTree
     *
     */
    @Override
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile, TSuiteTimestamp tSuiteTimestamp) {
        throw new UnsupportedOperationException()
    }


    /**
     * implementing TSuiteResultTree
     *
     */
    @Override
    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId) {
        return componentMR_.getTSuiteResult(tSuiteResultId)
    }


    /**
     * implementing TSuiteResultTree
     *
     */
    @Override
    List<TSuiteResultId> getTSuiteResultIdList(TSuiteName tSuiteName,
                                               TExecutionProfile tExecutionProfile) {
        return componentMR_.getTSuiteResultIdList(tSuiteName, tExecutionProfile)
    }


    /**
     * implementing TSuiteResultTree
     *
     */
    @Override
    List<TSuiteResultId> getTSuiteResultIdList() {
        return componentMR_.getTSuiteResultIdList()
    }


    /**
     * implementing TSuiteResultTree
     *
     */
    @Override
    List<TSuiteResult> getTSuiteResultList() {
        return componentMR_.getTSuiteResultList()
    }


    /**
     * implementing TSuiteResultTree
     * not used in fact
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName) {
        return componentMR_.getTSuiteResultList(tSuiteName)
    }


    /**
     * implementing TSuiteResultTree
     * not used in fact
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName, TExecutionProfile tExecutionProfile) {
        return componentMR_.getTSuiteResultList(tSuiteName, tExecutionProfile)
    }


    /**
     * implementing TSuiteResultTree
     */
    @Override
    List<TSuiteResult> getTSuiteResultList(List<TSuiteResultId> tSuiteResultIdList) {
        return componentMR_.getTSuiteResultList(tSuiteResultIdList)
    }

    //---------------------------------------------------------------------

    @Override
    void status(Writer output, Map<String, Object> options) {
        TSuiteName pTSuiteName = null
        String key = 'TSuiteName'
        if (options.containsKey(key) &&
            options.get(key) instanceof TSuiteName) {
            pTSuiteName = options.get(key)
        }
        //
        String fmtS = '%-26s\t%-15s\t%20s'
        String fmtD = '%-26s\t%-15s\t%,20d'
        BufferedWriter bw = new BufferedWriter(output)
        bw.println(String.format(
            fmtS,
            '--------TSuiteName-------',
            '---Timestamp---',
            '---sum length---'))
        long sum = 0
        for (TSuiteResult tsr : this.getTSuiteResultList()) {
            //bw.println(tsr.toString())
            if (pTSuiteName == null || 
                tsr.getTSuiteName().equals(pTSuiteName)) {
                bw.println(String.format(
                    fmtD,
                    tsr.getTSuiteName().getValue(),
                    tsr.getTSuiteTimestamp().format(),
                    tsr.getSize()))
                sum += tsr.getSize()
            }
        }
        bw.println(String.format(fmtS, '', '', '================'))
        bw.println(String.format(fmtD, '', '', sum))
        bw.flush()
    }
    
    
    @Override
    long reduce(long targetBytes) throws IOException {
        // need to clone the list as componentMR_.getTSuiteResultList() returns unmodifiable list
        List<TSuiteResult> source = new ArrayList<TSuiteResult>(componentMR_.getTSuiteResultList())
        // sort the list as required
        Collections.sort(source, new TSuiteResult.TimestampFirstTSuiteResultComparator())
        // now calculate
        List<TSuiteResultId> toBeDeleted = new ArrayList<TSuiteResultId>()
        long size = 0
        for (TSuiteResult tsr : source) {
            if (size + tsr.getSize() <= targetBytes) {
                size += tsr.getSize()
            } else {
                toBeDeleted.add(tsr.getId())
            }
        }
        // now delete it
        int numDeletedFiles = componentMR_.clear(toBeDeleted)
        // report the current size of the Storage
        return componentMR_.getSize()
    }


    /**
     *
     */
    @Override
    RestoreResult restore(MaterialRepository intoMR, TSuiteResultId tSuiteResultId, boolean scan = true) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")

        // identify the input directory
        if (componentMR_.getTSuiteResult(tSuiteResultId) == null) {
            //throw new IllegalArgumentException("${tSuiteResultId} is not found in ${componentMR_.getBaseDir()}")
            System.err.println("${tSuiteResultId} is not found in ${componentMR_.getBaseDir()}")
            return RestoreResult.NULL
        }
        Path fromDir = componentMR_.getTSuiteResult(tSuiteResultId).getTSuiteTimestampDirectory()
        
        // make sure the output directory is there
        TSuiteResult tSuiteResultInMR = intoMR.ensureTSuiteResultPresent(tSuiteResultId)
        Path toDir   = tSuiteResultInMR.getTSuiteTimestampDirectory()
        
        // now copy files from the Storage dir into the Materials dir
        logger_.debug("#restore processing ${tSuiteResultId} fromDir=${fromDir} toDir=${toDir}")
        boolean skipIfIdentical = false
        Integer count = Helpers.copyDirectory(fromDir, toDir, skipIfIdentical)
        
        // let the MaterialRepository to scan the disk to recognize the copied files
        if (scan) {
            intoMR.scan()
        }
        
        // done
        return new RestoreResultImpl(new TSuiteResultImpl(tSuiteResultId), count)
    }

    @Override
    List<RestoreResult> restore(MaterialRepository intoMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteResultIdList, "tSuiteResultIdList must not be null")
        List<RestoreResult> restoreResultList = new ArrayList<RestoreResult>()
        for (TSuiteResultId tsri : tSuiteResultIdList) {
            RestoreResult restoreResult = this.restore(intoMR, tsri, false)
            restoreResultList.add(restoreResult)
        }
        //componentMR_.scan()
        return restoreResultList
    }


    /**
     *
     */
    @Override
    RestoreResult retrievingRestoreUnaryExclusive(MaterialRepository intoMR,
                                                  TSuiteName tSuiteName,
                                                  TExecutionProfile tExecutionProfile,
                                                  RetrievalBy by) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
        Objects.requireNonNull(by, "by must not be null")
        SearchContext context = new SearchContext(this, tSuiteName, tExecutionProfile)
        // find one TSuiteResult object
        TSuiteResult tSuiteResult = by.findTSuiteResultBeforeExclusive(context)
		//                                                   ^^ exclusive!
        if (tSuiteResult != TSuiteResult.NULL) {
            // copy the files
            RestoreResult restoreResult = this.restore(intoMR, tSuiteResult.getId())
            return restoreResult
        } else {
            vtLogger_.info("MaterialStorageImpl#restoreUnary by.findTSuiteResult() returned TSuiteResult.NULL")
            return RestoreResult.NULL
        }
    }
    
	/**
	 *
	 */
	@Override
	RestoreResult retrievingRestoreUnaryInclusive(MaterialRepository intoMR,
                                                  TSuiteName tSuiteName,
                                                  TExecutionProfile tExecutionProfile,
                                                  RetrievalBy by) throws IOException {
		Objects.requireNonNull(intoMR, "intoMR must not be null")
		Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tExecutionProfile, "tExecutionProfile must not be null")
		Objects.requireNonNull(by, "by must not be null")
		SearchContext context = new SearchContext(this, tSuiteName, tExecutionProfile)
		// find one TSuiteResult object
		TSuiteResult tSuiteResult = by.findTSuiteResultBeforeInclusive(context)
		//                                                   ^^ inclusive!
		if (tSuiteResult != TSuiteResult.NULL) {
			// copy the files
			RestoreResult restoreResult = this.restore(intoMR, tSuiteResult.getId())
			return restoreResult
		} else {
			vtLogger_.info("MaterialStorageImpl#restoreUnary by.findTSuiteResult() returned TSuiteResult.NULL")
			return RestoreResult.NULL
		}
	}
	    
    @Override
    void scan() {
        componentMR_.scan()
    }

    @Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }
    
    // ---------------------- overriding Object properties --------------------
    @Override
    String toString() {
        return this.toJsonText()
    }

    @Override
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"MaterialStorage":{')
        sb.append('"baseDir":"' +
            Helpers.escapeAsJsonText(this.getBaseDir().toString()) + '",')
        sb.append('"componentMR":' +
            componentMR_.toJsonText())
        sb.append('}}')
        return sb.toString()
    }
}
