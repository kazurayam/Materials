package com.kazurayam.materials.impl

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.RetrievalBy
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.RetrievalBy.SearchContext
import com.kazurayam.materials.repository.RepositoryRoot

class MaterialStorageImpl implements MaterialStorage {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStorageImpl.class)
    
    private Path baseDir_
    private MaterialRepository componentMR_
    
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
        baseDir_ = baseDir
        // create the directory if not present
        Helpers.ensureDirs(baseDir_)
        //
        componentMR_ = MaterialRepositoryFactory.createInstance(baseDir_)
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
        componentMR_.putCurrentTestSuite(tSuiteResultId)
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
    /*
    @Override
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        return componentMR_.getTSuiteResult(tSuiteName, tSuiteTimestamp)
    }
    */
    
    @Override
    long getSize() {
        return componentMR_.getSize()
    }
    
    @Override
    Set<Path> getSetOfMaterialPathRelativeToTSuiteTimestamp(TSuiteName tSuiteName) {
        return componentMR_.getSetOfMaterialPathRelativeToTSuiteTimestamp(tSuiteName)
    }
    
    /**
     * list of TSuiteName, unique
     */
    @Override
    List<TSuiteName> getTSuiteNameList() {
        return componentMR_.getTSuiteNameList()
    }
    
    @Override
    TSuiteResult getTSuiteResult(TSuiteResultId tSuiteResultId) {
        return componentMR_.getTSuiteResult(tSuiteResultId)
    }
    
    @Override
    List<TSuiteResultId> getTSuiteResultIdList(TSuiteName tSuiteName) {
        return componentMR_.getTSuiteResultIdList(tSuiteName)
    }
    
    @Override
    List<TSuiteResultId> getTSuiteResultIdList() {
        return componentMR_.getTSuiteResultIdList()
    }
    
    @Override
    List<TSuiteResult> getTSuiteResultList(List<TSuiteResultId> tSuiteResultIdList) {
        return componentMR_.getTSuiteResultList(tSuiteResultIdList)
    }
    
    @Override
    List<TSuiteResult> getTSuiteResultList() {
        return componentMR_.getTSuiteResultList()
    }
        
    /**
     *
     */
    @Override
    int restore(MaterialRepository intoMR, TSuiteResultId tSuiteResultId, boolean scan = true) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")
        intoMR.putCurrentTestSuite(tSuiteResultId)
        if (componentMR_.getTSuiteResult(tSuiteResultId) == null) {
            throw new IllegalArgumentException("${tSuiteResultId} is not found in ${componentMR_.getBaseDir()}")
        }
        Path fromDir = componentMR_.getTSuiteResult(tSuiteResultId).getTSuiteTimestampDirectory()
        Path toDir   = intoMR.getTSuiteResult(tSuiteResultId).getTSuiteTimestampDirectory()
        boolean skipIfIdentical = true
        
        logger_.debug("#restore processing ${tSuiteResultId} fromDir=${fromDir} toDir=${toDir}")
        int count = Helpers.copyDirectory(fromDir, toDir, skipIfIdentical)
        
        // let the MaterialRepository to scan the disk to reflesh its internal data structure
        if (scan) {
            intoMR.scan()
        }
        // done
        return count
    }
    
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
        Collections.sort(source, new com.kazurayam.materials.TSuiteResult.TimestampFirstTSuiteResultComparator())
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
     * sort a list of TSuiteResult by
     * 1. Descending order of TSuiteTimestamp
     * 2. Ascending order of TSuiteName
     *
    public static class TimestampFirstTSuiteResultComparator implements Comparator<TSuiteResult> {
        @Override
        int compare(TSuiteResult a, TSuiteResult b) {
            int v = a.getId().getTSuiteTimestamp().compareTo(b.getId().getTSuiteTimestamp())
            if (v < 0) {
                return v
            } else if (v == 0) {
                v = a.getId().getTSuiteName().compareTo(b.getId().getTSuiteName())
                return v
            } else {
                return v
            }
        }
    }
     */
    
    @Override
    int restore(MaterialRepository intoMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteResultIdList, "tSuiteResultIdList must not be null")
        int count = 0
        for (TSuiteResultId tsri : tSuiteResultIdList) {
            count += this.restore(intoMR, tsri, false)
        }
        componentMR_.scan()
        return count
    }
    
    @Override
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
                                    RetrievalBy by) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(by, "by must not be null")
        return this.restoreUnary(intoMR, tSuiteName, by)
    }
    
    /**
     *
     */
    @Override
    int restoreUnary(MaterialRepository intoMR, TSuiteName tSuiteName,
                                    RetrievalBy by) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(by, "by must not be null")
        int count = 0
        RetrievalBy.SearchContext context = new SearchContext(this, tSuiteName)
        // find one TSuiteResult object
        TSuiteResult tSuiteResult = by.findTSuiteResult(context)
        // copy the files
        count += this.restore(intoMR, tSuiteResult.getId())
        return count
    }
    
    /**
     * 
     */
    @Override
    int restoreCollective(MaterialRepository intoMR, TSuiteName tSuiteName,
                                    RetrievalBy by) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(by, "by must not be null")
        int count = 0
        RetrievalBy.SearchContext context = new SearchContext(this, tSuiteName)
        // find some TSuiteResult objects
        List<TSuiteResult> list = by.findTSuiteResults(context)
        for (TSuiteResult tSuiteResult : list) {
            // copy the files
            count += this.restore(intoMR, tSuiteResult.getId(), false)
        }
        componentMR_.scan()
        return count
    }
    
    @Override
    void scan() {
        componentMR_.scan()
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
            Helpers.escapeAsJsonText(baseDir_.toString()) + '"')
        sb.append('}}')
        return sb.toString()
    }
}
