package com.kazurayam.materials.model

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
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.RetrievalBy
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
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        Objects.requireNonNull(tSuiteName, "tSuiteName must not be null")
        Objects.requireNonNull(tSuiteTimestamp, "tSuiteTimestamp must not be null")
        //
        componentMR_.putCurrentTestSuite(tSuiteName, tSuiteTimestamp)
        //
        int count = 0
        TSuiteResult tsr = fromMR.getTSuiteResult(tSuiteName, tSuiteTimestamp)
        List<Material> sourceList = tsr.getMaterialList()
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
            count += 1
        }
        // scan the directories/files to update the internal status of componentMR
        componentMR_.scan()
        // done
        return count
    }
    
    @Override
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName, RetrievalBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    @Override
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        Objects.requireNonNull(tSuiteName, "tSUiteName must not be null")
        List<TSuiteResult> list = fromMR.getTSuiteResultList(tSuiteName)
        int count = 0
        for (TSuiteResult tSuiteResult : list) {
            count += this.backup(fromMR, tSuiteName, tSuiteResult.getTSuiteTimestamp())
        }
        return count
    }
    
    @Override
    int backup(MaterialRepository fromMR) throws IOException {
        Objects.requireNonNull(fromMR, "fromMR must not be null")
        List<TSuiteResult> list = componentMR_.getTSuiteResultList()
        int count = 0
        for (TSuiteResult tSuiteResult : list) {
            count += this.backup(fromMR, tSuiteResult.getTSuiteName())
        }
        return count
    }
    
    @Override
    int clear(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) throws IOException {
        int count = componentMR_.clear(tSuiteName, tSuiteTimestamp)
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
    int expire(TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    @Override
    int expire(TSuiteName tSuiteName,
        RetrievalBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    
    @Override
    Path getBaseDir() {
        return componentMR_.getBaseDir()    
    }
    
    @Override
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        return componentMR_.getTSuiteResult(tSuiteName, tSuiteTimestamp)
    }
    
    @Override
    List<TSuiteResult> getTSuiteResultList(TSuiteName tSuiteName) {
        return componentMR_.getTSuiteResultList(tSuiteName)
    }
    
    @Override
    List<TSuiteResult> getTSuiteResultList() {
        return componentMR_.getTSuiteResultList()
    }
        
    /**
     *
     */
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) throws IOException {
        //
        intoMR.putCurrentTestSuite(tSuiteName, tSuiteTimestamp)
        //
        int count = 0
        TSuiteResult tsr = componentMR_.getTSuiteResult(tSuiteName, tSuiteTimestamp)
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
            count += 1
        }
        // Is it ok to do this? not sure.
        intoMR.scan()
        // done
        return count
    }
    
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        RetrievalBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
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
