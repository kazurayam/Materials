package com.kazurayam.materials.impl

import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.RetrievalBy
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.RetrievalBy.SearchContext
import com.kazurayam.materials.model.repository.RepositoryRoot

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
     */
    @Override
    int backup(MaterialRepository fromMR, TSuiteResultId tSuiteResultId) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        Objects.requireNonNull(tSuiteResultId, "tSuiteResultId must not be null")
        //
        int count = 0
        //
        componentMR_.putCurrentTestSuite(tSuiteResultId)
        List<Material> sourceList = fromMR.getTSuiteResult(tSuiteResultId).getMaterialList()
        for (Material sourceMate : sourceList) {
            TCaseName tcn = sourceMate.getTCaseName()
            Path subpath = sourceMate.getSubpath()
            String fileName = sourceMate.getFileName()
            Path copyTo
            if (subpath != null) {
                copyTo = componentMR_.resolveMaterialPath(tcn, subpath, fileName)
            } else {
                copyTo = componentMR_.resolveMaterialPath(tcn, fileName)
            }
            CopyOption[] options = [ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES ]
            Files.copy(sourceMate.getPath(), copyTo, options)
            logger_.info("copied ${sourceMate.getPath().toString()} into ${copyTo.toString()}")
            count += 1
        }
        // scan the directories/files to update the internal status of componentMR
        componentMR_.scan()
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
            count += this.backup(fromMR, tSuiteResult.getId())
        }
        return count
    }
    
    @Override
    int backup(MaterialRepository fromMR) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        List<TSuiteResult> list = fromMR.getTSuiteResultList()
        logger_.debug("#backup(MaterialRepository) list.size()=${list.size()}")
        int count = 0
        for (TSuiteResult tSuiteResult : list) {
            count += this.backup(fromMR, tSuiteResult.getId())
        }
        return count
    }
    
    @Override
    int clear(TSuiteResultId tSuiteResultId) throws IOException {
        int count = componentMR_.clear(tSuiteResultId)
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

    @Override
    int clear(TSuiteName tSuiteName) throws IOException {
        int count = componentMR_.clear(tSuiteName)
        return count
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
    int restore(MaterialRepository intoMR, TSuiteResultId tSuiteResultId) throws IOException {
        TSuiteName tSuiteName = tSuiteResultId.getTSuiteName()
        TSuiteTimestamp tSuiteTimestamp = tSuiteResultId.getTSuiteTimestamp()
        TSuiteResultId tsri = TSuiteResultId.newInstance(tSuiteName, tSuiteTimestamp)
        intoMR.putCurrentTestSuite(tsri)
        //
        int count = 0
        TSuiteResult tsr = componentMR_.getTSuiteResult(tsri)
        List<Material> sourceList = tsr.getMaterialList()
        for (Material sourceMate : sourceList) {
            TCaseName tcn = sourceMate.getTCaseName()
            Path subpath = sourceMate.getSubpath()
            String fileName = sourceMate.getFileName()
            Path copyTo
            if (subpath != null) {
                copyTo = intoMR.resolveMaterialPath(tcn, subpath, fileName)
            } else {
                copyTo = intoMR.resolveMaterialPath(tcn, fileName)
            }
            CopyOption[] options = [ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES ]
            Files.copy(sourceMate.getPath(), copyTo, options)
            logger_.info("copied ${sourceMate.getPath().toString()} into ${copyTo.toString()}")
            count += 1
        }
        // Is it ok to do this? not sure.
        intoMR.scan()
        // done
        return count
    }
    
    @Override
    int restore(MaterialRepository intoMR, List<TSuiteResultId> tSuiteResultIdList) throws IOException {
        Objects.requireNonNull(intoMR, "intoMR must not be null")
        Objects.requireNonNull(tSuiteResultIdList, "tSuiteResultIdList must not be null")
        int count = 0
        for (TSuiteResultId tsri : tSuiteResultIdList) {
            count += this.restore(intoMR, tsri)
        }
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
            count += this.restore(intoMR, tSuiteResult.getId())
        }
        return count
    }
    

    // ---------------------- overriding Object properties --------------------
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"MaterialStorageImpl":{')
        sb.append('"baseDir":"' +
            Helpers.escapeAsJsonText(baseDir_.toString()) + '"')
        sb.append('}}')
        return sb.toString()
    }
}
